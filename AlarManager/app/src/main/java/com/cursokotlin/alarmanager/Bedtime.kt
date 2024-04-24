package com.cursokotlin.alarmanager

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService


class Bedtime : Fragment(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private lateinit var textView: TextView
    private lateinit var sleepButton: Button
    private var buttonClicked = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_bedtime, container, false)

        textView = root.findViewById(R.id.textView2)
        sleepButton = root.findViewById(R.id.button2)

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        return root
    }

    override fun onResume() {
        super.onResume()
        lightSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        sleepButton.setOnClickListener {
            buttonClicked = true
            if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
                val lightValue = event.values[0]
                when {
                    lightValue < 20f -> { // Muy oscuro
                        textView.setTextColor(Color.WHITE)
                        textView.text = "Felicidades, podras dormir agusto!"
                    }
                    lightValue < 100f -> { // Poca luz
                        val orangeColor = Color.rgb(255, 165, 0)
                        textView.setTextColor(Color.WHITE)
                        textView.text = "Puedes dormir mejor, busca mejor iluminación!"
                    }
                    else -> { // Mucha luz
                        textView.setTextColor(Color.WHITE)
                        textView.text = "Aguas no vas a poder dormir nada!"
                    }
                }
            }

        }


    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

}