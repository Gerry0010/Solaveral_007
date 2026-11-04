package com.solareval.app.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solareval.app.ui.tema.Azul
import com.solareval.app.ui.tema.Borde
import com.solareval.app.ui.tema.Gris
import com.solareval.app.ui.tema.Sol
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LogoSolareval() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Canvas(Modifier.size(36.dp).semantics { contentDescription = "Logo de SOLAREVAL" }) {
            val c = center
            val r = size.minDimension * 0.2f
            drawCircle(Sol, radius = r, center = c)
            for (i in 0 until 8) {
                val angulo = Math.toRadians(i * 45.0)
                val cs = cos(angulo).toFloat()
                val sn = sin(angulo).toFloat()
                drawLine(
                    color = Sol,
                    start = Offset(c.x + r * 1.5f * cs, c.y + r * 1.5f * sn),
                    end = Offset(c.x + r * 2.3f * cs, c.y + r * 2.3f * sn),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
        Text("SOLAREVAL", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Azul)
    }
}

@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cargando: Boolean = false,
    habilitado: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = habilitado && !cargando,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Sol, contentColor = Azul,
            disabledContainerColor = Sol.copy(alpha = 0.45f), disabledContentColor = Azul.copy(alpha = 0.6f),
        ),
    ) {
        if (cargando) CircularProgressIndicator(Modifier.size(22.dp), color = Azul, strokeWidth = 2.dp)
        else Text(texto, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BotonSecundario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = habilitado,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(2.dp, if (habilitado) Azul else Borde),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Azul, disabledContentColor = Gris),
    ) {
        Text(texto, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

// Campo con la etiqueta visible arriba, como en el diseño de Figma. "ayuda" es un texto pequeño debajo.
@Composable
fun CampoTexto(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit,
    modifier: Modifier = Modifier,
    teclado: KeyboardType = KeyboardType.Text,
    esPassword: Boolean = false,
    ayuda: String? = null,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(etiqueta, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Azul)
        OutlinedTextField(
            value = valor,
            onValueChange = onCambio,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = etiqueta },
            visualTransformation = if (esPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (esPassword) KeyboardType.Password else teclado),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Azul, unfocusedBorderColor = Borde),
        )
        if (ayuda != null) Text(ayuda, fontSize = 13.sp, color = Gris)
    }
}

@Composable
fun SelectorOpciones(
    opciones: List<Pair<String, String>>,      // (valor, texto)
    seleccionado: String,
    onSeleccion: (String) -> Unit,
) {
    Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        opciones.forEach { (valor, texto) ->
            OpcionSeleccion(texto, seleccionado == valor, Modifier.weight(1f)) { onSeleccion(valor) }
        }
    }
}

@Composable
fun SelectorRol(seleccionado: String, onSeleccion: (String) -> Unit) =
    SelectorOpciones(listOf("pyme" to "Soy PyME", "instalador" to "Soy instalador"), seleccionado, onSeleccion)

@Composable
fun SelectorPeriodo(seleccionado: String, onSeleccion: (String) -> Unit) =
    SelectorOpciones(listOf("mensual" to "Mensual", "bimestral" to "Bimestral"), seleccionado, onSeleccion)

@Composable
private fun OpcionSeleccion(texto: String, activo: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val forma = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(forma)
            .background(if (activo) Azul else Color.White)
            .border(1.5.dp, if (activo) Azul else Borde, forma)
            .selectable(selected = activo, onClick = onClick, role = Role.RadioButton)
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(texto, color = if (activo) Color.White else Azul, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

// Menú inferior del prototipo: Inicio, Historial, Salir.
@Composable
fun MenuInferior(activo: String, onInicio: () -> Unit, onHistorial: () -> Unit, onSalir: () -> Unit) {
    Column {
        HorizontalDivider(thickness = 1.5.dp, color = Borde)
        Row(Modifier.fillMaxWidth()) {
            ItemMenu("Inicio", activo == "inicio", Modifier.weight(1f), onInicio)
            ItemMenu("Historial", activo == "historial", Modifier.weight(1f), onHistorial)
            ItemMenu("Salir", false, Modifier.weight(1f), onSalir)
        }
    }
}

@Composable
private fun ItemMenu(texto: String, activo: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .heightIn(min = 52.dp)
            .clickable(onClick = onClick, role = Role.Tab)
            .drawBehind {
                if (activo) drawLine(Sol, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 3.dp.toPx())
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            texto,
            fontSize = 15.sp,
            fontWeight = if (activo) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (activo) Azul else Gris,
        )
    }
}
