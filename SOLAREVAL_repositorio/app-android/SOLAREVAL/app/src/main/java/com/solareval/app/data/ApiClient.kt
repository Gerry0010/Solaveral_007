package com.solareval.app.data

import com.solareval.app.Config
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    fun crear(sesion: SessionStore): SolarevalApi {
        // Agrega "Authorization: Bearer <token>" a cada petición cuando hay sesión.
        val auth = Interceptor { chain ->
            val token = sesion.token
            val peticion = if (token != null) {
                chain.request().newBuilder().header("Authorization", "Bearer $token").build()
            } else {
                chain.request()
            }
            chain.proceed(peticion)
        }
        // BASIC no imprime el cuerpo, así que las contraseñas no salen en el Logcat.
        val log = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }

        val cliente = OkHttpClient.Builder()
            .addInterceptor(auth)
            .addInterceptor(log)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(Config.BASE_URL)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SolarevalApi::class.java)
    }
}
