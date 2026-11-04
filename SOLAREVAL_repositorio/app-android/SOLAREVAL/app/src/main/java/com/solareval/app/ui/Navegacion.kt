package com.solareval.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.solareval.app.data.SessionStore
import com.solareval.app.ui.auth.AuthViewModel
import com.solareval.app.ui.auth.LoginScreen
import com.solareval.app.ui.auth.RegistroScreen
import com.solareval.app.ui.componentes.BotonSecundario
import com.solareval.app.ui.instalador.AceptadaScreen
import com.solareval.app.ui.instalador.BandejaScreen
import com.solareval.app.ui.instalador.BandejaViewModel
import com.solareval.app.ui.instalador.DetalleSolicitudScreen
import com.solareval.app.ui.pyme.AvisoScreen
import com.solareval.app.ui.pyme.ContactoScreen
import com.solareval.app.ui.pyme.ContactoViewModel
import com.solareval.app.ui.pyme.DatosScreen
import com.solareval.app.ui.pyme.EnviadaScreen
import com.solareval.app.ui.pyme.GenerandoScreen
import com.solareval.app.ui.pyme.HistorialScreen
import com.solareval.app.ui.pyme.InicioScreen
import com.solareval.app.ui.pyme.InicioViewModel
import com.solareval.app.ui.pyme.ReciboScreen
import com.solareval.app.ui.pyme.ReciboViewModel
import com.solareval.app.ui.pyme.ReporteScreen
import com.solareval.app.ui.pyme.ReporteViewModel
import com.solareval.app.ui.pyme.UbicacionScreen
import com.solareval.app.ui.pyme.aVista
import com.solareval.app.ui.tema.Azul
import com.solareval.app.ui.tema.Gris

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val INICIO_PYME = "inicio_pyme"
    const val RECIBO = "recibo"
    const val AVISO = "aviso"
    const val DATOS = "datos"
    const val UBICACION = "ubicacion"
    const val GENERANDO = "generando"
    const val REPORTE = "reporte"
    const val CONTACTO = "contacto"
    const val ENVIADA = "enviada"
    const val HISTORIAL = "historial"
    const val BANDEJA = "bandeja"
    const val DETALLE = "detalle"
    const val ACEPTADA = "aceptada"
    const val ADMIN = "admin"
}

// Cada rol entra a su propio flujo (ver "Flujo de pantallas" del prototipo).
fun rutaPorRol(rol: String?): String = when (rol) {
    "pyme" -> Rutas.INICIO_PYME
    "instalador" -> Rutas.BANDEJA
    "admin" -> Rutas.ADMIN
    else -> Rutas.LOGIN
}

@Composable
fun Navegacion(sesion: SessionStore) {
    val nav = rememberNavController()
    val authVm: AuthViewModel = viewModel()
    val inicioVm: InicioViewModel = viewModel()
    val reciboVm: ReciboViewModel = viewModel()
    val reporteVm: ReporteViewModel = viewModel()
    val contactoVm: ContactoViewModel = viewModel()
    val bandejaVm: BandejaViewModel = viewModel()

    val irAlInicio: (String) -> Unit = { rol ->
        nav.navigate(rutaPorRol(rol)) { popUpTo(Rutas.LOGIN) { inclusive = true } }
    }
    val salir: () -> Unit = {
        sesion.limpiar()
        reciboVm.reiniciar()
        reporteVm.reiniciar()
        nav.navigate(Rutas.LOGIN) { popUpTo(nav.graph.id) { inclusive = true } }
    }
    val volverAlInicioPyme: () -> Unit = {
        nav.navigate(Rutas.INICIO_PYME) { popUpTo(Rutas.INICIO_PYME) { inclusive = true } }
    }
    val volverALaBandeja: () -> Unit = {
        nav.navigate(Rutas.BANDEJA) { popUpTo(Rutas.BANDEJA) { inclusive = true } }
    }

    NavHost(
        navController = nav,
        startDestination = if (sesion.token != null) rutaPorRol(sesion.rol) else Rutas.LOGIN,
    ) {
        composable(Rutas.LOGIN) {
            LoginScreen(authVm, onRegistro = { nav.navigate(Rutas.REGISTRO) }, onExito = irAlInicio)
        }
        composable(Rutas.REGISTRO) {
            RegistroScreen(authVm, onVolver = { nav.popBackStack() }, onExito = irAlInicio)
        }

        // ---------------- Flujo de la PyME
        composable(Rutas.INICIO_PYME) {
            InicioScreen(
                vm = inicioVm,
                nombre = sesion.nombre,
                onEvaluar = { reciboVm.reiniciar(); reporteVm.reiniciar(); nav.navigate(Rutas.RECIBO) },
                onHistorial = { nav.navigate(Rutas.HISTORIAL) },
                onSalir = salir,
            )
        }
        composable(Rutas.RECIBO) {
            ReciboScreen(
                vm = reciboVm,
                onVolver = { nav.popBackStack() },
                onAviso = { nav.navigate(Rutas.AVISO) },
                onManual = { reciboVm.reiniciar(); nav.navigate(Rutas.DATOS) },
                onListo = { nav.navigate(Rutas.DATOS) },
            )
        }
        composable(Rutas.AVISO) { AvisoScreen(onEntendido = { nav.popBackStack() }) }
        composable(Rutas.DATOS) {
            DatosScreen(
                vm = reciboVm,
                onTomarOtra = { nav.popBackStack() },
                onConfirmar = { nav.navigate(Rutas.UBICACION) },
            )
        }
        composable(Rutas.UBICACION) {
            UbicacionScreen(
                vm = reporteVm,
                onVolver = { nav.popBackStack() },
                onGenerar = { reporteVm.reporte = null; nav.navigate(Rutas.GENERANDO) },
            )
        }
        composable(Rutas.GENERANDO) {
            GenerandoScreen(
                vm = reporteVm,
                recibo = reciboVm,
                onListo = { nav.navigate(Rutas.REPORTE) { popUpTo(Rutas.GENERANDO) { inclusive = true } } },
                onAjustarUbicacion = { nav.popBackStack() },
            )
        }
        composable(Rutas.REPORTE) {
            BackHandler(onBack = volverAlInicioPyme)
            ReporteScreen(
                reporte = reporteVm.reporte,
                onInicio = volverAlInicioPyme,
                onSolicitarContacto = { nav.navigate(Rutas.CONTACTO) },
            )
        }
        composable(Rutas.CONTACTO) {
            ContactoScreen(
                vm = contactoVm,
                reporteId = reporteVm.reporte?.id,
                onVolver = { nav.popBackStack() },
                onEnviada = { nav.navigate(Rutas.ENVIADA) { popUpTo(Rutas.CONTACTO) { inclusive = true } } },
            )
        }
        composable(Rutas.ENVIADA) {
            BackHandler(onBack = volverAlInicioPyme)
            EnviadaScreen(
                onHistorial = { nav.navigate(Rutas.HISTORIAL) },
                onInicio = volverAlInicioPyme,
            )
        }
        composable(Rutas.HISTORIAL) {
            HistorialScreen(
                vm = inicioVm,
                onReporte = { reporteVm.reporte = it.aVista(); nav.navigate(Rutas.REPORTE) },
                onInicio = volverAlInicioPyme,
                onSalir = salir,
            )
        }

        // ---------------- Flujo del instalador
        composable(Rutas.BANDEJA) {
            BandejaScreen(
                vm = bandejaVm,
                nombre = sesion.nombre,
                onAbrir = { id -> bandejaVm.idSeleccionado = id; nav.navigate(Rutas.DETALLE) },
                onSalir = salir,
            )
        }
        composable(Rutas.DETALLE) {
            DetalleSolicitudScreen(
                vm = bandejaVm,
                onVolver = { nav.popBackStack() },
                onAceptada = { nav.navigate(Rutas.ACEPTADA) { popUpTo(Rutas.DETALLE) { inclusive = true } } },
                onRechazada = { nav.popBackStack() },
            )
        }
        composable(Rutas.ACEPTADA) {
            BackHandler(onBack = volverALaBandeja)
            AceptadaScreen(vm = bandejaVm, onVolver = volverALaBandeja)
        }

        // ---------------- Administrador (deseable; pendiente)
        composable(Rutas.ADMIN) { Pendiente("Administración", sesion.nombre, salir) }
    }
}

@Composable
private fun Pendiente(titulo: String, nombre: String, onAccion: () -> Unit) {
    Column(
        Modifier.fillMaxSize().systemBarsPadding().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Hola, $nombre", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Azul)
        Text("$titulo (pantalla pendiente)", fontSize = 15.sp, color = Gris)
        BotonSecundario("Salir", onAccion)
    }
}
