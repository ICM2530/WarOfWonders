package com.example.warofwonders.data.source.hardware

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class BarometerSensorDataSource(
    private val context: Context
) {
    private var listener: SensorEventListener? = null

    /**
     * Empieza a escuchar los cambios del sensor de presión (barómetro).
     * @param onPressureChanged función callback que recibe el valor de presión en hPa.
     */
    fun startListening(onPressureChanged: (Float) -> Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) ?: return

        listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val pressure = event.values[0] // presión atmosférica en hPa (hectopascales)
                onPressureChanged(pressure)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // No hacemos nada en este caso
            }
        }

        sensorManager.registerListener(
            listener,
            pressureSensor,
            SensorManager.SENSOR_DELAY_NORMAL
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