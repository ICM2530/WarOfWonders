package com.example.warofwonders.data.source.hardware

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class TemperatureSensorDataSource(
    private val context: Context
) {
    private var listener: SensorEventListener? = null

    /**
     * Comienza a escuchar los cambios del sensor de temperatura ambiente.
     * @param onTemperatureChanged callback que recibe la temperatura en grados Celsius (°C).
     */
    fun startListening(onTemperatureChanged: (Float) -> Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) ?: return

        listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val temperature = event.values[0] // Temperatura en °C
                onTemperatureChanged(temperature)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // No se necesita manejar cambios de precisión
            }
        }

        sensorManager.registerListener(
            listener,
            temperatureSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    /**
     * Detiene la escucha del sensor y libera los recursos asociados.
     */
    fun stopListening() {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        listener?.let { sensorManager.unregisterListener(it) }
        listener = null
    }
}