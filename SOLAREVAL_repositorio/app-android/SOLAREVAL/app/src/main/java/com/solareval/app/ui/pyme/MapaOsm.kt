package com.solareval.app.ui.pyme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.io.File

/**
 * Mapa de OpenStreetMap (no necesita llave). El "pin" es fijo al centro de la pantalla:
 * la PyME mueve el mapa hasta que el centro quede sobre su negocio.
 */
@Composable
fun MapaOsm(
    latInicial: Double,
    lonInicial: Double,
    zoomInicial: Double,
    destinoLat: Double,
    destinoLon: Double,
    versionDestino: Int,
    onCambio: (lat: Double, lon: Double, zoom: Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val alCambiar by rememberUpdatedState(onCambio)

    val mapa = remember {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            osmdroidBasePath = File(context.cacheDir, "osmdroid")
            osmdroidTileCache = File(context.cacheDir, "osmdroid/tiles")
        }
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            minZoomLevel = 3.0
            maxZoomLevel = 20.0
            controller.setZoom(zoomInicial)
            controller.setCenter(GeoPoint(latInicial, lonInicial))
            addMapListener(object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean {
                    alCambiar(mapCenter.latitude, mapCenter.longitude, zoomLevelDouble)
                    return false
                }

                override fun onZoom(event: ZoomEvent?): Boolean {
                    alCambiar(mapCenter.latitude, mapCenter.longitude, zoomLevelDouble)
                    return false
                }
            })
        }
    }

    // Cuando se pide mover el mapa (GPS o búsqueda de dirección)
    LaunchedEffect(versionDestino) {
        if (versionDestino > 0) {
            mapa.controller.setZoom(17.0)
            mapa.controller.setCenter(GeoPoint(destinoLat, destinoLon))
            alCambiar(destinoLat, destinoLon, 17.0)
        }
    }

    // El mapa necesita avisarle a osmdroid cuando la pantalla se pausa o se reanuda
    val dueno = context as? LifecycleOwner
    DisposableEffect(dueno, mapa) {
        val observador = LifecycleEventObserver { _, evento ->
            when (evento) {
                Lifecycle.Event.ON_RESUME -> mapa.onResume()
                Lifecycle.Event.ON_PAUSE -> mapa.onPause()
                else -> Unit
            }
        }
        dueno?.lifecycle?.addObserver(observador)
        onDispose {
            dueno?.lifecycle?.removeObserver(observador)
            mapa.onDetach()
        }
    }

    AndroidView(factory = { mapa }, modifier = modifier)
}
