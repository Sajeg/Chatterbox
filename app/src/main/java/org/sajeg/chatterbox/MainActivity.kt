package org.sajeg.chatterbox

import android.content.Context
import android.hardware.Sensor
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.ai.client.generativeai.type.asTextOrNull
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
                Surface(Modifier.fillMaxSize()) {
                    MainComposable()
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

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun MainComposable() {
        // The state of the different Buttons on Screen
        // First row from left to right
        var languageSelectorActivated: Boolean by remember { mutableStateOf(false) }
        var speakerChecked: Boolean by remember { mutableStateOf(Config.speaker) }
        var subtitlesActivated: Boolean by remember { mutableStateOf(Config.subtitles) }
        // Second row from left to right
        var button1: Boolean by remember { mutableStateOf(false) }
        var gladosMode: Boolean by remember { mutableStateOf(Config.gladosMode) }
        var button2: Boolean by remember { mutableStateOf(Config.button2) }
        // Other important variables
        var language: String by remember { mutableStateOf(Config.language) }

        Scaffold { innerPadding ->
            val contentModifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
            Row(
                modifier = contentModifier
                    .fillMaxWidth()
                    .fillMaxSize()
                    .padding(horizontal = 30.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Picture
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                }
                // Action Buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 15.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FilledIconToggleButton(
                            modifier = Modifier.size(72.dp),
                            checked = languageSelectorActivated,
                            onCheckedChange = {
                                languageSelectorActivated = it
                                if (it) subtitlesActivated = false
                            },
                            content = {
                                Icon(
                                    modifier = Modifier.size(48.dp),
                                    painter = painterResource(id = R.drawable.translate),
                                    contentDescription = stringResource(R.string.desc_lang)
                                )
                            }
                        )
                        FilledIconToggleButton(
                            modifier = Modifier.size(72.dp),
                            checked = speakerChecked,
                            onCheckedChange = { speakerChecked = it; Config.speaker = it },
                            content = {
                                Icon(
                                    modifier = Modifier.size(48.dp),
                                    painter = painterResource(id = R.drawable.speaker),
                                    contentDescription = stringResource(R.string.dec_vol)
                                )
                            }
                        )
                        FilledIconToggleButton(
                            modifier = Modifier.size(72.dp),
                            checked = subtitlesActivated,
                            onCheckedChange = {
                                subtitlesActivated = it; Config.subtitles = it
                                if (it) languageSelectorActivated = false
                            },
                            content = {
                                Icon(
                                    modifier = Modifier.size(48.dp),
                                    painter = painterResource(id = R.drawable.subtitles),
                                    contentDescription = stringResource(R.string.desc_subtitles)
                                )
                            }
                        )
                    }
                    AnimatedVisibility(languageSelectorActivated) {
                        LazyColumn(
                            modifier = Modifier.height(160.dp)
                        ) {
                            item {
                                ListItem(
                                    leadingContent = {
                                        RadioButton(
                                            selected = language == "en-US",
                                            onClick = {
                                                Config.language = "en-US"; language = "en-US"
                                            })
                                    },
                                    headlineContent = { Text(text = stringResource(R.string.english)) },
                                    colors = ListItemDefaults.colors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                )
                            }
                            item {
                                ListItem(
                                    leadingContent = {
                                        RadioButton(
                                            selected = language == "de-DE",
                                            onClick = {
                                                Config.language = "de-DE"; language = "de-DE"
                                            })
                                    },
                                    headlineContent = { Text(text = stringResource(R.string.german)) },
                                    colors = ListItemDefaults.colors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                )
                            }
                        }
                    }
                    AnimatedVisibility(subtitlesActivated) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            item {
                                for (item in LLMManager.chat.history) {
                                    var text = item.parts[0].asTextOrNull().toString().trim()
                                    var cardColor = CardDefaults.cardColors()

                                    if (item.role == "user") {
                                        text = text
                                            .replace("[", "")
                                            .replace("]", "")
                                        cardColor = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(5.dp),
                                        colors = cardColor
                                    ) {
                                        Text(
                                            text = text,
                                            modifier = Modifier
                                                .padding(5.dp)
                                                .fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .padding(top = 15.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FilledIconToggleButton(
                            modifier = Modifier.size(72.dp),
                            checked = button1,
                            onCheckedChange = { button1 = it; Config.button1 = it },
                            content = {
                                Icon(
                                    modifier = Modifier.size(48.dp),
                                    painter = painterResource(id = R.drawable.translate),
                                    contentDescription = "temp desc"
                                )
                            }
                        )
                        FilledIconToggleButton(
                            modifier = Modifier.size(72.dp),
                            checked = gladosMode,
                            onCheckedChange = { gladosMode = it; Config.gladosMode = it; },
                            colors = IconButtonDefaults.iconToggleButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.error,
                                checkedContainerColor = MaterialTheme.colorScheme.error,
                                checkedContentColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            content = {
                                Icon(
                                    modifier = Modifier.size(48.dp),
                                    painter = painterResource(id = R.drawable.warning),
                                    contentDescription = stringResource(R.string.desc_glados)
                                )
                            }
                        )
                        FilledIconToggleButton(
                            modifier = Modifier.size(72.dp),
                            checked = button2,
                            onCheckedChange = { button2 = it; Config.button2 = it },
                            content = {
                                Icon(
                                    modifier = Modifier.size(48.dp),
                                    painter = painterResource(id = R.drawable.translate),
                                    contentDescription = "temp desc"
                                )
                            }
                        )
                    }
                }
                // Call button

            }
        }
    }
}