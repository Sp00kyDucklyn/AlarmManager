package com.cursokotlin.alarmanager

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import java.util.Calendar


class Alarm : Fragment() {

    private lateinit var addsBtn: FloatingActionButton
    private lateinit var recv: RecyclerView
    private lateinit var userList: ArrayList<AlarmData>
    private lateinit var userAdapter: AlarmAdapter2
    private lateinit var picker: MaterialTimePicker

    private lateinit var recyclerView: RecyclerView

    private val REQUEST_CODE_PICK_AUDIO = 123
    private val EXTRA_SERVICE_ID = "service_id"
    private val EXTRA_DELAY = "delay"
    private var alarmId: Int = 0
    private lateinit var selectedTimeString: String
    private lateinit var selectedDaysString: String

    /*private var editEnabled: Boolean = false*/
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
        requestNotificationPermissions()
    }
    private val REQUEST_NOTIFICATION_PERMISSION = 1

    private fun requestNotificationPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
            // Si el permiso no está otorgado, solicita el permiso
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.VIBRATE)) {
                // Si el usuario ha rechazado el permiso previamente, muestra un diálogo explicativo
                showPermissionExplanationDialog()
            } else {
                // Si es la primera vez que se solicita el permiso o si el usuario marcó "No volver a preguntar", solicita el permiso directamente
                requestPermissions(arrayOf(android.Manifest.permission.VIBRATE), REQUEST_NOTIFICATION_PERMISSION)
            }
        }
    }

    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Needed")
            .setMessage("This app requires notification permission to function properly.")
            .setPositiveButton("OK") { _, _ ->
                // Cuando el usuario hace clic en Aceptar, solicita el permiso
                requestPermissions(arrayOf(android.Manifest.permission.VIBRATE), REQUEST_NOTIFICATION_PERMISSION)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // Cuando el usuario hace clic en Cancelar, cierra el diálogo
                dialog.dismiss()
            }
            .create()
            .show()
    }
    fun showTimePicker() {

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
        val hoursUntilAlarm = calculateMinutesUntilAlarm(timeString, daysString)
        println(hoursUntilAlarm)
        val toastMessage = if (hoursUntilAlarm > 0) {
            "Alarm added successfully. Alarm will go off in $hoursUntilAlarm minutos."
        } else {
            "Alarm added successfully. Alarm will go off soon."
        }
        Toast.makeText(requireContext(), toastMessage , Toast.LENGTH_SHORT).show()
        deployAlarms(requireContext())
        val nombre :String= ""+daysString+" "+timeString
        val dinero :Long =((hoursUntilAlarm*60*1000).toLong())
            startService(0,dinero,nombre)
    }
    private fun startService(serviceId: Int, delayMillis: Long,nombre:String) {
        val serviceIntent = Intent(requireContext(), NotificationService::class.java)
        serviceIntent.putExtra(EXTRA_SERVICE_ID, serviceId)
        serviceIntent.putExtra(EXTRA_DELAY, delayMillis)
        serviceIntent.putExtra("Nombre", nombre)
        requireContext().startService(serviceIntent)
    }

    private fun calculateMinutesUntilAlarm(timeString: String, daysString: String): Int {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_WEEK) // 1 for Sunday, 2 for Monday, ..., 7 for Saturday
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        // Parsea la hora seleccionada para la alarma
        val parts = timeString.split(":")
        val selectedHour = parts[0].toInt()
        val selectedMinute = parts[1].toInt()

        // Obtenemos el día actual en formato de cadena
        val currentDayName = getDayOfWeek(currentDay)

        // Verificamos si el día actual está seleccionado para la alarma
        val selectedDays = daysString.split(",")
        val daysDifference = daysDifference(currentDayName, selectedDays)
        println("hora "+selectedHour+"current"+currentHour)
        // Calcula la diferencia de tiempo en minutos
        var hoursDifference = selectedHour - currentHour
        val minutesDifference = selectedMinute - currentMinute

        if (hoursDifference < 0 || (hoursDifference == 0 && minutesDifference < 0)) {
            // Si la alarma está programada para después de la hora actual, suma 24 horas
            hoursDifference += 24
        }

        val totalMinutesDifference = (daysDifference * 24 * 60) + (hoursDifference * 60) + minutesDifference
        return if (totalMinutesDifference >= 0) totalMinutesDifference else 0
    }

    private fun daysDifference(currentDay: String, selectedDays: List<String>): Int {
        val currentIndex = selectedDays.indexOf(currentDay)
        val selectedDaysCount = selectedDays.size

        if (currentIndex != -1) {
            // Si el día actual está en la lista de días seleccionados, la diferencia es cero
            return 0
        } else {
            // Si el día actual no está en la lista, encontramos el siguiente día en la lista
            var nextIndex = (currentIndex + 1) % selectedDaysCount
            var counter = 1 // Iniciamos en 1 porque ya hemos avanzado un día

            // Buscamos el siguiente día en la lista, teniendo en cuenta la posibilidad de llegar al final de la lista
            while (selectedDays[nextIndex] != currentDay && counter < selectedDaysCount) {
                nextIndex = (nextIndex + 1) % selectedDaysCount
                counter++
            }

            return counter
        }
    }
    private fun getDayOfWeek(day: Int): String {
        return when (day) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> throw IllegalArgumentException("Invalid day of week")
        }
    }
    private fun updateAlarm(alarmId: Int, timeString: String, daysString: String, alarmTone: Uri) {
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
        if (audioUri != null) {
            createAlarm(0, selectedTimeString, selectedDaysString, audioUri)
        }
    }

    private fun deployAlarms(context: Context) {
        val dbHandler = AlarmDAO(context)
        val alarmList = dbHandler.getAllAlarms()

        recyclerView.adapter = AlarmAdapter2(context, alarmList)
    }

    fun deleteAlarm(context: Context, alarmId: Int) {
        val dbHandler = AlarmDAO(context)
        dbHandler.deleteAlarmById(alarmId)
        Toast.makeText(context, "Alarm deleted successfully", Toast.LENGTH_SHORT).show()
        //deployAlarms(context)
        val mainActivity = context as MainActivity
        mainActivity.goToBedtimeFragment()
    }

}

    /*private fun showEditAlarmDialog(alarmData: AlarmData) {
        val hour = alarmData.alarmHour
        val days = alarmData.alarmDays
        val alarmTone = alarmData.alarmTone

        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(hour.substringBefore(":").toInt())
            .setMinute(hour.substringAfter(":").toInt())
            .setTitleText("Edit Alarm Time")
            .build()

        picker.addOnCancelListener {
            isPickerVisible = false
        }

        picker.addOnDismissListener {
            isPickerVisible = false
        }

        picker.addOnPositiveButtonClickListener {
            isPickerVisible = false
            val newHour = String.format("%02d:%02d", picker.hour, picker.minute)
            showEditDaysOfWeekDialog(alarmData, newHour)
        }

        // Verifica si el fragmento está adjunto antes de mostrar el picker
        if (!isAdded || picker.isVisible || isPickerVisible) {
            return
        }

        isPickerVisible = true
        picker.show(childFragmentManager, "EditAlarmPicker")
    }

    private fun showEditDaysOfWeekDialog(alarmData: AlarmData, timeString: String) {
        val weekDays = WeekDays()
        weekDays.setDaysFromString(alarmData.alarmDays)

        val daysOfWeek = arrayOf(
            "Monday", "Tuesday", "Wednesday",
            "Thursday", "Friday", "Saturday", "Sunday"
        )

        val checkedDays = booleanArrayOf(
            weekDays.monday, weekDays.tuesday, weekDays.wednesday,
            weekDays.thursday, weekDays.friday, weekDays.saturday, weekDays.sunday
        )

        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Edit Days of the Week")
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
            showEditTonePicker(alarmData)
            dialog.dismiss()
        }

        alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        alertDialogBuilder.create().show()
    }

    private fun showEditTonePicker(alarmData: AlarmData) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Select New Alarm Tone")
        alertDialogBuilder.setPositiveButton("Pick File") { dialog, _ ->
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "audio/*"

            startActivityForResult(intent, REQUEST_CODE_PICK_AUDIO)

            dialog.dismiss()
        }

        alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            val defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            handleEditAlarmTone(alarmData, defaultRingtoneUri)
            dialog.dismiss()
        }

        alertDialogBuilder.create().show()
    }

    private fun handleEditAlarmTone(alarmData: AlarmData, audioUri: Uri?) {
        if (audioUri != null) {
            updateAlarm(alarmData.alarmId, selectedTimeString, selectedDaysString, audioUri)
        } else {
            updateAlarm(alarmData.alarmId, selectedTimeString, selectedDaysString, alarmData.alarmTone)
        }
    }

    fun editAlarm(context: Context, alarmId: Int) {
        val dbHandler = AlarmDAO(context)
        val alarm = dbHandler.getAlarmById(alarmId)
        if (alarm != null) {
            this.alarmId = alarm.alarmId
            println("EDITAR AAAA")
            showTimePicker()
        }
    }
    */
     */
