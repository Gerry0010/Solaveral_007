package com.solareval.app.ui.instalador

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solareval.app.ui.componentes.BotonPrincipal
import com.solareval.app.ui.componentes.BotonSecundario
import com.solareval.app.ui.pyme.IconoExito
import com.solareval.app.ui.tema.Azul
import com.solareval.app.ui.tema.Borde
import com.solareval.app.ui.tema.ErrorRojo
import com.solareval.app.ui.tema.Fondo
import com.solareval.app.ui.tema.Gris
import com.solareval.app.ui.tema.Sol
import com.solareval.app.ui.textoRecomendacion
import java.util.Locale

private val MX = Locale("es", "MX")

@Composable
fun DetalleSolicitudScreen(
    vm: BandejaViewModel,
    onVolver: () -> Unit,
    onAceptada: () -> Unit,
    onRechazada: () -> Unit,
) {
    LaunchedEffect(Unit) { vm.cargarDetalle() }
    val d = vm.detalle

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).systemBarsPadding()
            .verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TextButton(onClick = onVolver) { Text("← Bandeja", color = Azul, fontWeight = FontWeight.SemiBold) }
        when {
            vm.cargando -> CircularProgressIndicator(color = Azul)
            d == null -> Text(vm.error ?: "No se encontró la solicitud.", color = ErrorRojo, fontSize = 15.sp)
            else -> {
                Text(d.negocio ?: "Negocio", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Azul)
                Text(listOf(d.giro ?: "", d.direccion ?: "").filter { it.isNotBlank() }.joinToString(" · "), fontSize = 15.sp, color = Gris)
                Text(
                    textoRecomendacion(d.recomendacion),
                    modifier = Modifier.clip(RoundedCornerShape(99.dp)).background(Sol).padding(horizontal = 14.dp, vertical = 6.dp),
                    color = Azul, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Dato("Potencia", String.format(MX, "%.1f kW", d.potenciaKw), Modifier.weight(1f))
                    Dato("Ahorro anual", String.format(MX, "\$%,.0f", d.ahorroEstimado), Modifier.weight(1f))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Dato("Inversión", String.format(MX, "\$%,.0f", d.inversionReferencial), Modifier.weight(1f))
                    Dato("Retorno", d.retornoInversion?.let { String.format(MX, "%.1f años", it) } ?: "Sin retorno", Modifier.weight(1f))
                }
                Text("Estimación preliminar generada por SOLAREVAL; no es una cotización.", fontSize = 14.sp, color = Gris)
                vm.error?.let { Text(it, color = ErrorRojo, fontSize = 14.sp) }
                Spacer(Modifier.height(4.dp))
                if (d.estado == "pendiente") {
                    BotonPrincipal("Aceptar solicitud", { vm.cambiarEstado("aceptada", onAceptada) }, cargando = vm.procesando)
                    BotonSecundario("Rechazar", { vm.cambiarEstado("rechazada", onRechazada) }, habilitado = !vm.procesando)
                } else {
                    Text("Esta solicitud ya fue ${textoEstadoInstalador(d.estado).lowercase()}.", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Azul)
                    if (d.estado == "aceptada") BotonPrincipal("Ver contacto", onAceptada)
                }
            }
        }
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String, modifier: Modifier) {
    val forma = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier.clip(forma).background(Fondo).border(1.5.dp, Borde, forma).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(etiqueta, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Gris)
        Text(valor, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Azul)
    }
}
