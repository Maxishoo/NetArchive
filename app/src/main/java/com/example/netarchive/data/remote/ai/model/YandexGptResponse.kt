package com.example.netarchive.data.remote.ai.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YandexGptResponse(
    @SerialName("result") val result: Result?
)

@Serializable
data class Result(
    @SerialName("alternatives") val alternatives: List<Alternative>,
    @SerialName("usage") val usage: Usage? = null
)

@Serializable
data class Alternative(
    @SerialName("message") val message: Message
)


@Serializable
data class Usage(
    @SerialName("inputTextCharacters") val inputTextCharacters: Int? = null,
    @SerialName("completionTokens") val completionTokens: Int? = null,
    @SerialName("totalTokens") val totalTokens: Int? = null
)