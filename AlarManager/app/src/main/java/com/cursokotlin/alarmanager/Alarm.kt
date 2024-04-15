package com.cursokotlin.alarmanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat


class Alarm : Fragment() {

    private lateinit var picker:MaterialTimePicker

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_alarm, container, false)

        val btnAddAlarm:ImageButton =root.findViewById<ImageButton>(R.id.addAlarmButton)

        btnAddAlarm.setOnClickListener(){

            showTimePicker()
        }
        return root
    }

    private fun showTimePicker() {

        picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Select Alarm Time")
            .build()

        picker.show(childFragmentManager,"1")

        picker.addOnPositiveButtonClickListener {

            if(picker.hour >12){


            }

        }


    }


}