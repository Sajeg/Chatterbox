package org.sajeg.chatterbox

import android.content.Context
import android.media.AudioAttributes
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

object TTSManager {
    private lateinit var tts: TextToSpeech

    fun initialize(context: Context) {
        tts = TextToSpeech(context) { status ->
            if (status != TextToSpeech.SUCCESS) {
                Log.e("StorySmithTTS", "Error Initializing TTS engine")
            } else {
                val audioAttributes: AudioAttributes = if (Config.speaker) {
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                        .build()
                } else {
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                }
                tts.setOnUtteranceProgressListener(TTSProgress())
                tts.setAudioAttributes(audioAttributes)
                if (Config.gladosMode) {
                    tts.setLanguage(Locale.US)
                } else {
                    tts.setLanguage(Locale.GERMAN)
                }
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