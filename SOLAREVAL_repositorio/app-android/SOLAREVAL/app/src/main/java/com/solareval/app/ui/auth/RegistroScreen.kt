package com.solareval.app.ui.auth

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
import com.solareval.app.ui.componentes.CampoTexto
import com.solareval.app.ui.componentes.SelectorRol
import com.solareval.app.ui.tema.Azul
import com.solareval.app.ui.tema.ErrorRojo
import com.solareval.app.ui.tema.Gris

@Composable
fun RegistroScreen(vm: AuthViewModel, onVolver: () -> Unit, onExito: (String) -> Unit) {
    var rol by remember { mutableStateOf("pyme") }
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var dato1 by remember { mutableStateOf("") }
    var dato2 by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { vm.limpiarError() }

    val esPyme = rol == "pyme"

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
        Text("Crea tu cuenta", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Azul)
        Text("Elige tu rol y completa tus datos.", fontSize = 15.sp, color = Gris)
        SelectorRol(rol) { rol = it; dato1 = ""; dato2 = "" }
        CampoTexto("Nombre", nombre, { nombre = it })
        CampoTexto("Correo", correo, { correo = it }, teclado = KeyboardType.Email)
        CampoTexto("Contraseña (mínimo 8 caracteres)", password, { password = it }, esPassword = true)
        CampoTexto(if (esPyme) "Razón social" else "Empresa", dato1, { dato1 = it })
        CampoTexto(if (esPyme) "Giro del negocio" else "Zona de cobertura", dato2, { dato2 = it })
        vm.error?.let { Text(it, color = ErrorRojo, fontSize = 14.sp) }
        BotonPrincipal(
            "Crear cuenta",
            { vm.registrar(rol, nombre, correo, password, dato1, dato2, onExito) },
            cargando = vm.cargando,
        )
    }
}
