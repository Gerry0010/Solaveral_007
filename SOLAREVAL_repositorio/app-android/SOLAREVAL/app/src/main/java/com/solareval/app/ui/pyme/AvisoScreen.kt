package com.solareval.app.ui.pyme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solareval.app.ui.componentes.BotonPrincipal
import com.solareval.app.ui.tema.Azul

@Composable
fun AvisoScreen(onEntendido: () -> Unit) {
    val puntos = listOf(
        "Qué datos usamos" to "consumo, tarifa e importe de tu recibo y la ubicación de tu negocio.",
        "Para qué" to "generar tu reporte de viabilidad solar.",
        "Con quién se comparten" to "solo con los instaladores que tú elijas.",
        "Cuánto tiempo" to "la foto del recibo se lee en tu teléfono y se elimina; solo se guardan los valores leídos.",
        "Tus derechos" to "puedes eliminar tus reportes y tu cuenta cuando quieras.",
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TextButton(onClick = onEntendido) { Text("← Volver", color = Azul, fontWeight = FontWeight.SemiBold) }
        Text("Aviso de privacidad", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Azul)
        puntos.forEach { (titulo, texto) ->
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("$titulo: ") }
                    append(texto)
                },
                fontSize = 15.sp, lineHeight = 21.sp, color = Azul,
            )
        }
        BotonPrincipal("Entendido", onEntendido)
    }
}
