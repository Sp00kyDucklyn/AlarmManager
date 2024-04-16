package com.cursokotlin.alarmanager

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cursokotlin.alarmanager.model.AlarmData
import com.cursokotlin.alarmanager.model.WeekDays
import com.cursokotlin.alarmanager.view.AlarmAdapter2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat

class Alarm : Fragment() {

    private lateinit var addsBtn: FloatingActionButton
    private lateinit var recv: RecyclerView
    private lateinit var userList:ArrayList<AlarmData>
    private lateinit var userAdapter: AlarmAdapter2
    private lateinit var picker:MaterialTimePicker

    private lateinit var recyclerView: RecyclerView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_alarm, container, false)

        recyclerView = root.findViewById(R.id.mRecycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        addsBtn = root.findViewById(R.id.addingBtn)
        recv = root.findViewById(R.id.mRecycler)
        userList = ArrayList()
        userAdapter = AlarmAdapter2(requireContext(), userList)
        recv.adapter = userAdapter

        addsBtn.setOnClickListener { showTimePicker() }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        deployAlarms()
    }

    private fun showTimePicker() {
        picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Select Alarm Time")
            .build()

        picker.show(childFragmentManager, "1")

        picker.addOnPositiveButtonClickListener {
            val hour = picker.hour
            val minute = picker.minute
            val timeString = String.format("%02d:%02d", hour, minute)

            //seleccionar los días de la semana
            showDaysOfWeekDialog(timeString)
        }
    }

    private fun showDaysOfWeekDialog(timeString: String) {
        val weekDays = WeekDays()

        val daysOfWeek = arrayOf(
            "Monday", "Tuesday", "Wednesday",
            "Thursday", "Friday", "Saturday", "Sunday"
        )

        val checkedDays = booleanArrayOf(
            weekDays.monday, weekDays.tuesday, weekDays.wednesday,
            weekDays.thursday, weekDays.friday, weekDays.saturday, weekDays.sunday
        )

        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Select Days of the Week")
        alertDialogBuilder.setMultiChoiceItems(daysOfWeek, checkedDays) { _, which, isChecked ->
            when (which) {
                0 -> weekDays.monday = isChecked
                1 -> weekDays.tuesday = isChecked
                2 -> weekDays.wednesday = isChecked
                3 -> weekDays.thursday = isChecked
                4 -> weekDays.friday = isChecked
                5 -> weekDays.saturday = isChecked
                6 -> weekDays.sunday = isChecked
            }
        }

        alertDialogBuilder.setPositiveButton("Ok") { dialog, _ ->
            val daysString = weekDays.getAllDaysAsString()
            createAlarm(timeString, daysString)
            dialog.dismiss()
        }

        alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        alertDialogBuilder.create().show()
    }

    private fun createAlarm(timeString: String, daysString: String) {
        val alarmData = AlarmData(timeString, daysString)

        val dbHandler = AlarmDAO(requireContext())
        dbHandler.addAlarm(alarmData)

        Toast.makeText(requireContext(), "Alarm added successfully", Toast.LENGTH_SHORT).show()
        deployAlarms()
    }

    private fun deployAlarms() {
        val dbHandler = AlarmDAO(requireContext())
        val alarmList = dbHandler.getAllAlarms()

        recyclerView.adapter = AlarmAdapter2(requireContext(), alarmList)
    }
}
