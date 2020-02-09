package com.vaibhavdhunde.android.practice.prochat.data

import java.util.*

object MessageType {
    const val TEXT = "TEXT"
    const val IMAGE = "IMAGE"
}

object MessageField {
    const val MESSAGE = "message"
    const val TIME = "time"
    const val SENDER_ID = "senderId"
    const val RECIPIENT_ID = "recipientId"
    const val SENDER_NAME = "senderName"
    const val TYPE = "type"
}

interface Message {
    val time: Date
    val senderId: String
    val recipientId: String
    val senderName: String
    val type: String
}