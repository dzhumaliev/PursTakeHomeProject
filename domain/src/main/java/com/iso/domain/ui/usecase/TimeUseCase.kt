package com.iso.domain.ui.usecase

import com.iso.domain.ui.TimesRepository
import com.iso.domain.ui.model.RestaurantModel
import javax.inject.Inject

class TimeUseCase @Inject constructor(private val newsRepo: TimesRepository) {

    suspend operator fun invoke(): Result<RestaurantModel> =
        newsRepo.getTimes()

}