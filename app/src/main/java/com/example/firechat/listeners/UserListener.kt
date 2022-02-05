package com.example.firechat.listeners

import com.example.firechat.models.User

interface UserListener {
    fun onUserClicked(user: User)
}