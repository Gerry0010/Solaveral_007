package com.solareval.app.ui.pyme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solareval.app.ui.componentes.BotonPrincipal
import com.solareval.app.ui.componentes.BotonSecundario
import com.solareval.app.ui.tema.Azul
import com.solareval.app.ui.tema.Borde
import com.solareval.app.ui.tema.Fondo
import com.solareval.app.ui.tema.Gris
import com.solareval.app.ui.tema.Sol
import com.solareval.app.ui.textoRecomendacion
import java.util.Locale

private val MX = Locale("es", "MX")
private val VerdeOk = Color(0xFF1B6B4F)

@Composable
fun ReporteScreen(
    reporte: VistaReporte?,
    onInicio: () -> Unit,
    onSolicitarContacto: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TextButton(onClick = onInicio) { Text("← Inicio", color = Azul, fontWeight = FontWeight.SemiBold) }
        if (reporte == null) {
            Text("No hay un reporte para mostrar.", fontSize = 15.sp, color = Gris)
            BotonPrincipal("Ir al inicio", onInicio)
            return@Column
        }
        Text("PASO 4 DE 4", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Gris)
        Text("Tu reporte de viabilidad", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Azul)

        // Insignia con la recomendación
        val (fondo, texto) = when (reporte.recomendacion) {
            "conviene_ahora" -> VerdeOk to Color.White
            "conviene_con_condiciones" -> Sol to Azul
            else -> Gris to Color.White
        }
        Text(
            textoRecomendacion(reporte.recomendacion),
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(fondo).padding(horizontal = 16.dp, vertical = 14.dp),
            color = texto, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold,
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Metrica("Potencia recomendada", String.format(MX, "%.1f kW", reporte.potenciaKw), Modifier.weight(1f))
            Metrica("Ahorro anual", dinero(reporte.ahorroEstimado), Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Metrica("Inversión referencial", dinero(reporte.inversionReferencial), Modifier.weight(1f))
            Metrica(
                "Retorno de inversión",
                reporte.retornoInversion?.let { String.format(MX, "%.1f años", it) } ?: "Sin retorno",
                Modifier.weight(1f),
            )
        }

        Text("Estimación preliminar: no es cotización ni garantía de ahorro.", fontSize = 14.sp, color = Gris)
        if (reporte.fuenteSolar == "demo") {
            Text(
                "Estimación de demostración: todavía no usa datos satelitales reales del techo.",
                fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Azul,
            )
        }

        BotonPrincipal("Solicitar contacto con instalador", onSolicitarContacto)
        BotonSecundario("Descargar PDF", {}, habilitado = false)
    }
}

@Composable
private fun Metrica(etiqueta: String, valor: String, modifier: Modifier) {
    val forma = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier.clip(forma).background(Fondo).border(1.5.dp, Borde, forma).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(etiqueta, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Gris)
        Text(valor, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Azul)
    }
}

private fun dinero(v: Double): String = String.format(MX, "\$%,.0f", v)
