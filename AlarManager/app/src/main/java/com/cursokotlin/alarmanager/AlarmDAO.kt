package com.cursokotlin.alarmanager

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import com.cursokotlin.alarmanager.model.AlarmData
import com.cursokotlin.alarmanager.model.State

class AlarmDAO(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_VERSION = 3
        private const val DATABASE_NAME = "AlarmDatabase"
        private const val TABLE_ALARMS = "alarms"

        private const val KEY_ID = "id"
        private const val KEY_HOUR = "hour"
        private const val KEY_DAYS = "days"
        private const val KEY_TONE = "tone"
        private const val KEY_STATE = "state"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE IF NOT EXISTS $TABLE_ALARMS($KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT, $KEY_HOUR TEXT, $KEY_DAYS TEXT, $KEY_TONE TEXT, $KEY_STATE TEXT)")
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
        values.put(KEY_TONE, alarmData.alarmTone.toString())
        values.put(KEY_STATE, alarmData.alarmState.toString())

        db.insert(TABLE_ALARMS, null, values)
        db.close()
    }

    fun updateAlarm(alarmData: AlarmData) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_HOUR, alarmData.alarmHour)
        values.put(KEY_DAYS, alarmData.alarmDays)
        values.put(KEY_TONE, alarmData.alarmTone.toString())
        values.put(KEY_STATE, alarmData.alarmState.toString())

        db.update(TABLE_ALARMS, values, "$KEY_ID = ?", arrayOf(alarmData.alarmId.toString()))
        db.close()
    }

    fun deleteAlarmById(alarmId: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_ALARMS, "$KEY_ID = ?", arrayOf(alarmId.toString()))
        db.close()
    }

    @SuppressLint("Range")
    fun getAlarmById(alarmId: Int): AlarmData? {
        val db = this.readableDatabase
        var alarmData: AlarmData? = null
        val selectQuery = "SELECT * FROM $TABLE_ALARMS WHERE $KEY_ID = ?"
        val cursor = db.rawQuery(selectQuery, arrayOf(alarmId.toString()))

        cursor?.use {
            if (it.moveToFirst()) {
                val hour = it.getString(it.getColumnIndex(KEY_HOUR))
                val days = it.getString(it.getColumnIndex(KEY_DAYS))
                val toneUri = Uri.parse(it.getString(it.getColumnIndex(KEY_TONE)))
                val state = State.valueOf(it.getString(it.getColumnIndex(KEY_STATE)))

                alarmData = AlarmData(alarmId, hour, days, toneUri, state)
            }
        }

        cursor.close()
        db.close()

        return alarmData
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
                    val toneUri = Uri.parse(it.getString(it.getColumnIndex(KEY_TONE)))
                    val state = State.valueOf(it.getString(it.getColumnIndex(KEY_STATE)))

                    val alarmData = AlarmData(id, hour, days, toneUri, state)
                    alarmList.add(alarmData)
                } while (it.moveToNext())
            }
        }

        cursor.close()
        db.close()

        return alarmList
    }

    fun updateAlarmState(alarmId: Int, state: State) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_STATE, state.name)
        }

        db.update(TABLE_ALARMS, values, "$KEY_ID=?", arrayOf(alarmId.toString()))
        db.close()
    }
}
