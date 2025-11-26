package com.example.warofwonders.data.source.hardware

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

class MagnetometerDataSource(
    private val context: Context
) {
    private var listener: SensorEventListener? = null

    /**
     * Empieza a escuchar los cambios del magnetómetro.
     * @param onMagneticFieldChanged callback que recibe intensidad total del campo magnético (µT)
     */
    fun startListening(onMagneticFieldChanged: (Float) -> Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) ?: return

        listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                // Intensidad total del campo magnético en microteslas (µT)
                val magnitude = sqrt(x * x + y * y + z * z)

                onMagneticFieldChanged(magnitude)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // No es necesario manejar cambios de precisión
            }
        }

        sensorManager.registerListener(
            listener,
            magneticSensor,
            SensorManager.SENSOR_DELAY_UI
        )
    }

    /**
     * Detiene la escucha del sensor y libera los recursos.
     */
    fun stopListening() {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        listener?.let { sensorManager.unregisterListener(it) }
        listener = null
    }
}