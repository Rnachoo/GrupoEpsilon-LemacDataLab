package com.example.lemacdatalab.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @GET("consentimiento")
    suspend fun obtenerConsentimiento(
        @Query("identificador") identificador: String? = null
    ): ConsentimientoDto

    @POST("consentimiento/aceptacion")
    suspend fun aceptarConsentimiento(
        @Body request: AceptacionConsentimientoRequestDto
    ): ConsentimientoDto

    @GET("registros")
    suspend fun obtenerRegistros(
        @Query("idUsuario") idUsuario: Int? = null
    ): List<RegistroDto>

    @POST("registros")
    suspend fun crearRegistro(@Body registro: RegistroDto): RegistroDto

    @PUT("registros/{id}")
    suspend fun actualizarRegistro(
        @Path("id") idRegistro: Int,
        @Body registro: RegistroDto
    ): RegistroDto

    @DELETE("registros/{id}")
    suspend fun eliminarRegistro(
        @Path("id") idRegistro: Int,
        @Query("modificadoEnUtc") modificadoEnUtc: Long = System.currentTimeMillis()
    ): RegistroDto

    @GET("cambios")
    suspend fun obtenerCambios(
        @Query("desde") desdeUtc: Long,
        @Query("idUsuario") idUsuario: Int? = null
    ): List<RegistroDto>

    @GET("catalogos")
    suspend fun obtenerCatalogos(): CatalogosDto

    @GET("perfil")
    suspend fun obtenerPerfil(
        @Query("idUsuario") idUsuario: Int? = null,
        @Query("identificador") identificador: String? = null
    ): PerfilDto
}
