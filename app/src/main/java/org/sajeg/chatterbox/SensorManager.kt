package org.sajeg.chatterbox

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class SensorManager (
    private val context: Context,
    private var sensorType: Int
) : SensorEventListener{

    private var onSensorValuesChanged: ((List<Float>) -> Unit)? = null
    private lateinit var sensorManager: SensorManager
    private var sensor: Sensor? = null

    fun startListening() {
        if (!::sensorManager.isInitialized && sensor == null) {
            sensorManager = context.getSystemService(SensorManager::class.java) as SensorManager
            sensor = sensorManager.getDefaultSensor(sensorType)
        }
        sensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stopListing() {
        if (!::sensorManager.isInitialized) {
            return
        }
        sensorManager.unregisterListener(this)
    }

    fun setOnSensorValuesChangedListener(listener: (List<Float>) -> Unit) {
        onSensorValuesChanged = listener
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == sensorType) {
            onSensorValuesChanged?.invoke(event.values.toList())
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}