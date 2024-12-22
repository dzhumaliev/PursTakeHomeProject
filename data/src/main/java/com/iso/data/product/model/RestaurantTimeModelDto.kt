package com.iso.data.product.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RestaurantTimeModelDto(

    @SerialName("day_of_week")
    val day_of_week: String,
    @SerialName("start_local_time")
    val start_local_time: String,
    @SerialName("end_local_time")
    val end_local_time: String

)