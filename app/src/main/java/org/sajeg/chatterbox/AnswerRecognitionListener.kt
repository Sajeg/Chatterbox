package org.sajeg.chatterbox

import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.annotation.RequiresApi

class AnswerRecognitionListener : RecognitionListener {

    override fun onReadyForSpeech(params: Bundle?) {
        // Called when the recognizer is ready to start listening
    }

    override fun onBeginningOfSpeech() {
        // Called when speech begins
    }

    override fun onRmsChanged(rmsdB: Float) {
        // Called when the RMS (root mean square) of the audio changes
    }

    override fun onBufferReceived(buffer: ByteArray?) {
        // Called when a buffer of audio data is received
    }

    override fun onEndOfSpeech() {
        // Called when speech ends
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onError(error: Int) {
        // Called when an error occurs
        Log.e("RecognitionListener", "Error Recognizing Speech. ERROR CODE: $error")
        if (error == 13 && Build.VERSION.SDK_INT >= 33) {
            SpeechManager.downloadModel()
        }
        Config.microphone = false
    }

    override fun onResults(results: Bundle?) {
        // Called when a set of speech recognition results is available
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            LLMManager.addChatMessage(matches[0].toString())
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        // Called when partial results are available
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        // Called when a non-recognition event occurs
    }

}