package com.Aquasul.posvenda

import android.app.Application
import com.Aquasul.posvenda.data.repository.AppRepository

/**
 * Classe de aplicação responsável pela inicialização dos componentes globais
 * e fornecimento do repositório singleton.
 */
class PosVendaApplication : Application() {

    val repository: AppRepository by lazy {
        AppRepository.getInstance(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
    }
}
