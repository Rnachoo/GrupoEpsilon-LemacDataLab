package com.example.lemacdatalab.data.remote

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.lemacdatalab.local/"

    @Volatile
    private var interceptorCompartido: BackendSimuladoInterceptor? = null

    @Volatile
    private var apiCompartida: ApiService? = null

    fun crearApiService(
        interceptor: BackendSimuladoInterceptor = BackendSimuladoInterceptor()
    ): ApiService {
        val okHttp = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun obtenerInterceptor(context: Context): BackendSimuladoInterceptor =
        interceptorCompartido ?: synchronized(this) {
            interceptorCompartido ?: BackendSimuladoInterceptor(
                almacen = AlmacenBackendSimulado.desdeContexto(context.applicationContext)
            ).also { interceptorCompartido = it }
        }

    fun obtenerApiService(context: Context): ApiService =
        apiCompartida ?: synchronized(this) {
            apiCompartida ?: crearApiService(obtenerInterceptor(context)).also {
                apiCompartida = it
            }
        }
}
