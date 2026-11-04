package com.solareval.app.data

import com.google.gson.JsonParser
import retrofit2.HttpException
import java.io.IOException

// Intermediario entre las pantallas y el backend: convierte los errores en mensajes en español.
class Repositorio(private val sesion: SessionStore) {
    private val api = ApiClient.crear(sesion)

    suspend fun login(correo: String, password: String): Result<LoginResponse> =
        llamar { api.login(LoginRequest(correo.trim().lowercase(), password)) }

    suspend fun registrar(datos: RegistroRequest): Result<RegistroResponse> =
        llamar { api.registro(datos) }

    suspend fun historial(): Result<List<ReporteResumen>> = llamar { api.historial() }

    suspend fun crearReporte(datos: ReporteRequest): Result<ReporteCreado> =
        llamar { api.crearReporte(datos) }

    suspend fun instaladores(): Result<List<InstaladorItem>> = llamar { api.instaladores() }

    suspend fun crearSolicitudes(datos: SolicitudRequest): Result<SolicitudesCreadas> =
        llamar { api.crearSolicitudes(datos) }

    suspend fun bandeja(): Result<List<SolicitudItem>> = llamar { api.bandeja() }

    suspend fun detalleSolicitud(id: Int): Result<SolicitudDetalle> = llamar { api.detalleSolicitud(id) }

    suspend fun cambiarEstado(id: Int, estado: String): Result<SolicitudDetalle> =
        llamar { api.cambiarEstado(id, EstadoRequest(estado)) }

    fun guardarSesion(r: LoginResponse) = sesion.guardar(r.accessToken, r.rol, r.nombre)

    private suspend fun <T> llamar(bloque: suspend () -> T): Result<T> =
        try {
            Result.success(bloque())
        } catch (e: HttpException) {
            Result.failure(Exception(mensajeDeError(e)))
        } catch (e: IOException) {
            Result.failure(Exception("No se pudo conectar: ${e.message}"))
        }

    // El backend responde {"detail": "mensaje"}; en errores de validación "detail" es una lista.
    private fun mensajeDeError(e: HttpException): String {
        val crudo = e.response()?.errorBody()?.string() ?: return "Error ${e.code()}"
        return try {
            val detalle = JsonParser.parseString(crudo).asJsonObject.get("detail")
            if (detalle != null && detalle.isJsonPrimitive) detalle.asString
            else "Revisa los datos e inténtalo de nuevo"
        } catch (ex: Exception) {
            "Error ${e.code()}"
        }
    }
}
