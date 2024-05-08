package com.cursokotlin.alarmanager

import SleepTracker
import android.content.Context
import android.content.res.ColorStateList
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton

class Start_Sleep : Fragment(), SensorEventListener {

    private lateinit var startStop: FloatingActionButton
    private lateinit var startStopText: TextView
    private lateinit var stopSleep: TextView
    private lateinit var stopLayout: LinearLayout
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var sleepTracker: SleepTracker? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_start__sleep, container, false)

        startStopText = root.findViewById(R.id.start_stop_text)
        stopSleep = root.findViewById(R.id.stop_sleep)
        startStop = root.findViewById(R.id.start_stop)
        stopLayout = root.findViewById(R.id.stop_layout)

        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        startStop.contentDescription = getString(R.string.stop)
        startStop.setImageResource(R.drawable.ic_stop)
        startStopText.text = getString(R.string.stop)

        startStop.backgroundTintList = createColorStateList(R.color.colorFabAccent)
        stopLayout.backgroundTintList = startStop.backgroundTintList

        stopLayout.setOnClickListener {
            findNavController().navigate(R.id.action_start_Sleep_to_bedtimeFragment)
            stopAccelerometerTracking()
        }

        return root
    }

    override fun onStart() {
        super.onStart()
        startAccelerometerTracking()
    }

    override fun onStop() {
        super.onStop()
        stopAccelerometerTracking()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not implemented
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // Handle accelerometer changes here
        event?.let {
            val x = it.values[0]
            val y = it.values[1]
            val z = it.values[2]
            // Log accelerometer values
            // You can implement your comparison logic here
        }
    }

    private fun createColorStateList(color: Int): ColorStateList {
        return ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color))
    }

    private fun startAccelerometerTracking() {
        sleepTracker = SleepTracker(requireContext())
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun stopAccelerometerTracking() {
        sleepTracker?.stopAccelerometerTracking()
        sensorManager.unregisterListener(this)
    }
}
