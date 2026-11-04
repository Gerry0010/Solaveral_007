package com.solareval.app.ui.instalador

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solareval.app.data.Repositorio
import com.solareval.app.data.SessionStore
import com.solareval.app.data.SolicitudDetalle
import com.solareval.app.data.SolicitudItem
import kotlinx.coroutines.launch

class BandejaViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repositorio(SessionStore(app))

    var solicitudes by mutableStateOf<List<SolicitudItem>>(emptyList())
        private set
    var detalle by mutableStateOf<SolicitudDetalle?>(null)
        private set
    var idSeleccionado by mutableStateOf<Int?>(null)
    var cargando by mutableStateOf(false)
        private set
    var procesando by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun cargarBandeja() {
        viewModelScope.launch {
            cargando = true
            error = null
            repo.bandeja().onSuccess { solicitudes = it }.onFailure { error = it.message }
            cargando = false
        }
    }

    fun cargarDetalle() {
        val id = idSeleccionado ?: return
        viewModelScope.launch {
            cargando = true
            error = null
            detalle = null
            repo.detalleSolicitud(id).onSuccess { detalle = it }.onFailure { error = it.message }
            cargando = false
        }
    }

    /** estado: "aceptada" o "rechazada". Al aceptar, el backend registra la comisión. */
    fun cambiarEstado(estado: String, onListo: () -> Unit) {
        val id = idSeleccionado ?: return
        viewModelScope.launch {
            procesando = true
            error = null
            repo.cambiarEstado(id, estado)
                .onSuccess { detalle = it; onListo() }
                .onFailure { error = it.message }
            procesando = false
        }
    }
}
