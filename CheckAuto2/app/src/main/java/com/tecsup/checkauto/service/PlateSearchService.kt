package com.tecsup.checkauto.service

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
    
    suspend fun searchPlate(plateNumber: String, userId: String): Result<Vehiculo> {
        return try {
            val request = PlateSearchRequest(
                plateNumber = plateNumber.uppercase().replace("-", "").replace(" ", ""),
                userId = userId
            )
            
            val response = api.searchPlate(request)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.vehicle != null) {
                    Result.success(body.vehicle.toVehiculo())
                } else {
                    Result.failure(Exception(body?.error ?: body?.message ?: "Error desconocido"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error HTTP ${response.code()}"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

