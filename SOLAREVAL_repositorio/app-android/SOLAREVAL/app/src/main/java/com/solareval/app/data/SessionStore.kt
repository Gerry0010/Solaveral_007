package com.solareval.app.data

import android.content.Context

// Guarda el token y los datos de la sesión en el teléfono (SharedPreferences).
class SessionStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("sesion", Context.MODE_PRIVATE)

    val token: String? get() = prefs.getString("token", null)
    val rol: String? get() = prefs.getString("rol", null)
    val nombre: String get() = prefs.getString("nombre", "") ?: ""

    fun guardar(token: String, rol: String, nombre: String) {
        prefs.edit().putString("token", token).putString("rol", rol).putString("nombre", nombre).apply()
    }

    fun limpiar() {
        prefs.edit().clear().apply()
    }
}
