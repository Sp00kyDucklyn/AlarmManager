package com.cursokotlin.alarmanager

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cursokotlin.alarmanager.model.AlarmData
import com.cursokotlin.alarmanager.model.State
import com.cursokotlin.alarmanager.model.WeekDays
import com.cursokotlin.alarmanager.view.AlarmAdapter2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlin.properties.Delegates


class Alarm : Fragment(){

    private lateinit var addsBtn: FloatingActionButton
    private lateinit var recv: RecyclerView
    private lateinit var userList:ArrayList<AlarmData>
    private lateinit var userAdapter: AlarmAdapter2
    private lateinit var picker:MaterialTimePicker

    private lateinit var recyclerView: RecyclerView

    private val REQUEST_CODE_PICK_AUDIO = 123

    private var alarmId: Int = 0
    private lateinit var selectedTimeString: String
    private lateinit var selectedDaysString: String

    private var editEnabled: Boolean = false
    private var editAlarmId: Int? = null // Almacena el ID de la alarma que se va a editar
    private var isPickerVisible = false // Indica si el selector de tiempo está visible



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
        deployAlarms(requireContext())

        // Verifica si se está editando una alarma
        if (editEnabled && editAlarmId != null && !isPickerVisible) {
            // Muestra el selector de tiempo si se está editando una alarma y no está visible
            showTimePicker()
        }
    }

    private fun showTimePicker() {
        picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Select Alarm Time")
            .build()

        picker.addOnCancelListener {
            isPickerVisible = false
        }

        picker.addOnDismissListener {
            isPickerVisible = false
        }

        picker.addOnPositiveButtonClickListener {
            isPickerVisible = false
            val hour = picker.hour
            val minute = picker.minute
            val timeString = String.format("%02d:%02d", hour, minute)

            // Seleccionar los días de la semana
            showDaysOfWeekDialog(timeString)
        }

        // Asegúrate de que el fragmento esté adjunto a la actividad antes de mostrar el selector de tiempo
        if (!isAdded || picker.isVisible || isPickerVisible) {
            return
        }

        isPickerVisible = true
        picker.show(childFragmentManager, "1")
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
            selectedTimeString = timeString
            selectedDaysString = daysString
            selectTone()
            dialog.dismiss()
        }

        alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        alertDialogBuilder.create().show()
    }

    private fun createAlarm(alarmId: Int, timeString: String, daysString: String, alarmTone: Uri) {
        val alarmData = AlarmData(0, timeString, daysString, alarmTone, alarmState = State.ON)

        val dbHandler = AlarmDAO(requireContext())
        dbHandler.addAlarm(alarmData)

        Toast.makeText(requireContext(), "Alarm added successfully", Toast.LENGTH_SHORT).show()
        deployAlarms(requireContext())
    }

    private fun updateAlarm(alarmId: Int, timeString: String, daysString: String, alarmTone: Uri){
        val alarmData = AlarmData(alarmId, timeString, daysString, alarmTone, alarmState = State.ON)
        val dbHandler = AlarmDAO(requireContext())
        dbHandler.updateAlarm(alarmData)

        Toast.makeText(requireContext(), "Alarm updated successfully", Toast.LENGTH_SHORT).show()
        deployAlarms(requireContext())
    }

    private fun selectTone() {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Select your alarm tone")
        alertDialogBuilder.setPositiveButton("Pick file") { dialog, _ ->
            //intent para abrir el explorador de archivos y seleccionar un audio
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "audio/*" //filtro para mostrar solo archivos de audio

            startActivityForResult(intent, REQUEST_CODE_PICK_AUDIO)

            dialog.dismiss()
        }

        alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            val defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            handleSelectedDataAlarm(defaultRingtoneUri)
            dialog.dismiss()
        }

        alertDialogBuilder.create().show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_AUDIO && resultCode == Activity.RESULT_OK) {
            //obtener la URI del audio seleccionado
            val selectedAudioUri = data?.data
            handleSelectedDataAlarm(selectedAudioUri)
        }
    }

    private fun handleSelectedDataAlarm(audioUri: Uri?) {
        if (audioUri != null && !editEnabled) {
            createAlarm(0, selectedTimeString, selectedDaysString, audioUri)
        }else if (audioUri != null){
            updateAlarm(alarmId, selectedTimeString, selectedDaysString, audioUri)
        }
    }

    private fun deployAlarms(context: Context) {
        val dbHandler = AlarmDAO(context)
        val alarmList = dbHandler.getAllAlarms()

        recyclerView.adapter = AlarmAdapter2(context, alarmList)
    }


    fun editAlarm(context: Context, alarmId: Int) {
        val dbHandler = AlarmDAO(context)
        val alarm = dbHandler.getAlarmById(alarmId)
        if (alarm != null) {
            this.alarmId = alarm.alarmId
        }
        editEnabled = true
    }

    fun deleteAlarm(context: Context, alarmId: Int) {
        val dbHandler = AlarmDAO(context)
        dbHandler.deleteAlarmById(alarmId)
        Toast.makeText(context, "Alarm deleted successfully", Toast.LENGTH_SHORT).show()
        //deployAlarms(context)
    }


}
