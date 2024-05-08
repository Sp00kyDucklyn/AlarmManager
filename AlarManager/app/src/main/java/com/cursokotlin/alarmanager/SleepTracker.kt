import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.util.Log

class SleepTracker(private val context: Context) {

    private val TAG = "SleepTracker"

    private var sensorManager: SensorManager? = null
    private var accelerometerSensor: Sensor? = null
    private var accelerometerListener: SensorEventListener? = null
    private val printIntervalMillis = 1L // Intervalo de tiempo entre impresiones
    private val handler = Handler(Looper.getMainLooper())

    init {
        // Inicializar el SensorManager
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Obtener el sensor de acelerómetro
        accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Iniciar la escucha del sensor de acelerómetro
        startAccelerometerTracking()
    }

    fun startAccelerometerTracking() {
        accelerometerListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                // Manejar los cambios en el sensor de acelerómetro aquí
                event?.let {
                    // Log the accelerometer values
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    Log.d(TAG, "Valor X: $x, Valor Y: $y, Valor Z: $z")
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Manejar los cambios en la precisión del sensor si es necesario
            }
        }

        // Registrar el listener para el sensor de acelerómetro
        accelerometerSensor?.let {
            sensorManager?.registerListener(
                accelerometerListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        // Programar la primera impresión después de 5 segundos
        handler.postDelayed(printRunnable, printIntervalMillis)
    }

    // Runnable para imprimir los valores del acelerómetro cada 5 segundos
    private val printRunnable = object : Runnable {
        override fun run() {
            // Programar la próxima impresión después de 5 segundos
            handler.postDelayed(this, printIntervalMillis)
        }
    }

    // Método para detener la escucha del sensor de acelerómetro si es necesario
    fun stopAccelerometerTracking() {
        accelerometerListener?.let {
            sensorManager?.unregisterListener(it)
        }

        // Detener la programación de impresión
        handler.removeCallbacks(printRunnable)
    }
}
