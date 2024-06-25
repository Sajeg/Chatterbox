package org.sajeg.chatterbox

object Config {
    var language: String = "de-DE"
    var speaker: Boolean = false
    var subtitles: Boolean = false
    var button1: Boolean = false
    var gladosMode: Boolean = false
        set(value) { field = value; LLMManager.initialize()}
    var button2: Boolean = false
}