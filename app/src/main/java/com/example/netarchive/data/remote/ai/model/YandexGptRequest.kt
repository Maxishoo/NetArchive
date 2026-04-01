package com.example.netarchive.data.remote.ai.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YandexGptRequest(
    @SerialName("modelUri") val modelUri: String,
    @SerialName("completionOptions") val completionOptions: CompletionOptions,
    @SerialName("messages") val messages: List<Message>
)

@Serializable
data class CompletionOptions(
    @SerialName("stream") val stream: Boolean = false,
    @SerialName("temperature") val temperature: Float = 0.7f,
    @SerialName("maxTokens") val maxTokens: String = "1000"
)
@Serializable
data class Message(
    @SerialName("role") val role: String,
    @SerialName("text") val text: String
)
