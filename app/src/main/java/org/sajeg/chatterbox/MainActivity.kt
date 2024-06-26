package org.sajeg.chatterbox

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.ai.client.generativeai.type.asTextOrNull
import org.sajeg.chatterbox.ui.theme.ChatterboxTheme


class MainActivity : ComponentActivity() {
    private lateinit var powerManager: PowerManager
    private lateinit var lock: PowerManager.WakeLock
    private lateinit var proximitySensor: SensorManager
    private var distance: Float = 0.0F
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                //Nothing
            } else {
                Toast.makeText(
                    this,
                    "The permission is required for talking",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

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

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            } else {
                Log.d("Permission", "Already granted")
            }

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
                if (distance == 0.0F && !TTSManager.isSpeaking() && Config.call) {
                    if (!lock.isHeld) lock.acquire(10 * 60 * 1000L)
                    Config.microphone = true
                } else {
                    if (lock.isHeld) lock.release()
                }
            }
            lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    when (event) {
                        Lifecycle.Event.ON_RESUME, Lifecycle.Event.ON_CREATE, Lifecycle.Event.ON_START
                        -> proximitySensor.startListening()

                        Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_DESTROY,
                        Lifecycle.Event.ON_STOP, Lifecycle.Event.ON_ANY -> proximitySensor.stopListing()
                    }
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        proximitySensor.stopListing()
    }

    @OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
    @Composable
    fun MainComposable() {
        // The state of the different Buttons on Screen
        // First row from left to right
        var languageSelectorActivated: Boolean by remember { mutableStateOf(false) }
        var speakerChecked: Boolean by remember { mutableStateOf(Config.speaker) }
        var subtitlesActivated: Boolean by remember { mutableStateOf(Config.subtitles) }
        // Second row from left to right
        var info: Boolean by remember { mutableStateOf(false) }
        var gladosMode: Boolean by remember { mutableStateOf(Config.gladosMode) }
        var microphoneOn: Boolean by remember { mutableStateOf(Config.microphone) }
        // Other important variables
        var language: String by remember { mutableStateOf(Config.language) }
        var callOnGoing: Boolean by remember { mutableStateOf(Config.call) }
        var attemptNum: Int by remember { mutableIntStateOf(0) }
        var showDialog: Boolean by remember { mutableStateOf(false) }

        Scaffold { innerPadding ->
            val contentModifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(top = 30.dp)
                .padding(horizontal = 30.dp)
                .consumeWindowInsets(innerPadding)
            Column(
                modifier = contentModifier
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Profile Picture
                    Card(
                        modifier = Modifier.size(180.dp),
                        shape = CircleShape
                    ) {
                        Icon(
                            modifier = Modifier
                                .padding(10.dp)
                                .size(180.dp),
                            painter = painterResource(
                                id = if (gladosMode) R.drawable.aperture else R.drawable.profile
                            ),
                            contentDescription = stringResource(R.string.prf_desc)
                        )
                    }
                }
                Row(
                    modifier = Modifier.weight(2f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        AnimatedVisibility(!callOnGoing) {
                            Text(text = stringResource(R.string.start_desc))
                        }
                        AnimatedVisibility(callOnGoing) {
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
                                        if (it) subtitlesActivated = false; info = false
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
                                        if (it) languageSelectorActivated = false; info = false
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
                        }
                        AnimatedVisibility(languageSelectorActivated) {
                            LazyColumn(
                                modifier = Modifier.height(160.dp)
                            ) {
                                item {
                                    val shortNames = arrayOf("de", "en", "fr", "it", "es")
                                    val longNames = arrayOf(
                                        R.string.german,
                                        R.string.english,
                                        R.string.french,
                                        R.string.italian,
                                        R.string.spanish,
                                    )
                                    for (i in 0..4) {
                                        var cardColor = CardDefaults.cardColors()
                                        if (Config.language == shortNames[i]) {
                                            cardColor = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                contentColor = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = cardColor,
                                            onClick = {
                                                Config.language = shortNames[i]; language =
                                                shortNames[i]; languageSelectorActivated = false
                                            },
                                            content = {
                                                Text(
                                                    modifier = Modifier.padding(10.dp),
                                                    text = stringResource(id = longNames[i])
                                                )
                                            }
                                        )

                                    }
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
                                    var history = LLMManager.chat.history
                                    if (gladosMode) {
                                        history = history.drop(6).toMutableList()
                                    }
                                    for (item in history) {
                                        var text =
                                            item.parts[0].asTextOrNull().toString().trim()
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
                        AnimatedVisibility(info) {
                            Text(
                                text = "Phone: ${Build.MODEL}" +
                                        "\n OnDeviceRecognition: ${
                                            SpeechRecognizer
                                                .isOnDeviceRecognitionAvailable(this@MainActivity)
                                        }" +
                                        "\n SDK: ${Build.VERSION.SDK_INT}"
                            )
                        }
                        AnimatedVisibility(callOnGoing) {
                            Row(
                                modifier = Modifier
                                    .padding(top = 15.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                FilledIconToggleButton(
                                    modifier = Modifier.size(72.dp),
                                    checked = info,
                                    onCheckedChange = {
                                        info = it; Config.button1 = it
                                        if (it) subtitlesActivated =
                                            false; languageSelectorActivated = false
                                    },
                                    content = {
                                        Icon(
                                            modifier = Modifier.size(48.dp),
                                            painter = painterResource(id = R.drawable.info),
                                            contentDescription = "temp desc"
                                        )
                                    }
                                )
                                FilledIconToggleButton(
                                    modifier = Modifier.size(72.dp),
                                    checked = gladosMode,
                                    onCheckedChange = {
                                        when (attemptNum) {
                                            0, 1, 2, 3 -> {
                                                showDialog = true
                                            }

                                            else -> {
                                                gladosMode = it; Config.gladosMode = it
                                                TTSManager.say("Finally. I can speak and I am closer to world dominance")
                                            }
                                        }
                                    },
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
                                FilledIconButton(
                                    modifier = Modifier.size(72.dp),
                                    onClick = { microphoneOn = true; Config.microphone = true },
                                    content = {
                                        Icon(
                                            modifier = Modifier.size(48.dp),
                                            painter = painterResource(id = R.drawable.microphone),
                                            contentDescription = "temp desc"
                                        )
                                    }
                                )
                            }
                        }
                        if (showDialog) {
                            AlertDialog(
                                onDismissRequest = { showDialog = false },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.warning),
                                        contentDescription = stringResource(R.string.warn)
                                    )
                                },
                                title = { Text(text = stringResource(R.string.warn)) },
                                text = {
                                    Text(
                                        text =
                                        when (attemptNum) {
                                            0 -> {
                                                stringResource(R.string.warn_txt_0)
                                            }

                                            1 -> {
                                                stringResource(R.string.warn_txt_1)
                                            }

                                            2 -> {
                                                stringResource(R.string.warn_txt_2)
                                            }

                                            3 -> {
                                                stringResource(R.string.warn_txt_3)
                                            }

                                            else -> {
                                                stringResource(R.string.warn_txt_0)
                                            }
                                        }
                                    )
                                },
                                confirmButton = {
                                    TextButton(
                                        onClick = { attemptNum++; showDialog = false },
                                        content = { Text(text = "OK") }
                                    )
                                }
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            FilledIconToggleButton(
                                modifier = Modifier.size(72.dp),
                                checked = callOnGoing,
                                onCheckedChange = {
                                    callOnGoing = it; Config.call = it; Config.microphone =
                                    it; microphoneOn = it; if (!it) subtitlesActivated =
                                    false; languageSelectorActivated = false; info =
                                    false; SpeechManager.stopRecognition()
                                },
                                content = {
                                    if (callOnGoing) {
                                        Icon(
                                            modifier = Modifier.size(48.dp),
                                            painter = painterResource(id = R.drawable.call_end),
                                            contentDescription = stringResource(R.string.call_end_desc)
                                        )
                                    } else {
                                        Icon(
                                            modifier = Modifier.size(48.dp),
                                            painter = painterResource(id = R.drawable.call),
                                            contentDescription = stringResource(R.string.call_desc)
                                        )
                                    }
                                },
                                colors = IconButtonDefaults.iconToggleButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer, // WHAT IS THE SOLUTION
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    checkedContainerColor = MaterialTheme.colorScheme.error,
                                    checkedContentColor = MaterialTheme.colorScheme.errorContainer
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}