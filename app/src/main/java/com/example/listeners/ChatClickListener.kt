package com.example.listeners

interface ChatClickListener {
    fun onChatClicked(name: String?, otherUserId: String?, chatImageUrl: String?, chatName: String?)
}