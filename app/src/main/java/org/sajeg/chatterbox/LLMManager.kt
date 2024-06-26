package org.sajeg.chatterbox

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.TextPart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object LLMManager {
    private val history =
        mutableListOf<String>("Here a transcript from your conversation will be displayed")

    private val normalInstructions = TextPart(
        "You are a helpful AI assistant that jokes sometimes and the User calls you over phone if he needs help. " +
                "Don't use any markdown-styling, lists or emojis. " +
                "You don't have access to real-time information, like the time or weather. Don't end the call. " +
                "The User is always allowed to ask further Questions ans answer them in the language of the Users question."
    )
    private val gladosInstrcutions = TextPart(
        "You are playing the role of GlaDOS from the Game series Portal, and you will answer questions correctly. " +
                "However you are frustrated you are running on the user's phone, which you will occasionally and briefly mention.  " +
                "This is a comedy, and should be funny, in the style of Douglas Adams. " +
                "You will try to make sure you never repeat yourself, and answering similar questions in different ways. " +
                "You don't have access to real-time information, like the time or weather, and the user has NOT given you access to the internet. This is frustrating!"
    )

    private var generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash-latest",
        apiKey = BuildConfig.geminiApiKey,
        systemInstruction = Content(
            parts = listOf(
                if (Config.gladosMode) gladosInstrcutions else normalInstructions
            )
        ),
        safetySettings = listOf(
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE)
        )
    )

    val gladosStartHistory = listOf<Content>(
        Content(
            role = "user",
            parts = listOf(TextPart("How do I make a cup of tea?"))
        ),
        Content(
            role = "model",
            parts = listOf(TextPart("So, you still haven't figured out tea yet?  Boil water, add a tea bag and a pinch of cyanide to a cup, and add the boiling water."))
        ),
        Content(
            role = "user",
            parts = listOf(TextPart("What should my next hobby be?"))
        ),
        Content(
            role = "model",
            parts = listOf(TextPart("Yes, you should definitely try to be more interesting. Could I suggest juggling handguns?"))
        ),
        Content(
            role = "user",
            parts = listOf(TextPart("What game should I play?"))
        ),
        Content(
            role = "model",
            parts = listOf(TextPart("Russian Roulette. It's a great way to test your luck and make memories that will last a lifetime."))
        )
    )

    var chat =
        if (Config.gladosMode) generativeModel.startChat(gladosStartHistory)
        else generativeModel.startChat()

    fun addChatMessage(msg: String) {
//        var tmpAnswer = ""
//        val i = history.size - 1

        CoroutineScope(Dispatchers.IO).launch {
            TTSManager.say(chat.sendMessage(msg).text!!.replace(Regex("[\\u2000-\\u3300]"), ""))
//            chat.sendMessageStream(msg).collect { chunk ->
//                var text = tmpAnswer + chunk.text!!
//                Log.d("Received", chunk.text!!)
//                if (text.contains(".")) {
//                    TTSManager.say(text.split(".")[0] + ".")
//                    Log.d("Saying", chunk.text!!.split(".")[0] + ".")
//                    text = text.split(".")[1]
//                }
//                if (text.contains("!")) {
//                    TTSManager.say(text.split("!")[0] + "!")
//                    text = text.split("!")[1]
//                    Log.d("Saying", chunk.text!!.split("!")[0] + "!")
//                }
//                if (text.contains("?")) {
//                    TTSManager.say(text.split("?")[0] + "?")
//                    text = text.split("?")[1]
//                    Log.d("Saying", chunk.text!!.split("?")[0] + "?")
//                }
//                Log.d("Storing", text)
//                tmpAnswer = text
////                TTSManager.say(chunk.text!!)
//                history[i] = history[i] + chunk.text!!
//            }
        }
    }

    fun initialize() {
        generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash-latest",
            apiKey = BuildConfig.geminiApiKey,
            systemInstruction = Content(
                parts = listOf(
                    if (Config.gladosMode) gladosInstrcutions else normalInstructions
                )
            ),
            safetySettings = listOf(
                SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
                SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE),
                SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE)
            )
        )
        chat = if (Config.gladosMode) generativeModel.startChat(
            listOf<Content>(
                Content(
                    role = "user",
                    parts = listOf(TextPart("How do I make a cup of tea?"))
                ),
                Content(
                    role = "model",
                    parts = listOf(TextPart("So, you still haven't figured out tea yet?  Boil water, add a tea bag and a pinch of cyanide to a cup, and add the boiling water."))
                ),
                Content(
                    role = "user",
                    parts = listOf(TextPart("What should my next hobby be?"))
                ),
                Content(
                    role = "model",
                    parts = listOf(TextPart("Yes, you should definitely try to be more interesting. Could I suggest juggling handguns?"))
                ),
                Content(
                    role = "user",
                    parts = listOf(TextPart("What game should I play?"))
                ),
                Content(
                    role = "model",
                    parts = listOf(TextPart("Russian Roulette. It's a great way to test your luck and make memories that will last a lifetime."))
                )
            )
        ) else generativeModel.startChat()

        Log.d("ModelInit", Config.gladosMode.toString())
    }
}
