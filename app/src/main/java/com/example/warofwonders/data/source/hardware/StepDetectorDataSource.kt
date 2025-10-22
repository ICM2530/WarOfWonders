package com.example.warofwonders.data.source.hardware

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class StepDetectorDataSource(
    private val context: Context
) {
    private var listener: SensorEventListener? = null
    private var stepCount = 0

    /**
     * Empieza a escuchar el sensor de detección de pasos.
     * @param onStepDetected callback que recibe el número total de pasos detectados.
     */
    fun startListening(onStepDetected: (Int) -> Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) ?: return

        listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                // TYPE_STEP_DETECTOR genera un evento por cada paso detectado
                if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                    stepCount++
                    onStepDetected(stepCount)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // No se requiere manejar precisión en este tipo de sensor
            }
        }

        sensorManager.registerListener(
            listener,
            stepSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    /**
     * Detiene la escucha del sensor y libera recursos.
     */
    fun stopListening() {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        listener?.let { sensorManager.unregisterListener(it) }
        listener = null
    }
}