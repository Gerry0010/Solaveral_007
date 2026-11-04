package com.solareval.app.ui.pyme

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solareval.app.data.InstaladorItem
import com.solareval.app.data.Repositorio
import com.solareval.app.data.SessionStore
import com.solareval.app.data.SolicitudRequest
import kotlinx.coroutines.launch

class ContactoViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repositorio(SessionStore(app))

    var instaladores by mutableStateOf<List<InstaladorItem>>(emptyList())
        private set
    var seleccionados by mutableStateOf<Set<Int>>(emptySet())
        private set
    var cargando by mutableStateOf(false)
        private set
    var enviando by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun cargar() {
        viewModelScope.launch {
            cargando = true
            error = null
            seleccionados = emptySet()
            repo.instaladores().onSuccess { instaladores = it }.onFailure { error = it.message }
            cargando = false
        }
    }

    fun alternar(id: Int) {
        seleccionados = if (id in seleccionados) seleccionados - id else seleccionados + id
    }

    fun enviar(reporteId: Int, onListo: () -> Unit) {
        viewModelScope.launch {
            enviando = true
            error = null
            // consentimiento = true: la PyME marcó la casilla de autorización en la pantalla
            repo.crearSolicitudes(SolicitudRequest(reporteId, seleccionados.toList(), true))
                .onSuccess {
                    if (it.solicitudes.isEmpty()) error = "Ya enviaste este reporte a esos instaladores."
                    else onListo()
                }
                .onFailure { error = it.message }
            enviando = false
        }
    }
}
