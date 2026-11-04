package com.solareval.app.ui.pyme

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solareval.app.data.ReporteResumen
import com.solareval.app.data.Repositorio
import com.solareval.app.data.SessionStore
import kotlinx.coroutines.launch

class InicioViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repositorio(SessionStore(app))

    var reportes by mutableStateOf<List<ReporteResumen>>(emptyList())
        private set
    var cargando by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun cargar() {
        viewModelScope.launch {
            cargando = true
            error = null
            repo.historial()
                .onSuccess { reportes = it }
                .onFailure { error = it.message }
            cargando = false
        }
    }
}
