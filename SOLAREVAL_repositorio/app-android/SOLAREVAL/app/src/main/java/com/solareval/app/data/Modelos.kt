package com.solareval.app.data

import com.google.gson.annotations.SerializedName

data class RegistroRequest(
    val nombre: String,
    val correo: String,
    val password: String,
    val rol: String,                                   // "pyme" o "instalador"
    @SerializedName("razon_social") val razonSocial: String? = null,
    val giro: String? = null,
    val empresa: String? = null,
    @SerializedName("zona_cobertura") val zonaCobertura: String? = null,
)

data class RegistroResponse(val id: Int, val nombre: String, val rol: String, val verificado: Boolean)

data class LoginRequest(val correo: String, val password: String)

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    val rol: String,
    val nombre: String,
)

// Una fila del historial (GET /reportes)
data class ReporteResumen(
    val id: Int,
    @SerializedName("creado_en") val creadoEn: String?,
    @SerializedName("potencia_kw") val potenciaKw: Double,
    @SerializedName("ahorro_estimado") val ahorroEstimado: Double,
    @SerializedName("inversion_referencial") val inversionReferencial: Double,
    @SerializedName("retorno_inversion") val retornoInversion: Double?,
    val recomendacion: String,
    @SerializedName("fuente_solar") val fuenteSolar: String,
    val direccion: String?,
    @SerializedName("estado_solicitud") val estadoSolicitud: String?,
)

data class ReporteRequest(
    @SerializedName("consumo_kwh") val consumoKwh: Double,
    val tarifa: String,
    val importe: Double,
    val periodo: String,
    val latitud: Double,
    val longitud: Double,
    val direccion: String?,
    val consentimiento: Boolean,
)

data class SupuestosReporte(
    @SerializedName("costo_por_kw") val costoPorKw: Double,
    val derate: Double,
)

data class ReporteCreado(
    val id: Int,
    val paneles: Int,
    @SerializedName("potencia_kw") val potenciaKw: Double,
    @SerializedName("precio_kwh") val precioKwh: Double,
    @SerializedName("fuente_solar") val fuenteSolar: String,
    val supuestos: SupuestosReporte?,
    val aviso: String?,
    @SerializedName("consumo_anual_kwh") val consumoAnualKwh: Double,
    @SerializedName("generacion_anual_kwh") val generacionAnualKwh: Double,
    val cobertura: Double,
    @SerializedName("ahorro_estimado") val ahorroEstimado: Double,
    @SerializedName("inversion_referencial") val inversionReferencial: Double,
    @SerializedName("retorno_inversion") val retornoInversion: Double?,
    val recomendacion: String,
)

// ---- Instaladores y solicitudes
data class InstaladorItem(
    val id: Int,
    val empresa: String?,
    @SerializedName("zona_cobertura") val zonaCobertura: String?,
)

data class SolicitudRequest(
    @SerializedName("reporte_id") val reporteId: Int,
    @SerializedName("instalador_ids") val instaladorIds: List<Int>,
    val consentimiento: Boolean,
)

data class SolicitudCreada(
    val id: Int,
    @SerializedName("instalador_id") val instaladorId: Int,
    val estado: String,
)

data class SolicitudesCreadas(val solicitudes: List<SolicitudCreada>)

// Una fila de la bandeja del instalador (GET /solicitudes)
data class SolicitudItem(
    val id: Int,
    val estado: String,
    @SerializedName("creado_en") val creadoEn: String?,
    val negocio: String?,
    val giro: String?,
    val direccion: String?,
    val recomendacion: String,
)

// Detalle de una solicitud (GET /solicitudes/{id}). El contacto solo viene si ya fue aceptada.
data class SolicitudDetalle(
    val id: Int,
    val estado: String,
    @SerializedName("creado_en") val creadoEn: String?,
    val negocio: String?,
    val giro: String?,
    val direccion: String?,
    @SerializedName("potencia_kw") val potenciaKw: Double,
    @SerializedName("ahorro_estimado") val ahorroEstimado: Double,
    @SerializedName("inversion_referencial") val inversionReferencial: Double,
    @SerializedName("retorno_inversion") val retornoInversion: Double?,
    val recomendacion: String,
    @SerializedName("contacto_nombre") val contactoNombre: String?,
    @SerializedName("contacto_correo") val contactoCorreo: String?,
)

data class EstadoRequest(val estado: String)   // "aceptada" o "rechazada"
