package com.example.warofwonders.chat

data class ChatMessage(
    val user: String,
    val role: String,
    val message: String
)