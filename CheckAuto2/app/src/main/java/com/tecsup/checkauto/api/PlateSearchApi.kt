package com.tecsup.checkauto.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PlateSearchApi {
    @POST("plate-search")
    suspend fun searchPlate(@Body request: PlateSearchRequest): Response<PlateSearchResponse>
}

