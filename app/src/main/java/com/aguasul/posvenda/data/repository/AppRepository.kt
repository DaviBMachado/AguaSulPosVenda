package com.Aquasul.posvenda.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.Aquasul.posvenda.domain.model.Atendimento
import com.Aquasul.posvenda.domain.model.Cliente
import com.Aquasul.posvenda.domain.model.MockDataProvider
import com.Aquasul.posvenda.domain.model.Visita
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "Aquasul_posvenda_prefs")

/**
 * Repositório central de dados do aplicativo.
 * Gerencia o estado persistido localmente em Jetpack DataStore (Preferences)
 * com fallback automático para os dados simulados (mocks) na primeira execução.
 */
class AppRepository private constructor(private val context: Context) {

    private val gson = Gson()

    companion object {
        val KEY_VISITAS = stringPreferencesKey("visitas_json")
        val KEY_CLIENTES = stringPreferencesKey("clientes_json")
        val KEY_ATENDIMENTOS = stringPreferencesKey("atendimentos_json")

        @Volatile
        private var INSTANCE: AppRepository? = null

        fun getInstance(context: Context): AppRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * Retorna a lista de visitas persistidas.
     * Caso o DataStore esteja vazio, inicializa com os dados de MockDataProvider.
     */
    fun getVisitas(): Flow<List<Visita>> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val json = preferences[KEY_VISITAS]
                if (json.isNullOrBlank()) {
                    val defaultList = MockDataProvider.visitasIniciais
                    // Salva assincronamente os dados padrão se estiver vazio
                    salvarVisitasInterno(defaultList)
                    defaultList
                } else {
                    val type = object : TypeToken<List<Visita>>() {}.type
                    gson.fromJson(json, type) ?: MockDataProvider.visitasIniciais
                }
            }
    }

    /**
     * Retorna a lista de clientes persistidos.
     * Caso o DataStore esteja vazio, inicializa com os dados de MockDataProvider.
     */
    fun getClientes(): Flow<List<Cliente>> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val json = preferences[KEY_CLIENTES]
                if (json.isNullOrBlank()) {
                    val defaultList = MockDataProvider.clientesIniciais
                    salvarClientesInterno(defaultList)
                    defaultList
                } else {
                    val type = object : TypeToken<List<Cliente>>() {}.type
                    gson.fromJson(json, type) ?: MockDataProvider.clientesIniciais
                }
            }
    }

    /**
     * Busca um cliente pelo ID.
     */
    fun getClienteById(id: String): Flow<Cliente?> {
        return getClientes().map { lista ->
            lista.find { it.id == id }
        }
    }

    /**
     * Retorna a lista de atendimentos registrados.
     */
    fun getAtendimentos(): Flow<List<Atendimento>> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val json = preferences[KEY_ATENDIMENTOS]
                if (json.isNullOrBlank()) {
                    emptyList()
                } else {
                    val type = object : TypeToken<List<Atendimento>>() {}.type
                    gson.fromJson(json, type) ?: emptyList()
                }
            }
    }

    /**
     * Salva um novo atendimento realizado:
     * 1. Adiciona o atendimento na lista persistida.
     * 2. Atualiza o status da visita correspondente para 'CONCLUIDA'.
     * 3. Caso haja reclamação registrada, atualiza a lista de reclamações do cliente.
     */
    suspend fun salvarAtendimento(atendimento: Atendimento) {
        val atendimentosAtuais = getAtendimentos().first().toMutableList()
        atendimentosAtuais.add(atendimento)

        val visitasAtuais = getVisitas().first().toMutableList()
        val indexVisita = visitasAtuais.indexOfFirst { it.id == atendimento.visitaId }
        if (indexVisita != -1) {
            val visitaAntiga = visitasAtuais[indexVisita]
            visitasAtuais[indexVisita] = visitaAntiga.copy(status = Visita.STATUS_CONCLUIDA)
        }

        val clientesAtuais = getClientes().first().toMutableList()
        if (atendimento.houveReclamacao) {
            val indexCliente = clientesAtuais.indexOfFirst { it.id == atendimento.clienteId }
            if (indexCliente != -1) {
                val clienteAntigo = clientesAtuais[indexCliente]
                val novasReclamacoes = clienteAntigo.reclamacoes.toMutableList()
                novasReclamacoes.add("Registrada em atendimento técnico: ${atendimento.parecerTecnico}")
                clientesAtuais[indexCliente] = clienteAntigo.copy(reclamacoes = novasReclamacoes)
            }
        }

        context.dataStore.edit { preferences ->
            preferences[KEY_ATENDIMENTOS] = gson.toJson(atendimentosAtuais)
            preferences[KEY_VISITAS] = gson.toJson(visitasAtuais)
            preferences[KEY_CLIENTES] = gson.toJson(clientesAtuais)
        }
    }

    /**
     * Reseta os dados persistidos para os valores mock iniciais (útil para testes ou reinicialização).
     */
    suspend fun resetarParaMocks() {
        context.dataStore.edit { preferences ->
            preferences[KEY_VISITAS] = gson.toJson(MockDataProvider.visitasIniciais)
            preferences[KEY_CLIENTES] = gson.toJson(MockDataProvider.clientesIniciais)
            preferences[KEY_ATENDIMENTOS] = gson.toJson(emptyList<Atendimento>())
        }
    }

    private suspend fun salvarVisitasInterno(visitas: List<Visita>) {
        context.dataStore.edit { preferences ->
            if (preferences[KEY_VISITAS].isNullOrBlank()) {
                preferences[KEY_VISITAS] = gson.toJson(visitas)
            }
        }
    }

    private suspend fun salvarClientesInterno(clientes: List<Cliente>) {
        context.dataStore.edit { preferences ->
            if (preferences[KEY_CLIENTES].isNullOrBlank()) {
                preferences[KEY_CLIENTES] = gson.toJson(clientes)
            }
        }
    }
}
