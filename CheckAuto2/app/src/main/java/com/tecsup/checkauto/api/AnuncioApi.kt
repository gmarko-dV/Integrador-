package com.tecsup.checkauto.api

import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface AnuncioApi {
    @DELETE("anuncios/{id}")
    suspend fun deleteAnuncio(
        @Path("id") id: Long,
        @Header("Authorization") token: String
    ): Response<AnuncioResponse>
    
    @GET("anuncios/mis-anuncios")
    suspend fun getMisAnuncios(
        @Header("Authorization") token: String
    ): Response<MisAnunciosResponse>
}

data class AnuncioResponse(
    val success: Boolean,
    val message: String?,
    val error: String?
)

data class MisAnunciosResponse(
    val success: Boolean,
    val anuncios: List<Any>?,
    val error: String?
)

