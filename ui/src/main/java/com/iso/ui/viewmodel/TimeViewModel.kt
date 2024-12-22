package com.iso.ui.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iso.domain.ui.model.RestaurantTimeModel
import com.iso.domain.ui.usecase.TimeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class TimeViewModel @Inject constructor(private val useCase: TimeUseCase) : ViewModel() {

    private val _location = MutableStateFlow<String?>(null)
    val locationName: StateFlow<String?> get() = _location

    private val _data = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val data: StateFlow<List<Map<String, Any>>> get() = _data


    @RequiresApi(Build.VERSION_CODES.O)
    fun getTime() = viewModelScope.launch {
        viewModelScope.launch {
            val data = useCase.invoke()

            data.fold(onSuccess = { value ->
                _location.value = value.locationName

                val timeResult = timeCreate(value.openHours)
                _data.value = timeResult

            }, onFailure = { error ->

                Log.e("Error:", "${error.message}")
            })


        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun timeCreate(workingHours: List<RestaurantTimeModel>): List<Map<String, Any>> {

        // Sort days of week
        val dayOrder =
            listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

        // short and full name of week
        val fullDayNames = mapOf(
            "MON" to "Monday",
            "TUE" to "Tuesday",
            "WED" to "Wednesday",
            "THU" to "Thursday",
            "FRI" to "Friday",
            "SAT" to "Saturday",
            "SUN" to "Sunday"
        )

        // Converted short to full name of week
        val updatedList: List<RestaurantTimeModel> = workingHours.map {
            it.copy(dayOfWeek = fullDayNames[it.dayOfWeek] ?: it.dayOfWeek)
        }


        val fullNameList = updatedList.map {
            it.copy(
                startTime = it.startTime.toFormattedTime(), endTime = it.endTime.toFormattedTime()
            )
        }

        val sortedList = addClosedDays(fullNameList, dayOrder)

        // Group by day of week
        val mergedList = mergeWorkHours(sortedList)

        // Processing data
        val data = mergeIntervalsWithCarryover(mergedList)

        return data
    }


    private fun mergeWorkHours(workHoursList: List<RestaurantTimeModel>): List<Map<String, Any>> {
        return workHoursList.groupBy { it.dayOfWeek }  // Group by day of week
            .map { (day, entries) ->
                mapOf("day_of_week" to day,
                    "intervals" to entries.map { "${it.startTime} - ${it.endTime}" })
            }
    }

    fun mergeIntervalsWithCarryover(workHoursList: List<Map<String, Any>>): List<Map<String, Any>> {
        val result = mutableListOf<Map<String, Any>>()
        var previousDay: Map<String, Any>? = null

        for (currentDay in workHoursList) {
            val currentDayIntervals = currentDay["intervals"] as List<String>
            val updatedIntervals = mutableListOf<String>()

            for (interval in currentDayIntervals) {
                if (interval == "12am - 12am" || isSameTimeInterval(interval)) {
                    // If interval is 12am - 12am or interval between time is same than it means "open 24 hours"
                    updatedIntervals.add("Open 24hrs")
                } else if (interval.startsWith("12am")) {
                    // If interval starts from 12am, I move it to the end of the previous day
                    previousDay?.let {
                        val lastInterval = it["intervals"] as List<String>
                        val updatedLastInterval =
                            lastInterval.last().replace("12am", interval.split(" - ")[1])
                        val newIntervals = lastInterval.dropLast(1) + updatedLastInterval
                        result[result.lastIndex] =
                            it.toMutableMap().apply { put("intervals", newIntervals) }
                    }
                } else if (interval.startsWith("closed")) {
                    updatedIntervals.add("Closed")
                } else {
                    // Add interval to current day
                    updatedIntervals.add(interval)
                }
            }

            if (updatedIntervals.isNotEmpty()) {
                val newDay = currentDay.toMutableMap().apply {
                    put("intervals", updatedIntervals)
                }
                result.add(newDay)
                previousDay = newDay
            }
        }
        return result
    }


    private fun isSameTimeInterval(interval: String): Boolean {
        val times = interval.split(" - ")
        return times[0] == times[1]
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun String.toFormattedTime(
    ): String {
        // Formatter for time conversion
        val inputTimeFormatter = DateTimeFormatter.ofPattern("H:mm:ss")  // from 1:00:00
        val outputTimeFormatter = DateTimeFormatter.ofPattern("ha") // to 1am
        val localTime = LocalTime.parse(this, inputTimeFormatter)
        return localTime.format(outputTimeFormatter).lowercase() // transform to "am"/"pm"
    }

    private fun addClosedDays(
        updatedList: List<RestaurantTimeModel>, dayOrder: List<String>
    ): List<RestaurantTimeModel> {
        val existingDays = updatedList.map { it.dayOfWeek }
        val missingDays = dayOrder.filterNot { it in existingDays }

        // Add closed days
        val fullList = updatedList + missingDays.map { day ->
            RestaurantTimeModel(dayOfWeek = day, startTime = "closed", endTime = "")
        }

        // Full Sorted days of week
        return fullList.sortedBy { dayOrder.indexOf(it.dayOfWeek) }
    }
}


//                val work = listOf(
//                    RestaurantTimeModel(dayOfWeek = "WED", startTime = "07:00:00", endTime = "13:00:00"),
//                    RestaurantTimeModel(dayOfWeek = "WED", startTime = "00:00:00", endTime = "02:00:00"),
//                    RestaurantTimeModel(dayOfWeek = "WED", startTime = "15:00:00", endTime = "22:00:00"),
//                    RestaurantTimeModel(dayOfWeek = "TUE", startTime = "07:00:00", endTime = "13:00:00"),
//                    RestaurantTimeModel(dayOfWeek = "TUE", startTime = "15:00:00", endTime = "24:00:00"),
//                    RestaurantTimeModel(dayOfWeek = "THU", startTime = "00:00:00", endTime = "24:00:00"),
//                    RestaurantTimeModel(dayOfWeek = "SAT", startTime = "01:00:00", endTime = "06:00:00")
//                )
//
//                val timeResult = timeCreate(work)