package com.cursokotlin.alarmanager

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.cursokotlin.alarmanager.model.AlarmData

class AlarmDAO(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "AlarmDatabase"
        private const val TABLE_ALARMS = "alarms"

        private const val KEY_ID = "id"
        private const val KEY_HOUR = "hour"
        private const val KEY_DAYS = "days"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE IF NOT EXISTS $TABLE_ALARMS($KEY_ID INTEGER PRIMARY KEY, $KEY_HOUR TEXT, $KEY_DAYS TEXT)")
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ALARMS")
        onCreate(db)
    }

    fun addAlarm(alarmData: AlarmData) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_HOUR, alarmData.alarmHour)
        values.put(KEY_DAYS, alarmData.alarmDays)

        db.insert(TABLE_ALARMS, null, values)
        db.close()
    }

    @SuppressLint("Range")
    fun getAllAlarms(): ArrayList<AlarmData> {
        val alarmList = ArrayList<AlarmData>()
        val selectQuery = "SELECT * FROM $TABLE_ALARMS"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        cursor?.use {
            if (it.moveToFirst()) {
                do {
                    val id = it.getInt(it.getColumnIndex(KEY_ID))
                    val hour = it.getString(it.getColumnIndex(KEY_HOUR))
                    val days = it.getString(it.getColumnIndex(KEY_DAYS))

                    val alarmData = AlarmData(hour, days)
                    alarmList.add(alarmData)
                } while (it.moveToNext())
            }
        }

        cursor.close()
        db.close()

        return alarmList
    }
}