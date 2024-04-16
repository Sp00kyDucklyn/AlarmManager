package com.cursokotlin.alarmanager.model

class WeekDays(
    var monday: Boolean = false,
    var tuesday: Boolean = false,
    var wednesday: Boolean = false,
    var thursday: Boolean = false,
    var friday: Boolean = false,
    var saturday: Boolean = false,
    var sunday: Boolean = false
) {
    fun getAllDaysAsString(): String {
        val daysList = mutableListOf<String>()
        if (monday) daysList.add("Monday")
        if (tuesday) daysList.add("Tuesday")
        if (wednesday) daysList.add("Wednesday")
        if (thursday) daysList.add("Thursday")
        if (friday) daysList.add("Friday")
        if (saturday) daysList.add("Saturday")
        if (sunday) daysList.add("Sunday")
        return daysList.joinToString(", ")
    }
}
