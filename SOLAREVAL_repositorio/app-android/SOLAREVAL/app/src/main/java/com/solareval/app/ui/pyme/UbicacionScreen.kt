package com.solareval.app.ui.pyme

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.solareval.app.ui.componentes.BotonPrincipal
import com.solareval.app.ui.componentes.BotonSecundario
import com.solareval.app.ui.componentes.CampoTexto
import com.solareval.app.ui.tema.Azul
import com.solareval.app.ui.tema.Borde
import com.solareval.app.ui.tema.ErrorRojo
import com.solareval.app.ui.tema.Gris
import com.solareval.app.ui.tema.Sol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun UbicacionScreen(vm: ReporteViewModel, onVolver: () -> Unit, onGenerar: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mensaje by remember { mutableStateOf<String?>(null) }
    var buscando by remember { mutableStateOf(false) }

    val permisos = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        if (r.values.any { it }) {
            pedirUbicacion(context, vm) { mensaje = it }
        } else {
            mensaje = "Sin permiso de ubicación. Mueve el mapa o escribe la dirección."
        }
    }

    val listo = vm.zoom >= ReporteViewModel.ZOOM_MINIMO_PARA_GENERAR

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
        Text("PASO 3 DE 4", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Gris)
        Text("¿Dónde está tu negocio?", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Azul)
        Text("Mueve el mapa hasta que el pin quede sobre el techo de tu negocio.", fontSize = 15.sp, color = Gris)

        Box(
            Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.5.dp, Borde, RoundedCornerShape(16.dp)),
        ) {
            MapaOsm(
                latInicial = vm.latitud, lonInicial = vm.longitud, zoomInicial = vm.zoom,
                destinoLat = vm.destinoLat, destinoLon = vm.destinoLon, versionDestino = vm.versionDestino,
                onCambio = { lat, lon, zoom -> vm.latitud = lat; vm.longitud = lon; vm.zoom = zoom },
                modifier = Modifier.fillMaxSize(),
            )
            PinCentral(Modifier.align(Alignment.Center).offset(y = (-27).dp))
        }

        CampoTexto("Dirección o referencia", vm.direccion, { vm.direccion = it })
        TextButton(
            onClick = {
                buscando = true
                mensaje = null
                scope.launch {
                    val encontrado = buscarDireccion(context, vm.direccion)
                    buscando = false
                    if (encontrado != null) vm.centrarEn(encontrado.first, encontrado.second)
                    else mensaje = "No encontramos esa dirección. Escríbela con calle, colonia y ciudad, o mueve el mapa."
                }
            },
            enabled = vm.direccion.isNotBlank() && !buscando,
        ) { Text(if (buscando) "Buscando..." else "Buscar esta dirección en el mapa", color = Azul, fontWeight = FontWeight.Bold) }

        BotonSecundario("Usar mi ubicación actual", {
            mensaje = null
            permisos.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        })

        if (!listo) {
            Text("Acerca el mapa hasta ver tu negocio para poder generar el reporte.", fontSize = 14.sp, color = Gris)
        }
        mensaje?.let { Text(it, color = ErrorRojo, fontSize = 14.sp) }

        BotonPrincipal("Generar reporte", onGenerar, habilitado = listo)
    }
}

@Composable
private fun PinCentral(modifier: Modifier) {
    Canvas(modifier.size(44.dp, 54.dp).semantics { contentDescription = "Ubicación del negocio" }) {
        val w = size.width
        val h = size.height
        val radio = w / 2
        val punta = Path().apply {
            moveTo(w * 0.12f, radio * 1.45f)
            lineTo(w / 2, h)
            lineTo(w * 0.88f, radio * 1.45f)
            close()
        }
        drawPath(punta, Azul)
        drawCircle(Azul, radius = radio, center = Offset(w / 2, radio))
        drawCircle(Sol, radius = w * 0.18f, center = Offset(w / 2, radio))
    }
}

@SuppressLint("MissingPermission")
private fun pedirUbicacion(context: Context, vm: ReporteViewModel, onError: (String) -> Unit) {
    LocationServices.getFusedLocationProviderClient(context)
        .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
        .addOnSuccessListener { ubicacion ->
            if (ubicacion != null) vm.centrarEn(ubicacion.latitude, ubicacion.longitude)
            else onError("No se pudo obtener tu ubicación. Enciende el GPS e inténtalo de nuevo.")
        }
        .addOnFailureListener { onError("No se pudo obtener tu ubicación. Mueve el mapa o escribe la dirección.") }
}

private suspend fun buscarDireccion(context: Context, texto: String): Pair<Double, Double>? =
    withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) return@withContext null
        try {
            @Suppress("DEPRECATION")
            Geocoder(context, Locale("es", "MX")).getFromLocationName(texto, 1)
                ?.firstOrNull()?.let { it.latitude to it.longitude }
        } catch (e: Exception) {
            null
        }
    }
