package com.solareval.app.ui.instalador

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
import com.solareval.app.ui.componentes.BotonSecundario
import com.solareval.app.ui.fechaCorta
import com.solareval.app.ui.tema.Azul
import com.solareval.app.ui.tema.Borde
import com.solareval.app.ui.tema.ErrorRojo
import com.solareval.app.ui.tema.Fondo
import com.solareval.app.ui.tema.Gris
import com.solareval.app.ui.tema.Sol
import com.solareval.app.ui.textoRecomendacion

private val VerdeOk = Color(0xFF1B6B4F)

fun textoEstadoInstalador(estado: String) = when (estado) {
    "pendiente" -> "Pendiente"
    "aceptada" -> "Aceptada"
    "rechazada" -> "Rechazada"
    else -> estado
}

@Composable
fun BandejaScreen(vm: BandejaViewModel, nombre: String, onAbrir: (Int) -> Unit, onSalir: () -> Unit) {
    LaunchedEffect(Unit) { vm.cargarBandeja() }

    Column(Modifier.fillMaxSize().background(Color.White).systemBarsPadding()) {
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Solicitudes", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Azul)
            Text("Hola, $nombre. Clientes que quieren que los contactes.", fontSize = 15.sp, color = Gris)
            when {
                vm.cargando -> CircularProgressIndicator(color = Azul)
                vm.error != null -> Text(vm.error ?: "", color = ErrorRojo, fontSize = 14.sp)
                vm.solicitudes.isEmpty() -> Text("Aún no tienes solicitudes.", fontSize = 15.sp, color = Gris)
                else -> vm.solicitudes.forEach { s ->
                    val forma = RoundedCornerShape(14.dp)
                    Column(
                        modifier = Modifier.fillMaxWidth().clip(forma).background(Fondo).border(1.5.dp, Borde, forma)
                            .clickable { onAbrir(s.id) }.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(s.negocio ?: "Negocio", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Azul)
                        Text(
                            listOf(s.giro ?: "", s.direccion ?: "", fechaCorta(s.creadoEn)).filter { it.isNotBlank() }.joinToString(" · "),
                            fontSize = 14.sp, color = Gris,
                        )
                        Text(textoRecomendacion(s.recomendacion), fontSize = 13.sp, color = Azul)
                        val (fondo, texto) = when (s.estado) {
                            "aceptada" -> VerdeOk to Color.White
                            "rechazada" -> Gris to Color.White
                            else -> Sol to Azul
                        }
                        Text(
                            textoEstadoInstalador(s.estado),
                            modifier = Modifier.clip(RoundedCornerShape(99.dp)).background(fondo).padding(horizontal = 12.dp, vertical = 5.dp),
                            color = texto, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
        Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) { BotonSecundario("Salir", onSalir) }
    }
}
