package com.solareval.app.ui.pyme

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.solareval.app.data.ParserRecibo
import java.io.File
import java.io.IOException

// Guarda los datos del recibo mientras la PyME avanza por las pantallas (foto, confirmar, ubicación...).
class ReciboViewModel(app: Application) : AndroidViewModel(app) {
    var consumo by mutableStateOf("")
    var tarifa by mutableStateOf("")
    var importe by mutableStateOf("")
    var periodo by mutableStateOf("bimestral")
    var textoLeido by mutableStateOf("")
        private set
    var procesando by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun reiniciar() {
        consumo = ""; tarifa = ""; importe = ""; periodo = "bimestral"
        textoLeido = ""; procesando = false; error = null
    }

    /** Lee la imagen con ML Kit (en el teléfono: la foto no se sube a ningún servidor). */
    fun leerImagen(context: Context, uri: Uri, temporal: File?, onListo: () -> Unit) {
        procesando = true
        error = null
        val imagen = try {
            InputImage.fromFilePath(context, uri)
        } catch (e: IOException) {
            procesando = false
            error = "No se pudo abrir la imagen. Intenta de nuevo."
            temporal?.delete()
            return
        }
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            .process(imagen)
            .addOnSuccessListener { resultado ->
                temporal?.delete()                         // RNF-05: la foto no se conserva
                val texto = resultado.text
                procesando = false
                if (texto.isBlank()) {
                    error = "No pudimos leer texto en la foto. Toma otra con buena luz o captura los datos a mano."
                } else {
                    textoLeido = texto
                    val d = ParserRecibo.analizar(texto)
                    consumo = d.consumoKwh?.let { numeroParaCampo(it) } ?: ""
                    tarifa = d.tarifa ?: ""
                    importe = d.importe?.let { numeroParaCampo(it) } ?: ""
                    d.periodo?.let { periodo = it }
                    onListo()
                }
            }
            .addOnFailureListener {
                temporal?.delete()
                procesando = false
                error = "No se pudo leer el recibo. Toma otra foto o captura los datos a mano."
            }
    }

    fun consumoNumero(): Double? = ParserRecibo.numero(consumo)
    fun importeNumero(): Double? = ParserRecibo.numero(importe)

    private fun numeroParaCampo(v: Double): String =
        if (v % 1.0 == 0.0) v.toLong().toString() else v.toString()
}
