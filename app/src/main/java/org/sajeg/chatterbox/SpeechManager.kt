package org.sajeg.chatterbox

import android.content.Context
import android.content.Intent
import android.os.Build
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.annotation.RequiresApi

object SpeechManager {
    private lateinit var stt: SpeechRecognizer

    fun initialize(context: Context) {
        stt = if (SpeechRecognizer.isOnDeviceRecognitionAvailable(context)){
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }
    }

    fun startRecognition() {
        val listener = AnswerRecognitionListener()
        val intent = Intent(RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        if(Config.gladosMode || Config.language == "en"){
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        } else {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                "${Config.language.lowercase()}-${Config.language.uppercase()}")
        }
        stt.setRecognitionListener(listener)
        stt.startListening(intent)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun downloadModel() {
        val intent = Intent(RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        if(Config.gladosMode || Config.language == "en"){
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        } else {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                "${Config.language.lowercase()}-${Config.language.uppercase()}")
        }
        stt.triggerModelDownload(intent)
    }

//    fun stopRecognition() {
//        stt.stopListening()
//    }
}

