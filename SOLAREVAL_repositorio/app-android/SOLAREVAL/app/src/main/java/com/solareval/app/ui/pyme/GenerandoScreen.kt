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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.solareval.app.ui.componentes.LogoSolareval
import com.solareval.app.ui.tema.Azul
import com.solareval.app.ui.tema.Borde
import com.solareval.app.ui.tema.ErrorRojo
import com.solareval.app.ui.tema.Gris
import com.solareval.app.ui.tema.Sol

// Muestra "Generando..." mientras llama al backend; si falla, muestra el error con opciones (RF-13).
@Composable
fun GenerandoScreen(
    vm: ReporteViewModel,
    recibo: ReciboViewModel,
    onListo: () -> Unit,
    onAjustarUbicacion: () -> Unit,
) {
    // Solo genera si no hay un reporte ya creado ni una petición en curso (evita duplicados al girar la pantalla)
    LaunchedEffect(Unit) {
        if (vm.reporte == null && !vm.generando) vm.generar(recibo, onListo)
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).systemBarsPadding().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val error = vm.error
        if (error == null) {
            Spacer(Modifier.height(80.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                LogoSolareval()
                Text("Generando tu reporte", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Azul, textAlign = TextAlign.Center)
                Text("Esto toma unos segundos.", fontSize = 15.sp, color = Gris)
            }
            LinearProgressIndicator(Modifier.fillMaxWidth(), color = Sol, trackColor = Borde)
            Text("• Leyendo los datos de tu recibo", fontSize = 15.sp, color = Azul)
            Text("• Consultando el potencial solar de tu techo", fontSize = 15.sp, color = Azul)
            Text("• Calculando ahorro y retorno de inversión", fontSize = 15.sp, color = Azul)
        } else {
            Spacer(Modifier.height(60.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                IconoError()
                Text("No pudimos evaluar tu techo", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Azul, textAlign = TextAlign.Center)
                Text(error, fontSize = 15.sp, color = Gris, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.weight(1f))
            BotonPrincipal("Ajustar ubicación", onAjustarUbicacion)
            BotonSecundario("Reintentar", { vm.generar(recibo, onListo) })
        }
    }
}

@Composable
private fun IconoError() {
    Canvas(Modifier.size(72.dp).semantics { contentDescription = "Error" }) {
        drawCircle(ErrorRojo)
        val c = center
        val d = size.minDimension * 0.18f
        drawLine(Color.White, Offset(c.x - d, c.y - d), Offset(c.x + d, c.y + d), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
        drawLine(Color.White, Offset(c.x + d, c.y - d), Offset(c.x - d, c.y + d), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    }
}
