package com.solareval.app.ui

fun textoRecomendacion(codigo: String): String = when (codigo) {
    "conviene_ahora" -> "Conviene ahora"
    "conviene_con_condiciones" -> "Conviene con condiciones"
    "no_conviene_todavia" -> "No conviene todavía"
    else -> codigo
}

fun textoEstadoSolicitud(estado: String?): String = when (estado) {
    "pendiente" -> "Contacto pendiente"
    "aceptada" -> "Contacto aceptado"
    "rechazada" -> "Contacto rechazado"
    else -> "Sin solicitud"
}

// "2026-11-01T15:24:12" -> "01/11/2026"
fun fechaCorta(iso: String?): String {
    val partes = iso?.take(10)?.split("-") ?: return ""
    return if (partes.size == 3) "${partes[2]}/${partes[1]}/${partes[0]}" else ""
}
