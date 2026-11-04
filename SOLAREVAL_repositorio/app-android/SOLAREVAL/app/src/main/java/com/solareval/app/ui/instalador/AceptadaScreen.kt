package com.solareval.app.ui.instalador

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solareval.app.ui.componentes.BotonPrincipal
import com.solareval.app.ui.pyme.IconoExito
import com.solareval.app.ui.tema.Azul
import com.solareval.app.ui.tema.Borde
import com.solareval.app.ui.tema.Fondo
import com.solareval.app.ui.tema.Gris

@Composable
fun AceptadaScreen(vm: BandejaViewModel, onVolver: () -> Unit) {
    val d = vm.detalle
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).systemBarsPadding().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(70.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            IconoExito()
            Text("Contacto confirmado", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Azul, textAlign = TextAlign.Center)
        }
        val forma = RoundedCornerShape(14.dp)
        Column(
            modifier = Modifier.fillMaxWidth().clip(forma).background(Fondo).border(1.5.dp, Borde, forma).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(d?.negocio ?: "Negocio", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Azul)
            Text("Contacto: ${d?.contactoNombre ?: "sin dato"}", fontSize = 14.sp, color = Gris)
            Text("Correo: ${d?.contactoCorreo ?: "sin dato"}", fontSize = 14.sp, color = Gris)
        }
        Text("Se registró tu comisión por esta solicitud.", fontSize = 15.sp, color = Gris)
        Spacer(Modifier.weight(1f))
        BotonPrincipal("Volver a la bandeja", onVolver)
    }
}
