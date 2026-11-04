package com.solareval.app.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface SolarevalApi {

    @POST("auth/registro")
    suspend fun registro(@Body body: RegistroRequest): RegistroResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("reportes")
    suspend fun historial(): List<ReporteResumen>

    @POST("reportes")
    suspend fun crearReporte(@Body body: ReporteRequest): ReporteCreado

    @GET("instaladores")
    suspend fun instaladores(): List<InstaladorItem>

    @POST("solicitudes")
    suspend fun crearSolicitudes(@Body body: SolicitudRequest): SolicitudesCreadas

    @GET("solicitudes")
    suspend fun bandeja(): List<SolicitudItem>

    @GET("solicitudes/{id}")
    suspend fun detalleSolicitud(@Path("id") id: Int): SolicitudDetalle

    @PATCH("solicitudes/{id}/estado")
    suspend fun cambiarEstado(@Path("id") id: Int, @Body body: EstadoRequest): SolicitudDetalle
}
