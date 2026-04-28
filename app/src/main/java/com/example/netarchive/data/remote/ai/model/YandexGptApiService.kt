package com.example.netarchive.data.remote.ai.model

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface YandexGptApiService {

    @Headers("Content-Type: application/json")
    @POST("completion")
    suspend fun generateCompletion(
        @Header("Authorization") apiKey: String,
        @Header("x-folder-id") folderId: String,
        @Body request: YandexGptRequest
    ): YandexGptResponse
}