package com.example.firechat.models

data class User (
    var name: String,
    val email: String,
    val image: String,
    val token: String?
)