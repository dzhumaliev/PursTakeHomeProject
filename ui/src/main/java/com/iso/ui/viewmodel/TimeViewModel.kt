package com.iso.ui.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iso.domain.ui.model.RestaurantTimeModel
import com.iso.domain.ui.usecase.TimeUseCase
import com.iso.ui.constants.Constants.CLOSED
import com.iso.ui.constants.Constants.DAYS_OF_WEEK
import com.iso.ui.constants.Constants.DAY_OF_WEEK
import com.iso.ui.constants.Constants.END_TIME
import com.iso.ui.constants.Constants.FULL_DAY_NAMES
import com.iso.ui.constants.Constants.GREEN
import com.iso.ui.constants.Constants.INTERVALS
import com.iso.ui.constants.Constants.LONG_TIME_FORMAT
import com.iso.ui.constants.Constants.MIDNIGHT
import com.iso.ui.constants.Constants.NO_INTERNET
import com.iso.ui.constants.Constants.OPENS
import com.iso.ui.constants.Constants.OPEN_24_HOURS
import com.iso.ui.constants.Constants.OPEN_AGAIN
import com.iso.ui.constants.Constants.OPEN_UNTIL
import com.iso.ui.constants.Constants.RED
import com.iso.ui.constants.Constants.REOPEN
import com.iso.ui.constants.Constants.SHORT_TIME_FORMAT
import com.iso.ui.constants.Constants.START_TIME
import com.iso.ui.constants.Constants.TWO_AM
import com.iso.ui.constants.Constants.YELLOW
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TimeViewModel @Inject constructor(private val useCase: TimeUseCase) : ViewModel() {

    private val _location = MutableStateFlow<String?>("")
    val locationName: StateFlow<String?> get() = _location

    private val _isInternetIssue = MutableStateFlow<String>("")
    val isInternetIssue: StateFlow<String> get() = _isInternetIssue


    private val _openStatus = MutableStateFlow<StatusModel?>(StatusModel("",""))
    val openStatus: StateFlow<StatusModel?> get() = _openStatus

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
                _openStatus.value = workSchedule(timeResult)

            }, onFailure = { error ->
                if (error.message == NO_INTERNET) {
                    _isInternetIssue.value = error.message.toString()
                }
            })
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun timeCreate(workingHours: List<RestaurantTimeModel>): List<Map<String, Any>> {

        //map days full names
        val updatedList: List<RestaurantTimeModel> = workingHours.map {
            it.copy(dayOfWeek = FULL_DAY_NAMES[it.dayOfWeek] ?: it.dayOfWeek)
        }

        val fullNameList = updatedList.map {
            it.copy(
                startTime = it.startTime.toFormattedTime(),
                endTime = it.endTime.toFormattedTime()
            )
        }

        // Add closed days
        val sortedList = addClosedDays(fullNameList, DAYS_OF_WEEK)

        // Group by day of week
        val mergedList = mergeWorkHours(sortedList)

        // Processing data
        val data = mergeIntervalsWithCarryover(mergedList)

        return data
    }


    private fun mergeWorkHours(workHoursList: List<RestaurantTimeModel>): List<Map<String, Any>> {
        return workHoursList.groupBy { it.dayOfWeek }  // Group by day of week
            .map { (day, entries) ->
                mapOf(DAY_OF_WEEK to day,
                    INTERVALS to entries.map { "${it.startTime} - ${it.endTime}" })
            }
    }

    private fun mergeIntervalsWithCarryover(workHoursList: List<Map<String, Any>>): List<Map<String, Any>> {
        val result = mutableListOf<Map<String, Any>>()
        var previousDay: Map<String, Any>? = null

        for (currentDay in workHoursList) {
            val currentDayIntervals = currentDay[INTERVALS] as List<String>
            val updatedIntervals = mutableListOf<String>()

            for (interval in currentDayIntervals) {
                if (interval == "$MIDNIGHT - $MIDNIGHT" || isSameTimeInterval(interval)) {
                    // If interval is 12am - 12am or interval between time is same than it means "open 24 hours"
                    updatedIntervals.add(OPEN_24_HOURS)
                } else if (interval.startsWith(MIDNIGHT)) {
                    // If interval starts from 12am, I move it to the end of the previous day
                    previousDay?.let {
                        val lastInterval = it[INTERVALS] as List<String>
                        val updatedLastInterval =
                            lastInterval.last().replace(MIDNIGHT, interval.split(" - ")[1])
                        val newIntervals = lastInterval.dropLast(1) + updatedLastInterval
                        result[result.lastIndex] =
                            it.toMutableMap().apply { put(INTERVALS, newIntervals) }
                    }
                } else if (interval.startsWith(CLOSED)) {
                    updatedIntervals.add(CLOSED)
                } else {
                    // Add interval to current day
                    updatedIntervals.add(interval)
                }
            }

            if (updatedIntervals.isNotEmpty()) {
                val newDay = currentDay.toMutableMap().apply {
                    put(INTERVALS, updatedIntervals)
                }
                result.add(newDay)
                previousDay = newDay
            }
        }
        return result
    }

    // check 24hrs open time
    private fun isSameTimeInterval(interval: String): Boolean {
        val times = interval.split(" - ")
        return times[0] == times[1]
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun String.toFormattedTime(
    ): String {
        // Formatter for time conversion
        val inputTimeFormatter = DateTimeFormatter.ofPattern(LONG_TIME_FORMAT)  // from 1:00:00
        val outputTimeFormatter = DateTimeFormatter.ofPattern(SHORT_TIME_FORMAT) // to 1am
        val localTime = LocalTime.parse(this, inputTimeFormatter)
        return localTime.format(outputTimeFormatter).lowercase() // transform to "am"/"pm"
    }

    // Add closed days
    private fun addClosedDays(
        updatedList: List<RestaurantTimeModel>, dayOrder: List<String>
    ): List<RestaurantTimeModel> {
        val existingDays = updatedList.map { it.dayOfWeek }
        val missingDays = dayOrder.filterNot { it in existingDays }

        val fullList = updatedList + missingDays.map { day ->
            RestaurantTimeModel(dayOfWeek = day, startTime = CLOSED, endTime = "")
        }
        return fullList.sortedBy { dayOrder.indexOf(it.dayOfWeek) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun workSchedule(timeResult: List<Map<String, Any>>): StatusModel {

        //get system currentTime & currentDayOfWeek
        val currentTime = getCurrentTime().toString()
        val currentDayOfWeek = getCurrentWeek().toString()

        val timeFormatter = DateTimeFormatter.ofPattern(SHORT_TIME_FORMAT)
        val result = try {
            val currentTimeParsed = LocalTime.parse(currentTime.uppercase(), timeFormatter)
            for (entry in timeResult) {

                val dayOfWeek = entry[DAY_OF_WEEK] as? String ?: continue

                if (dayOfWeek == currentDayOfWeek) {
                    val intervals = entry[INTERVALS] as? List<String> ?: continue

                    if (intervals.size > 1) {
                        val formattedIntervals = processIntervals(intervals)

                        if (formattedIntervals.size >= 2) {

                            val secondStartTime = formattedIntervals[1][START_TIME]
                            val secondEndTime = formattedIntervals[1][END_TIME]

                            val firstStartTime = formattedIntervals[0][START_TIME]
                            val firstEndTime = formattedIntervals[0][END_TIME]


                            val secondStartTimeParsed =
                                LocalTime.parse(secondStartTime?.uppercase(), timeFormatter)
                            val secondEndTimeParsed =
                                LocalTime.parse(secondEndTime?.uppercase(), timeFormatter)

                            val firstStartTimeParsed =
                                LocalTime.parse(firstStartTime?.uppercase(), timeFormatter)
                            val firstEndTimeParsed =
                                LocalTime.parse(firstEndTime?.uppercase(), timeFormatter)


                            if (currentTimeParsed.isBefore(secondEndTimeParsed) && !currentTimeParsed.isBefore(
                                    secondStartTimeParsed
                                )
                            ) {
                                val timeLeft =
                                    Duration.between(currentTimeParsed, secondEndTimeParsed)
                                        .toMinutes()
                                // If there is 1 hour or less left
                                val statusColor = if (timeLeft <= 60) YELLOW else GREEN
                                return StatusModel("$OPEN_UNTIL $secondEndTime", statusColor)
                            } else if (!currentTimeParsed.isBefore(firstStartTimeParsed) && currentTimeParsed.isBefore(
                                    firstEndTimeParsed
                                )
                            ) {
                                val timeLeft =
                                    Duration.between(currentTimeParsed, firstEndTimeParsed)
                                        .toMinutes()
                                val statusColor = if (timeLeft <= 60) YELLOW else GREEN
                                return StatusModel(
                                    "$OPEN_UNTIL $firstEndTime, $REOPEN $secondStartTime",
                                    statusColor
                                )
                            } else if (!currentTimeParsed.isBefore(firstEndTimeParsed) && currentTimeParsed.isBefore(
                                    secondStartTimeParsed
                                )
                            ) {
                                return StatusModel("$OPEN_AGAIN $secondStartTime", RED)
                            } else if (currentTimeParsed.isBefore(firstStartTimeParsed)) {
                                return StatusModel("$OPEN_AGAIN $firstStartTimeParsed ", RED)
                            } else if (currentTimeParsed.plusHours(24)
                                    .isAfter(secondEndTimeParsed) && secondEndTime.equals(TWO_AM)
                            ) {
                                return StatusModel("$OPEN_UNTIL $TWO_AM", GREEN)
                            } else if (currentTimeParsed.isAfter(secondEndTimeParsed)) {
                                return StatusModel("$OPENS $firstStartTimeParsed", RED)
                            } else {
                                return StatusModel(CLOSED, RED)
                            }
                        }
                    } else if (intervals.component1() == OPEN_24_HOURS) {
                        return StatusModel("$OPEN_UNTIL $MIDNIGHT", GREEN)
                    } else if (intervals.component1() == CLOSED) {
                        return getNextWorkDay(
                            currentDayOfWeek = currentDayOfWeek,
                            timeResult = timeResult
                        )
                    } else {
                        return StatusModel(CLOSED, RED)
                    }
                }
            }
        } catch (e: Exception) {
            return StatusModel(CLOSED, RED)
        }
        return StatusModel(result.toString(), RED)
    }
}

// get next work day after closed days
@RequiresApi(Build.VERSION_CODES.O)
private fun getNextWorkDay(
    currentDayOfWeek: String,
    timeResult: List<Map<String, Any>>,
): StatusModel {
    for (entry in timeResult) {

        val nextDayOfWeek = getNextDayFormatted(currentDayOfWeek)

        val dayOfWeek = entry[DAY_OF_WEEK] as? String ?: continue
        if (dayOfWeek == nextDayOfWeek) {
            val intervals = entry[INTERVALS] as? List<String> ?: continue

            if (intervals.size > 1) {
                val formattedIntervals = processIntervals(intervals)

                if (formattedIntervals.size >= 2) {
                    val firstStartTime = formattedIntervals[0][START_TIME]
                    return StatusModel("$OPENS $nextDayOfWeek $firstStartTime", RED)
                }
            } else {
                return continue
            }
        }
    }
    return StatusModel(CLOSED, RED)

}

@RequiresApi(Build.VERSION_CODES.O)
private fun getNextDayFormatted(currentDay: String): String? {
    // Convert the string to DayOfWeek
    val currentDayOfWeek = DayOfWeek.entries.find {
        it.getDisplayName(TextStyle.FULL, Locale.ENGLISH).equals(currentDay, ignoreCase = true)
    } ?: return null // If the day of the week is not found, null is returned.

    // We get the next day
    val nextDayOfWeek = currentDayOfWeek.plus(1)

    // Return the next day in the "Monday" format
    return nextDayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
}

// get intervals between start time and end time
private fun processIntervals(intervals: List<String>): List<Map<String, String>> {
    return intervals.map { interval ->
        val times = interval.split(" - ")
        mapOf(
            START_TIME to times[0].trim(),
            END_TIME to times[1].trim()
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun getCurrentTime(): String? {
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern(SHORT_TIME_FORMAT)
    return currentDateTime.format(formatter)
}

@RequiresApi(Build.VERSION_CODES.O)
private fun getCurrentWeek(): String? {
    val currentDateTime = LocalDateTime.now()
    return currentDateTime.dayOfWeek.getDisplayName(
        TextStyle.FULL,
        Locale.getDefault()
    )
}


data class StatusModel(
    val statusText: String,
    val statusColor: String
)