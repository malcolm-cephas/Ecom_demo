package com.malcolm.ecomapp.api

import retrofit2.http.Body
import retrofit2.http.POST

data class ChatRequest(
    val message: String, 
    val model: String? = null, 
    val conversationId: String? = null
)

data class ChatResponse(
    val response: String, 
    val status: String
)

interface ChatApi {
    @POST("api/ai/chat")
    suspend fun chat(@Body request: ChatRequest): ChatResponse
}
