package org.sajeg.chatterbox

object Config {
    var language: String
        get() = "de-DE"
        set(value) = tmp()
    var speaker: Boolean
        get() = false
        set(value) =  tmp() //TTSManager.initialize(context = this)
    var subtitles: Boolean
        get() = false
        set(value) = tmp()
    var button1: Boolean
        get() = false
        set(value) = tmp()
    var gladosMode: Boolean
        get() = false
        set(value) = LLMManager.initialize()
    var button2: Boolean
        get() = false
        set(value) = tmp()


    fun tmp() {

    }
}