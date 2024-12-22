package com.iso.data.network.datasource

import com.iso.data.network.Api
import com.iso.data.product.mapper.Mapper
import com.iso.domain.ui.TimesRepository
import com.iso.domain.ui.model.RestaurantModel
import javax.inject.Inject


class TimesServerRepository @Inject constructor(private val api: Api, private val mapper: Mapper) :
    TimesRepository {

    override suspend fun getTimes(): Result<RestaurantModel> {
        return try {
            val response = api.fetchTimeData()

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null) {
                    Result.success(mapper.mapRestDtoToDomain(body))
                } else {
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                Result.failure(Exception("Error: ${response.code()} ${response.message()}")) // Ошибка HTTP
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}