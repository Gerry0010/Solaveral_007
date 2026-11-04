package com.solareval.app.ui.pyme

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solareval.app.data.ReporteCreado
import com.solareval.app.data.ReporteRequest
import com.solareval.app.data.Repositorio
import com.solareval.app.data.SessionStore
import kotlinx.coroutines.launch
import java.util.Locale

// Lo que muestra la pantalla del reporte (sirve para un reporte nuevo y, después, para uno del historial).
data class VistaReporte(
    val id: Int,
    val recomendacion: String,
    val potenciaKw: Double,
    val ahorroEstimado: Double,
    val inversionReferencial: Double,
    val retornoInversion: Double?,
    val fuenteSolar: String,
    val direccion: String?,
)

class ReporteViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repositorio(SessionStore(app))

    // ---- Ubicación elegida en el mapa
    var latitud by mutableStateOf(MEXICO_LAT)
    var longitud by mutableStateOf(MEXICO_LON)
    var zoom by mutableStateOf(MEXICO_ZOOM)
    var direccion by mutableStateOf("")

    // Cuando se pide mover el mapa (GPS o búsqueda), se guarda el destino y se sube la versión.
    var destinoLat by mutableStateOf(MEXICO_LAT)
        private set
    var destinoLon by mutableStateOf(MEXICO_LON)
        private set
    var versionDestino by mutableStateOf(0)
        private set

    // ---- Resultado
    var generando by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var reporte by mutableStateOf<VistaReporte?>(null)

    fun centrarEn(lat: Double, lon: Double) {
        destinoLat = lat
        destinoLon = lon
        versionDestino += 1
    }

    fun reiniciar() {
        latitud = MEXICO_LAT; longitud = MEXICO_LON; zoom = MEXICO_ZOOM; direccion = ""
        destinoLat = MEXICO_LAT; destinoLon = MEXICO_LON
        generando = false; error = null; reporte = null
    }

    fun generar(recibo: ReciboViewModel, onListo: () -> Unit) {
        val consumo = recibo.consumoNumero()
        val importe = recibo.importeNumero()
        if (consumo == null || importe == null) {
            error = "Faltan los datos del recibo. Regresa y confirma la lectura."
            return
        }
        val texto = direccion.trim().ifBlank { "%.5f, %.5f".format(Locale.US, latitud, longitud) }
        viewModelScope.launch {
            generando = true
            error = null
            // consentimiento = true: la PyME ya aceptó el aviso de privacidad en la pantalla del recibo
            repo.crearReporte(
                ReporteRequest(consumo, recibo.tarifa.trim(), importe, recibo.periodo, latitud, longitud, texto, true)
            )
                .onSuccess {
                    reporte = it.aVista(texto)
                    onListo()
                }
                .onFailure { error = it.message }
            generando = false
        }
    }

    companion object {
        // Vista inicial: centro de México, sin ubicación elegida todavía
        const val MEXICO_LAT = 23.6345
        const val MEXICO_LON = -102.5528
        const val MEXICO_ZOOM = 5.0
        const val ZOOM_MINIMO_PARA_GENERAR = 14.0
    }
}

private fun ReporteCreado.aVista(direccion: String) = VistaReporte(
    id = id,
    recomendacion = recomendacion,
    potenciaKw = potenciaKw,
    ahorroEstimado = ahorroEstimado,
    inversionReferencial = inversionReferencial,
    retornoInversion = retornoInversion,
    fuenteSolar = fuenteSolar,
    direccion = direccion,
)
