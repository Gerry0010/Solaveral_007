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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solareval.app.ui.componentes.BotonPrincipal
import com.solareval.app.ui.tema.Azul
import com.solareval.app.ui.tema.Borde
import com.solareval.app.ui.tema.ErrorRojo
import com.solareval.app.ui.tema.Fondo
import com.solareval.app.ui.tema.Gris

@Composable
fun ContactoScreen(vm: ContactoViewModel, reporteId: Int?, onVolver: () -> Unit, onEnviada: () -> Unit) {
    var autorizo by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { vm.cargar() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TextButton(onClick = onVolver) { Text("← Volver al reporte", color = Azul, fontWeight = FontWeight.SemiBold) }
        Text("Contactar instaladores", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Azul)
        Text("Elige a quién enviar tu reporte. Solo se comparte si lo autorizas.", fontSize = 15.sp, color = Gris)

        when {
            vm.cargando -> CircularProgressIndicator(color = Azul)
            vm.instaladores.isEmpty() && vm.error == null ->
                Text("Por ahora no hay instaladores verificados disponibles.", fontSize = 15.sp, color = Gris)
            else -> vm.instaladores.forEach { inst ->
                val forma = RoundedCornerShape(14.dp)
                Row(
                    modifier = Modifier.fillMaxWidth().clip(forma).background(Fondo).border(1.5.dp, Borde, forma).padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = inst.id in vm.seleccionados,
                        onCheckedChange = { vm.alternar(inst.id) },
                        colors = CheckboxDefaults.colors(checkedColor = Azul),
                    )
                    Column {
                        Text(inst.empresa ?: "Instalador", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Azul)
                        Text("Zona: ${inst.zonaCobertura ?: "sin dato"}", fontSize = 14.sp, color = Gris)
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.Top) {
            Checkbox(
                checked = autorizo,
                onCheckedChange = { autorizo = it },
                colors = CheckboxDefaults.colors(checkedColor = Azul),
            )
            Text(
                "Autorizo compartir mi reporte con los instaladores seleccionados.",
                fontSize = 14.sp, color = Azul, modifier = Modifier.padding(top = 12.dp),
            )
        }

        vm.error?.let { Text(it, color = ErrorRojo, fontSize = 14.sp) }

        BotonPrincipal(
            "Enviar solicitud",
            { if (reporteId != null) vm.enviar(reporteId, onEnviada) },
            cargando = vm.enviando,
            habilitado = autorizo && vm.seleccionados.isNotEmpty() && reporteId != null,
        )
    }
}
