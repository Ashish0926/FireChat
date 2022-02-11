package com.example.firechat.models

import java.io.Serializable

data class User (
    var name: String,
    val email: String?,
    val image: String,
    val token: String?,
    val id: String
): Serializable