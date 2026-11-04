package com.solareval.app.ui.pyme

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.solareval.app.ui.componentes.BotonPrincipal
import com.solareval.app.ui.componentes.BotonSecundario
import com.solareval.app.ui.tema.Azul
import com.solareval.app.ui.tema.ErrorRojo
import com.solareval.app.ui.tema.Fondo
import com.solareval.app.ui.tema.Gris
import java.io.File

@Composable
fun ReciboScreen(
    vm: ReciboViewModel,
    onVolver: () -> Unit,
    onAviso: () -> Unit,
    onManual: () -> Unit,
    onListo: () -> Unit,
) {
    val context = LocalContext.current
    var acepto by remember { mutableStateOf(false) }
    var uriFoto by remember { mutableStateOf<Uri?>(null) }
    var archivoFoto by remember { mutableStateOf<File?>(null) }

    val tomarFoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { tomada ->
        val uri = uriFoto
        if (tomada && uri != null) vm.leerImagen(context, uri, archivoFoto, onListo)
        else archivoFoto?.delete()
    }
    val elegirDeGaleria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.leerImagen(context, uri, null, onListo)
    }

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
        TextButton(onClick = onVolver) { Text("← Volver", color = Azul, fontWeight = FontWeight.SemiBold) }
        Text("PASO 1 DE 4", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Gris)
        Text("Foto de tu recibo de luz", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Azul)
        Text("Encuadra el recibo completo, con buena luz y sin sombras.", fontSize = 15.sp, color = Gris)

        // Recuadro punteado, como en el diseño
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Fondo)
                .drawBehind {
                    drawRoundRect(
                        color = Azul,
                        cornerRadius = CornerRadius(16.dp.toPx()),
                        style = Stroke(width = 2.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 14f))),
                    )
                }
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (vm.procesando) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(color = Azul)
                    Text("Leyendo tu recibo...", fontSize = 15.sp, color = Azul)
                }
            } else {
                Text(
                    "Aquí se abrirá la cámara. La foto se lee en tu teléfono y no se guarda.",
                    fontSize = 15.sp, color = Gris, textAlign = TextAlign.Center,
                )
            }
        }

        Row(verticalAlignment = Alignment.Top) {
            Checkbox(
                checked = acepto,
                onCheckedChange = { acepto = it },
                colors = CheckboxDefaults.colors(checkedColor = Azul),
            )
            Text(
                "Acepto el aviso de privacidad y el uso de mi recibo y ubicación para generar el reporte.",
                fontSize = 14.sp, color = Azul, modifier = Modifier.padding(top = 12.dp),
            )
        }
        TextButton(onClick = onAviso) { Text("Leer el aviso de privacidad", color = Azul, fontWeight = FontWeight.Bold) }

        vm.error?.let { Text(it, color = ErrorRojo, fontSize = 14.sp) }

        BotonPrincipal(
            "Tomar foto",
            {
                val carpeta = File(context.cacheDir, "recibos").apply { mkdirs() }
                val archivo = File(carpeta, "recibo_${System.currentTimeMillis()}.jpg")
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
                archivoFoto = archivo
                uriFoto = uri
                tomarFoto.launch(uri)
            },
            habilitado = acepto && !vm.procesando,
        )
        BotonSecundario(
            "Elegir de la galería",
            { elegirDeGaleria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            habilitado = acepto && !vm.procesando,
        )
        TextButton(onClick = onManual, enabled = acepto && !vm.procesando, modifier = Modifier.fillMaxWidth()) {
            Text("Capturar los datos a mano", color = Azul, fontWeight = FontWeight.SemiBold)
        }
    }
}
