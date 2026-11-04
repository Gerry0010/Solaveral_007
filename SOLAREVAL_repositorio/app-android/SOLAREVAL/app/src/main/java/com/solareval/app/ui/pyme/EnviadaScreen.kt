package com.solareval.app.ui.pyme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solareval.app.ui.componentes.BotonPrincipal
import com.solareval.app.ui.componentes.BotonSecundario
import com.solareval.app.ui.tema.Azul
import com.solareval.app.ui.tema.Gris

private val VerdeOk = Color(0xFF1B6B4F)

@Composable
fun IconoExito() {
    Canvas(Modifier.size(72.dp).semantics { contentDescription = "Listo" }) {
        drawCircle(VerdeOk)
        val w = size.width
        drawLine(Color.White, Offset(w * 0.30f, w * 0.52f), Offset(w * 0.45f, w * 0.67f), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
        drawLine(Color.White, Offset(w * 0.45f, w * 0.67f), Offset(w * 0.70f, w * 0.36f), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Composable
fun EnviadaScreen(onHistorial: () -> Unit, onInicio: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).systemBarsPadding().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(80.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            IconoExito()
            Text("Solicitud enviada", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Azul, textAlign = TextAlign.Center)
            Text(
                "Enviamos tu reporte a los instaladores que elegiste. Te avisaremos cuando alguno la acepte.",
                fontSize = 15.sp, color = Gris, textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.weight(1f))
        BotonPrincipal("Ver mis reportes", onHistorial)
        BotonSecundario("Ir al inicio", onInicio)
    }
}
