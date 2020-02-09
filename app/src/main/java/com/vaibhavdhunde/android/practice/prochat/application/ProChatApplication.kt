package com.vaibhavdhunde.android.practice.prochat.application

import android.app.Application
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider

class ProChatApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // This line needs to be executed before any usage of EmojiTextView, EmojiEditText or EmojiButton.
        EmojiManager.install(GoogleEmojiProvider())
    }
}