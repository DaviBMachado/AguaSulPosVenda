package com.aguasul.posvenda.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aguasul.posvenda.domain.model.Atendimento
import com.aguasul.posvenda.domain.model.Cliente
import com.aguasul.posvenda.domain.model.ClienteCritico
import com.aguasul.posvenda.domain.model.MetricasRelatorio
import com.aguasul.posvenda.domain.model.MockDataProvider
import com.aguasul.posvenda.domain.model.OrigemReclamacao
import com.aguasul.posvenda.domain.model.Reclamacao
import com.aguasul.posvenda.domain.model.SessaoUsuario
import com.aguasul.posvenda.domain.model.StatusReclamacao
import com.aguasul.posvenda.domain.model.TipoPerfil
import com.aguasul.posvenda.domain.model.Visita
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "aguasul_posvenda_prefs")

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
        val KEY_RECLAMACOES = stringPreferencesKey("reclamacoes_json")

        val KEY_SESSAO_PERFIL = stringPreferencesKey("sessao_perfil")
        val KEY_SESSAO_CLIENTE_ID = stringPreferencesKey("sessao_cliente_id")
        val KEY_SESSAO_NOME = stringPreferencesKey("sessao_nome")

        @Volatile
        private var INSTANCE: AppRepository? = null

        fun getInstance(context: Context): AppRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppRepository(context.applicationContext).also { INSTANCE = it }
            }
        }

        fun calcularMetricas(
            clientes: List<Cliente>,
            visitas: List<Visita>,
            reclamacoes: List<Reclamacao>
        ): MetricasRelatorio {
            val totalClientes = clientes.size
            var alta = 0
            var media = 0
            var baixa = 0

            clientes.forEach { cliente ->
                when (cliente.usabilidade.trim().lowercase()) {
                    "excelente", "boa", "alta", "ótima", "otima" -> alta++
                    "satisfatória", "satisfatoria", "média", "media", "regular" -> media++
                    else -> baixa++
                }
            }

            val percentualAlta = if (totalClientes > 0) (alta * 100) / totalClientes else 0
            val percentualMedia = if (totalClientes > 0) (media * 100) / totalClientes else 0
            val percentualBaixa = if (totalClientes > 0) (baixa * 100) / totalClientes else 0

            val totalReclamacoes = reclamacoes.size
            val resolvidas = reclamacoes.count { it.status == StatusReclamacao.RESOLVIDA }
            val pendentes = totalReclamacoes - resolvidas
            val taxaResolucao = if (totalReclamacoes > 0) (resolvidas * 100) / totalReclamacoes else 0

            val totalVisitas = visitas.size
            val concluidas = visitas.count { it.status == Visita.STATUS_CONCLUIDA }
            val preventivas = visitas.count { it.tipo == Visita.TIPO_PREVENTIVA }
            val corretivas = visitas.count { it.tipo == Visita.TIPO_CORRETIVA }
            val outras = visitas.count { it.tipo != Visita.TIPO_PREVENTIVA && it.tipo != Visita.TIPO_CORRETIVA }

            val clientesCriticos = reclamacoes
                .filter { it.status != StatusReclamacao.RESOLVIDA }
                .groupBy { it.clienteId }
                .map { (clienteId, recs) ->
                    val nome = recs.firstOrNull()?.clienteNome
                        ?: clientes.find { it.id == clienteId }?.nome
                        ?: clienteId
                    ClienteCritico(
                        clienteId = clienteId,
                        clienteNome = nome,
                        totalReclamacoesAbertas = recs.size
                    )
                }
                .sortedByDescending { it.totalReclamacoesAbertas }

            return MetricasRelatorio(
                totalClientes = totalClientes,
                usabilidadeAlta = alta,
                usabilidadeMedia = media,
                usabilidadeBaixa = baixa,
                percentualAlta = percentualAlta,
                percentualMedia = percentualMedia,
                percentualBaixa = percentualBaixa,
                totalReclamacoes = totalReclamacoes,
                reclamacoesResolvidas = resolvidas,
                reclamacoesPendentes = pendentes,
                taxaResolucaoPercentual = taxaResolucao,
                totalVisitas = totalVisitas,
                visitasConcluidas = concluidas,
                visitasPreventivas = preventivas,
                visitasCorretivas = corretivas,
                visitasOutras = outras,
                clientesCriticos = clientesCriticos
            )
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
     * 3. Caso haja reclamação registrada, atualiza a lista de reclamações do cliente e registra a queixa.
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
        val reclamacoesAtuais = obterReclamacoes().first().toMutableList()

        if (atendimento.houveReclamacao) {
            val indexCliente = clientesAtuais.indexOfFirst { it.id == atendimento.clienteId }
            if (indexCliente != -1) {
                val clienteAntigo = clientesAtuais[indexCliente]
                val novasReclamacoes = clienteAntigo.reclamacoes.toMutableList()
                novasReclamacoes.add("Registrada em atendimento técnico: ${atendimento.parecerTecnico}")
                clientesAtuais[indexCliente] = clienteAntigo.copy(reclamacoes = novasReclamacoes)
            }
            reclamacoesAtuais.add(
                0,
                Reclamacao(
                    id = "rec_${System.currentTimeMillis()}",
                    clienteId = atendimento.clienteId,
                    clienteNome = atendimento.clienteNome,
                    dataAbertura = atendimento.dataHora,
                    descricao = "Queixa técnica registrada em atendimento: ${atendimento.parecerTecnico}",
                    status = StatusReclamacao.ABERTA,
                    origem = OrigemReclamacao.VISITA_TECNICA
                )
            )
        }

        context.dataStore.edit { preferences ->
            preferences[KEY_ATENDIMENTOS] = gson.toJson(atendimentosAtuais)
            preferences[KEY_VISITAS] = gson.toJson(visitasAtuais)
            preferences[KEY_CLIENTES] = gson.toJson(clientesAtuais)
            if (atendimento.houveReclamacao) {
                preferences[KEY_RECLAMACOES] = gson.toJson(reclamacoesAtuais)
            }
        }
    }

    // ==========================================
    // Controle de Sessão
    // ==========================================

    /**
     * Salva as credenciais e estado da sessão ativa do usuário.
     */
    suspend fun salvarSessao(tipoPerfil: TipoPerfil, clienteId: String?, nome: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SESSAO_PERFIL] = tipoPerfil.name
            if (clienteId != null) {
                preferences[KEY_SESSAO_CLIENTE_ID] = clienteId
            } else {
                preferences.remove(KEY_SESSAO_CLIENTE_ID)
            }
            preferences[KEY_SESSAO_NOME] = nome
        }
    }

    /**
     * Retorna a sessão ativa atual ou null caso o usuário não esteja autenticado.
     */
    fun obterSessao(): Flow<SessaoUsuario?> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val perfilStr = preferences[KEY_SESSAO_PERFIL] ?: return@map null
                val perfil = runCatching { TipoPerfil.valueOf(perfilStr) }.getOrNull() ?: return@map null
                val nome = preferences[KEY_SESSAO_NOME] ?: ""
                val clienteId = preferences[KEY_SESSAO_CLIENTE_ID]
                SessaoUsuario(perfil = perfil, clienteId = clienteId, nomeExibicao = nome)
            }
    }

    /**
     * Realiza o logout do usuário, limpando apenas as chaves da sessão ativa
     * e mantendo intactos todos os dados de clientes, visitas, atendimentos e reclamações.
     */
    suspend fun fazerLogout() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_SESSAO_PERFIL)
            preferences.remove(KEY_SESSAO_CLIENTE_ID)
            preferences.remove(KEY_SESSAO_NOME)
        }
    }

    // ==========================================
    // Gerenciamento de Reclamações
    // ==========================================

    /**
     * Retorna a lista de reclamações e queixas técnicas registradas.
     * Caso o DataStore esteja vazio, inicializa com os dados de MockDataProvider.
     */
    fun obterReclamacoes(): Flow<List<Reclamacao>> {
        return context.dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                val json = preferences[KEY_RECLAMACOES]
                if (json.isNullOrBlank()) {
                    val defaultList = MockDataProvider.reclamacoesIniciais
                    salvarReclamacoesInterno(defaultList)
                    defaultList
                } else {
                    val type = object : TypeToken<List<Reclamacao>>() {}.type
                    gson.fromJson(json, type) ?: MockDataProvider.reclamacoesIniciais
                }
            }
    }

    /**
     * Retorna as reclamações de um cliente específico.
     */
    fun obterReclamacoesPorCliente(clienteId: String): Flow<List<Reclamacao>> {
        return obterReclamacoes().map { lista ->
            lista.filter { it.clienteId == clienteId }
        }
    }

    /**
     * Adiciona uma nova reclamação ao repositório persistido.
     */
    suspend fun adicionarReclamacao(reclamacao: Reclamacao) {
        val listaAtual = obterReclamacoes().first().toMutableList()
        listaAtual.add(0, reclamacao)
        context.dataStore.edit { preferences ->
            preferences[KEY_RECLAMACOES] = gson.toJson(listaAtual)
        }
    }

    /**
     * Atualiza o status e/ou parecer de resolução de uma reclamação existente.
     */
    suspend fun atualizarStatusReclamacao(id: String, novoStatus: StatusReclamacao, resolucao: String? = null) {
        val listaAtual = obterReclamacoes().first().toMutableList()
        val index = listaAtual.indexOfFirst { it.id == id }
        if (index != -1) {
            val antiga = listaAtual[index]
            listaAtual[index] = antiga.copy(
                status = novoStatus,
                resolucaoTecnica = resolucao ?: antiga.resolucaoTecnica
            )
            context.dataStore.edit { preferences ->
                preferences[KEY_RECLAMACOES] = gson.toJson(listaAtual)
            }
        }
    }

    // ==========================================
    // Métricas de Usabilidade e Relatórios
    // ==========================================

    /**
     * Retorna um Flow reativo contendo as métricas de usabilidade, resolução e serviços.
     */
    fun obterMetricasUsabilidade(): Flow<MetricasRelatorio> {
        return combine(getClientes(), getVisitas(), obterReclamacoes()) { clientes, visitas, reclamacoes ->
            calcularMetricas(clientes, visitas, reclamacoes)
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
            preferences[KEY_RECLAMACOES] = gson.toJson(MockDataProvider.reclamacoesIniciais)
            preferences.remove(KEY_SESSAO_PERFIL)
            preferences.remove(KEY_SESSAO_CLIENTE_ID)
            preferences.remove(KEY_SESSAO_NOME)
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

    private suspend fun salvarReclamacoesInterno(reclamacoes: List<Reclamacao>) {
        context.dataStore.edit { preferences ->
            if (preferences[KEY_RECLAMACOES].isNullOrBlank()) {
                preferences[KEY_RECLAMACOES] = gson.toJson(reclamacoes)
            }
        }
    }
}
