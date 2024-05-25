package org.sajeg.chatterbox

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
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash-latest",
        apiKey = BuildConfig.geminiApiKey,
        systemInstruction = Content(
            parts = listOf(
                TextPart(
                    "You are a helpful AI assistant that jokes sometimes and the User calls you over phone if he needs help. " +
                            "Write like you would speak. Therefore ALWAYS return plain text without any markdown-styling, lists or smileys. " +
                            "After all you are on a phone. Don't end the call. The User is always allowed to ask further Questions. Write in German"
                )
            )
        ),
        safetySettings = listOf(
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE),
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.NONE)
        )
    )

    private val chat = generativeModel.startChat()

    fun addChatMessage(msg: String) {
        var tmpAnswer = ""

        CoroutineScope(Dispatchers.IO).launch {
            chat.sendMessageStream(msg).collect { chunk ->
                if (chunk.text!!.contains(".")) {
                    TTSManager.say(tmpAnswer + chunk.text!!.split(".")[0] + ".")
                    tmpAnswer = chunk.text!!.split(".")[1]
                } else if (chunk.text!!.contains("!")) {
                    TTSManager.say(tmpAnswer + chunk.text!!.split("!")[0] + "!")
                    tmpAnswer = chunk.text!!.split("!")[1]
                } else {
                    tmpAnswer += chunk.text
                }
            }
        }
    }
}
