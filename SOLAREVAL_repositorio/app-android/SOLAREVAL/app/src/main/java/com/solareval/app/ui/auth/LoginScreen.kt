package com.solareval.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solareval.app.ui.componentes.BotonPrincipal
import com.solareval.app.ui.componentes.BotonSecundario
import com.solareval.app.ui.componentes.CampoTexto
import com.solareval.app.ui.componentes.LogoSolareval
import com.solareval.app.ui.componentes.SelectorRol
import com.solareval.app.ui.tema.Azul
import com.solareval.app.ui.tema.ErrorRojo
import com.solareval.app.ui.tema.Gris

@Composable
fun LoginScreen(vm: AuthViewModel, onRegistro: () -> Unit, onExito: (String) -> Unit) {
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rol by remember { mutableStateOf("pyme") }
    LaunchedEffect(Unit) { vm.limpiarError() }

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
        LogoSolareval()
        Spacer(Modifier.height(8.dp))
        Text(
            "Sabe si te conviene instalar paneles solares",
            fontSize = 26.sp, lineHeight = 31.sp, fontWeight = FontWeight.Bold, color = Azul,
        )
        Text(
            "Gratis para tu negocio. Sin visita técnica: solo tu recibo de luz y tu ubicación.",
            fontSize = 15.sp, color = Gris,
        )
        CampoTexto("Correo", correo, { correo = it }, teclado = KeyboardType.Email)
        CampoTexto("Contraseña", password, { password = it }, esPassword = true)
        SelectorRol(rol) { rol = it }
        vm.error?.let { Text(it, color = ErrorRojo, fontSize = 14.sp) }
        BotonPrincipal("Iniciar sesión", { vm.login(correo, password, rol, onExito) }, cargando = vm.cargando)
        BotonSecundario("Crear cuenta", onRegistro)
    }
}
