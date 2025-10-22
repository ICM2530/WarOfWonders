package com.example.warofwonders.data.source.hardware

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class LightSensorDataSource(
    private val context: Context
) {
    private var listener: SensorEventListener? = null

    fun startListening(onLightChanged: (Float) -> Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) ?: return

        listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val lux = event.values[0]
                onLightChanged(lux)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(
            listener,
            lightSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    fun stopListening() {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        listener?.let { sensorManager.unregisterListener(it) }
        listener = null
    }
}
