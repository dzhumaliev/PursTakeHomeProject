package com.iso.ui.constants

object Constants {
    val DAYS_OF_WEEK = listOf(
        "Monday", "Tuesday", "Wednesday", "Thursday",
        "Friday", "Saturday", "Sunday"
    )

    val FULL_DAY_NAMES = mapOf(
        "MON" to "Monday",
        "TUE" to "Tuesday",
        "WED" to "Wednesday",
        "THU" to "Thursday",
        "FRI" to "Friday",
        "SAT" to "Saturday",
        "SUN" to "Sunday"
    )

    const val DAY_OF_WEEK = "day_of_week"
    const val INTERVALS = "intervals"
    const val MIDNIGHT = "12am"
    const val TWO_AM = "2am"
    const val OPEN_24_HOURS = "Open 24hrs"
    const val CLOSED = "Closed"
    const val SHORT_TIME_FORMAT = "ha"
    const val LONG_TIME_FORMAT = "H:mm:ss"
    const val START_TIME = "starttime"
    const val END_TIME = "endtime"
    const val GREEN = "Green"
    const val RED = "Red"
    const val YELLOW = "Yellow"
    const val OPEN_UNTIL = "Open until"
    const val REOPEN = "reopens at"
    const val OPEN_AGAIN = "Opens again at"
    const val OPENS = "Opens"
    const val NO_INTERNET = "No internet connection"
}