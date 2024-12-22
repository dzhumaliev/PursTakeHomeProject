package com.iso.data.product.model

import kotlinx.serialization.SerialName



data class RestaurantModelDto(

    @SerialName("location_name")
    val location_name: String,
    @SerialName("hours")
    val hours: List<RestaurantTimeModelDto>

)
