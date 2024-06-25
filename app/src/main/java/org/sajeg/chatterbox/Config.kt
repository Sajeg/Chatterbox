package org.sajeg.chatterbox

object Config {
    var call: Boolean = false
        set(value) { field = value; if (value) SpeechManager.startRecognition()}
    var language: String = "de-DE"
    var speaker: Boolean = false
    var subtitles: Boolean = false
    var button1: Boolean = false
    var gladosMode: Boolean = false
        set(value) { field = value; LLMManager.initialize()}
    var microphone: Boolean = false
        set(value) { field = value; if(value) SpeechManager.startRecognition() }
}