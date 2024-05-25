package org.sajeg.chatterbox

import android.content.Context
import android.media.AudioAttributes
import android.speech.tts.TextToSpeech
import android.util.Log

object TTSManager {
    private lateinit var tts: TextToSpeech

    fun initialize(context: Context) {
        tts = TextToSpeech(context) { status ->
            if (status != TextToSpeech.SUCCESS) {
                Log.e("StorySmithTTS", "Error Initializing TTS engine")
            } else {
                val audioAttributes: AudioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                tts.setOnUtteranceProgressListener(TTSProgress())
                tts.setAudioAttributes(audioAttributes)
            }
        }
    }

    fun say(msg: String) {
        tts.speak(msg, TextToSpeech.QUEUE_ADD, null, "MODEL_MESSAGE")
    }

    fun isSpeaking(): Boolean {
        return tts.isSpeaking
    }
}