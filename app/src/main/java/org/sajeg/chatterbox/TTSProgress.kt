package org.sajeg.chatterbox

import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TTSProgress : UtteranceProgressListener() {
    override fun onStart(utteranceId: String?) {
    }

    override fun onDone(utteranceId: String?) {
        if (!TTSManager.isSpeaking()) {
            Log.d("TTSProgress", "Finished")
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
                    SpeechManager.startRecognition()
                }
            }
        }

//        SpeechManager.startRecognition()
    }

    @Deprecated("Deprecated in Java")
    override fun onError(utteranceId: String?) {
    }

}