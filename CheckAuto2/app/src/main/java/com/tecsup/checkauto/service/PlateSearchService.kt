package com.tecsup.checkauto.service

import android.util.Log
import com.tecsup.checkauto.api.PlateSearchApi
import com.tecsup.checkauto.api.PlateSearchRequest
import com.tecsup.checkauto.model.Vehiculo
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tecsup.checkauto.config.ApiConfig

class PlateSearchService {
    private val retrofit: Retrofit
    private val api: PlateSearchApi
    
    init {
        val gson: Gson = GsonBuilder()
            .setLenient()
            .create()
        
        retrofit = Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        
        api = retrofit.create(PlateSearchApi::class.java)
    }
    
    suspend fun searchPlate(plateNumber: String, userId: String?): Result<Vehiculo> {
        return try {
            val placaLimpia = plateNumber.uppercase().replace("-", "").replace(" ", "")
            Log.d("PlateSearchService", "Buscando placa en backend: $placaLimpia, userId: ${userId ?: "null"}")
            Log.d("PlateSearchService", "URL base: ${ApiConfig.BASE_URL}")
            
            val request = PlateSearchRequest(
                plateNumber = placaLimpia,
                userId = userId ?: ""
            )
            
            Log.d("PlateSearchService", "Enviando request: plateNumber=${request.plateNumber}, userId=${request.userId}")
            
            val response = api.searchPlate(request)
            
            Log.d("PlateSearchService", "Response code: ${response.code()}")
            Log.d("PlateSearchService", "Response isSuccessful: ${response.isSuccessful}")
            
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("PlateSearchService", "Response body: success=${body?.success}, vehicle=${body?.vehicle != null}")
                
                if (body != null && body.success && body.vehicle != null) {
                    Log.d("PlateSearchService", "✅ Vehículo encontrado: ${body.vehicle.placa}")
                    Result.success(body.vehicle.toVehiculo())
                } else {
                    val errorMsg = body?.error ?: body?.message ?: "Error desconocido"
                    Log.e("PlateSearchService", "❌ Error en respuesta: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Error HTTP ${response.code()}"
                } catch (e: Exception) {
                    "Error HTTP ${response.code()}: ${e.message}"
                }
                Log.e("PlateSearchService", "❌ Error HTTP ${response.code()}: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e("PlateSearchService", "❌ Excepción al buscar placa: ${e.message}", e)
            Result.failure(e)
        }
    }
}

