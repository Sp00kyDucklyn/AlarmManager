package com.cursokotlin.alarmanager.model

import android.net.Uri

data class AlarmData (
    var alarmId: Int,
    var alarmHour:String,
    var alarmDays: String,
    var alarmTone: Uri,
    var alarmState: State
)
