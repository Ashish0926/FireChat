package com.example.firechat.listeners

import com.example.firechat.models.User

interface ConversionListener {
    fun onConversionClicked(user: User)
}