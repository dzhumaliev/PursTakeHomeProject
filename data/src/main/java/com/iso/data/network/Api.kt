package com.iso.data.network

import com.iso.data.product.model.RestaurantModelDto
import retrofit2.Response
import retrofit2.http.GET

interface Api {

    @GET("location.json")
    suspend fun fetchTimeData(): Response<RestaurantModelDto>

}