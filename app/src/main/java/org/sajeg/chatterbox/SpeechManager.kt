package org.sajeg.chatterbox

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

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
        if(Config.gladosMode){
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        } else {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Config.language)
        }
        stt.setRecognitionListener(listener)
        stt.startListening(intent)
    }

//    fun stopRecognition() {
//        stt.stopListening()
//    }
}

