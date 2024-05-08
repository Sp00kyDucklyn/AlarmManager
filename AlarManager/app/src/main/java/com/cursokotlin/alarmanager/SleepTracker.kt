package com.cursokotlin.alarmanager

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class SleepTracker(private val context: Context) : SensorEventListener {

    private val TAG = "SleepTracker"

    private var sensorManager: SensorManager? = null
    private var accelerometerSensor: Sensor? = null

    init {
        // Inicializar el SensorManager
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Obtener el sensor de acelerómetro
        accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Iniciar la escucha del sensor de acelerómetro
        startAccelerometerTracking()
    }

    private fun startAccelerometerTracking() {
        // Registrar el listener para el sensor de acelerómetro
        accelerometerSensor?.let {
            sensorManager?.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    // Método para detener la escucha del sensor de acelerómetro si es necesario
    fun stopAccelerometerTracking() {
        accelerometerSensor?.let {
            sensorManager?.unregisterListener(this)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No se utiliza en este ejemplo
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // Manejar los cambios en el sensor de acelerómetro aquí
        // Por ejemplo, puedes obtener los valores de los ejes X, Y, Z
        event?.let {
            val x = it.values[0]
            val y = it.values[1]
            val z = it.values[2]
            Log.d(TAG, "Valor X: $x, Valor Y: $y, Valor Z: $z")
        }
    }
}
