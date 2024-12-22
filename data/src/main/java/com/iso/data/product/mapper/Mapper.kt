package com.iso.data.product.mapper

import com.iso.data.product.model.RestaurantModelDto
import com.iso.data.product.model.RestaurantTimeModelDto
import com.iso.domain.ui.model.RestaurantModel
import com.iso.domain.ui.model.RestaurantTimeModel
import javax.inject.Inject


class Mapper @Inject constructor() {

    fun mapTimeDtoToDomain(dto: RestaurantTimeModelDto): RestaurantTimeModel {
        return RestaurantTimeModel(
            dayOfWeek = dto.day_of_week,
            startTime = dto.start_local_time,
            endTime = dto.end_local_time
        )

    }

    fun mapRestDtoToDomain(dto: RestaurantModelDto): RestaurantModel {
        return RestaurantModel(
            locationName = dto.location_name,
            openHours = dto.hours.map {
                mapTimeDtoToDomain(it)
            }
        )

    }
    }