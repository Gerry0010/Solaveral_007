package com.solareval.app.ui.pyme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solareval.app.ui.componentes.BotonPrincipal
import com.solareval.app.ui.componentes.BotonSecundario
import com.solareval.app.ui.componentes.CampoTexto
import com.solareval.app.ui.componentes.SelectorPeriodo
import com.solareval.app.ui.tema.Azul
import com.solareval.app.ui.tema.ErrorRojo
import com.solareval.app.ui.tema.Gris

@Composable
fun DatosScreen(vm: ReciboViewModel, onTomarOtra: () -> Unit, onConfirmar: () -> Unit) {
    var errorLocal by remember { mutableStateOf<String?>(null) }
    var verTexto by remember { mutableStateOf(false) }
    val noLeido = "No pudimos leer este dato. Escríbelo como aparece en tu recibo."

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TextButton(onClick = onTomarOtra) { Text("← Volver", color = Azul, fontWeight = FontWeight.SemiBold) }
        Text("PASO 2 DE 4", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Gris)
        Text("Confirma la lectura", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Azul)
        Text("Corrige lo que no coincida con tu recibo.", fontSize = 15.sp, color = Gris)

        CampoTexto(
            "Consumo del periodo (kWh)", vm.consumo, { vm.consumo = it }, teclado = KeyboardType.Decimal,
            ayuda = if (vm.consumo.isBlank()) noLeido else null,
        )
        CampoTexto(
            "Tarifa", vm.tarifa, { vm.tarifa = it },
            ayuda = if (vm.tarifa.isBlank()) "Por ejemplo: PDBT o GDBT." else null,
        )
        CampoTexto(
            "Importe total (\$)", vm.importe, { vm.importe = it }, teclado = KeyboardType.Decimal,
            ayuda = if (vm.importe.isBlank()) noLeido else null,
        )
        Text("Periodo de facturación", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Azul)
        SelectorPeriodo(vm.periodo) { vm.periodo = it }

        if (vm.textoLeido.isNotBlank()) {
            TextButton(onClick = { verTexto = !verTexto }) {
                Text(if (verTexto) "Ocultar texto detectado" else "Ver texto detectado en la foto", color = Azul)
            }
            if (verTexto) Text(vm.textoLeido, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = Gris)
        }

        errorLocal?.let { Text(it, color = ErrorRojo, fontSize = 14.sp) }

        BotonPrincipal("Confirmar datos", {
            val consumo = vm.consumoNumero()
            val importe = vm.importeNumero()
            errorLocal = when {
                consumo == null || consumo <= 0 -> "Escribe el consumo en kWh (un número mayor que cero)"
                vm.tarifa.isBlank() -> "Escribe la tarifa de tu recibo"
                importe == null || importe < 0 -> "Escribe el importe total de tu recibo"
                else -> null
            }
            if (errorLocal == null) onConfirmar()
        })
        BotonSecundario("Volver a tomar la foto", onTomarOtra)
    }
}
