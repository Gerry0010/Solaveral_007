package com.solareval.app.ui.pyme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solareval.app.data.ReporteResumen
import com.solareval.app.ui.componentes.MenuInferior
import com.solareval.app.ui.fechaCorta
import com.solareval.app.ui.tema.Azul
import com.solareval.app.ui.tema.Borde
import com.solareval.app.ui.tema.ErrorRojo
import com.solareval.app.ui.tema.Fondo
import com.solareval.app.ui.tema.Gris
import com.solareval.app.ui.textoEstadoSolicitud
import com.solareval.app.ui.textoRecomendacion

fun ReporteResumen.aVista() = VistaReporte(
    id = id, recomendacion = recomendacion, potenciaKw = potenciaKw, ahorroEstimado = ahorroEstimado,
    inversionReferencial = inversionReferencial, retornoInversion = retornoInversion,
    fuenteSolar = fuenteSolar, direccion = direccion,
)

@Composable
fun HistorialScreen(
    vm: InicioViewModel,
    onReporte: (ReporteResumen) -> Unit,
    onInicio: () -> Unit,
    onSalir: () -> Unit,
) {
    LaunchedEffect(Unit) { vm.cargar() }

    Column(Modifier.fillMaxSize().background(Color.White).systemBarsPadding()) {
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Tus reportes", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Azul)
            when {
                vm.cargando -> CircularProgressIndicator(color = Azul)
                vm.error != null -> Text(vm.error ?: "", color = ErrorRojo, fontSize = 14.sp)
                vm.reportes.isEmpty() -> Text("Aún no tienes reportes.", fontSize = 15.sp, color = Gris)
                else -> vm.reportes.forEach { r ->
                    val forma = RoundedCornerShape(14.dp)
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(forma).background(Fondo).border(1.5.dp, Borde, forma)
                            .clickable { onReporte(r) }.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(textoRecomendacion(r.recomendacion), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Azul)
                        Text(
                            listOf(fechaCorta(r.creadoEn), r.direccion ?: "").filter { it.isNotBlank() }.joinToString(" · "),
                            fontSize = 14.sp, color = Gris,
                        )
                        Text(textoEstadoSolicitud(r.estadoSolicitud), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Azul)
                    }
                }
            }
        }
        MenuInferior("historial", onInicio = onInicio, onHistorial = {}, onSalir = onSalir)
    }
}
