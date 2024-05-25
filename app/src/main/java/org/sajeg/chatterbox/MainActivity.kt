package org.sajeg.chatterbox

import android.content.Context
import android.hardware.Sensor
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import org.sajeg.chatterbox.ui.theme.ChatterboxTheme


class MainActivity : ComponentActivity() {
    private lateinit var powerManager: PowerManager
    private lateinit var lock: PowerManager.WakeLock
    private lateinit var proximitySensor: SensorManager
    private var distance: Float = 0.0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChatterboxTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var modifier = Modifier.padding(innerPadding)
//                    Button(modifier = modifier,
//                        onClick = { LLMManager.addChatMessage("What do I need to keep in mind when working with android Studio?") }) {
//                    }
                }
            }
            TTSManager.initialize(this)
            SpeechManager.initialize(this)

            proximitySensor = SensorManager(
                context = this,
                sensorType = Sensor.TYPE_PROXIMITY
            )

            powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            lock = powerManager.newWakeLock(
                PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                "chatterbox:call"
            )

            proximitySensor.startListening()
            proximitySensor.setOnSensorValuesChangedListener { values ->
                distance = values[0]
                Log.d("SensorData", distance.toString())
                if (distance == 0.0F && !TTSManager.isSpeaking()) {
                    if (!lock.isHeld) lock.acquire(10 * 60 * 1000L /*10 minutes*/)
                    SpeechManager.startRecognition()
//                    CoroutineScope(Dispatchers.IO).launch {
//                        while (distance == 0.0F) {
//                            if (!TTSManager.isSpeaking()) {
//                                Log.d("SensorData", "Still ear")
//                                speechRecognition()
//                            }
//                            delay(100)
//                        }
//                    }
                } else {
                    if (lock.isHeld) lock.release()
//                    SpeechManager.stopRecognition()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        proximitySensor.stopListing()
    }
}

fun speechOderSo() {
    SpeechManager.startRecognition()
}