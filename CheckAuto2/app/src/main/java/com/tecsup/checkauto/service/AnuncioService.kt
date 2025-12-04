package com.tecsup.checkauto.service

import com.tecsup.checkauto.api.AnuncioApi
import com.tecsup.checkauto.config.ApiConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder

class AnuncioService {
    private val retrofit: Retrofit
    private val api: AnuncioApi
    
    init {
        val gson: Gson = GsonBuilder()
            .setLenient()
            .create()
        
        retrofit = Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        
        api = retrofit.create(AnuncioApi::class.java)
    }
    
    /**
     * Eliminar un anuncio usando el backend de Spring Boot
     * @param id ID del anuncio a eliminar
     * @param accessToken Token de acceso de Supabase (JWT)
     */
    suspend fun deleteAnuncio(id: Long, accessToken: String): Result<Unit> {
        return try {
            // El backend espera el token en formato "Bearer {token}"
            val authHeader = if (accessToken.startsWith("Bearer ")) {
                accessToken
            } else {
                "Bearer $accessToken"
            }
            
            android.util.Log.d("AnuncioService", "Eliminando anuncio $id con token: ${authHeader.take(20)}...")
            
            val response = api.deleteAnuncio(id, authHeader)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    android.util.Log.d("AnuncioService", "Anuncio $id eliminado exitosamente")
                    Result.success(Unit)
                } else {
                    val errorMsg = body?.error ?: body?.message ?: "Error desconocido"
                    android.util.Log.e("AnuncioService", "Error al eliminar: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error HTTP ${response.code()}"
                android.util.Log.e("AnuncioService", "Error HTTP ${response.code()}: $errorBody")
                Result.failure(Exception("Error ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            android.util.Log.e("AnuncioService", "Excepción al eliminar anuncio: ${e.message}", e)
            Result.failure(e)
        }
    }
}

