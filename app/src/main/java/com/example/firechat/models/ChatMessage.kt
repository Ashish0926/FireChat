package com.example.firechat.models

import java.util.*

data class ChatMessage (
    val senderId: String,
    val receiverId: String,
    var message: String,
    val dateTime: String?,
    var dateObject: Date,
    var conversionId: String? = null,
    var conversionName: String? = null,
    var conversionImage: String? = null
)