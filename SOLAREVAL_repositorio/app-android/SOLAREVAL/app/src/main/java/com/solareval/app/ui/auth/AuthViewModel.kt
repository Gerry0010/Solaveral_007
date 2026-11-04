package com.solareval.app.ui.auth

import android.app.Application
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.solareval.app.data.RegistroRequest
import com.solareval.app.data.Repositorio
import com.solareval.app.data.SessionStore
import kotlinx.coroutines.launch

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = Repositorio(SessionStore(app))

    var cargando by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun limpiarError() {
        error = null
    }

    fun login(correo: String, password: String, rolElegido: String, onExito: (String) -> Unit) {
        if (correo.isBlank() || password.isBlank()) {
            error = "Escribe tu correo y tu contraseña"
            return
        }
        viewModelScope.launch {
            cargando = true
            error = null
            repo.login(correo, password)
                .onSuccess { r ->
                    // El rol elegido en pantalla debe coincidir con el de la cuenta (el administrador entra siempre).
                    if (r.rol != "admin" && r.rol != rolElegido) {
                        error = if (r.rol == "instalador") "Esta cuenta es de instalador. Elige \"Soy instalador\"."
                        else "Esta cuenta es de PyME. Elige \"Soy PyME\"."
                    } else {
                        repo.guardarSesion(r)
                        onExito(r.rol)
                    }
                }
                .onFailure { error = it.message }
            cargando = false
        }
    }

    fun registrar(
        rol: String, nombre: String, correo: String, password: String,
        dato1: String, dato2: String,          // PyME: razón social y giro; instalador: empresa y zona
        onExito: (String) -> Unit,
    ) {
        val mensaje = when {
            nombre.trim().length < 2 -> "Escribe tu nombre"
            !Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches() -> "Escribe un correo válido"
            password.length < 8 -> "La contraseña debe tener al menos 8 caracteres"
            dato1.isBlank() || dato2.isBlank() ->
                if (rol == "pyme") "Escribe la razón social y el giro de tu negocio"
                else "Escribe tu empresa y tu zona de cobertura"
            else -> null
        }
        if (mensaje != null) {
            error = mensaje
            return
        }
        val datos = RegistroRequest(
            nombre = nombre.trim(), correo = correo.trim().lowercase(), password = password, rol = rol,
            razonSocial = if (rol == "pyme") dato1.trim() else null,
            giro = if (rol == "pyme") dato2.trim() else null,
            empresa = if (rol == "instalador") dato1.trim() else null,
            zonaCobertura = if (rol == "instalador") dato2.trim() else null,
        )
        viewModelScope.launch {
            cargando = true
            error = null
            // Después de crear la cuenta se inicia sesión automáticamente.
            repo.registrar(datos)
                .onSuccess {
                    repo.login(correo, password)
                        .onSuccess { r ->
                            repo.guardarSesion(r)
                            onExito(r.rol)
                        }
                        .onFailure { error = it.message }
                }
                .onFailure { error = it.message }
            cargando = false
        }
    }
}
