package com.iso.domain.ui

import com.iso.domain.ui.model.RestaurantModel

interface TimesRepository  {
    suspend fun getTimes(): Result<RestaurantModel>
    fun isNetworkAvailable(): Boolean
}