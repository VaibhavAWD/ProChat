package com.vaibhavdhunde.android.practice.prochat.data

object ChatChannelField {
    const val CHANNEL_ID = "channelId"
}

data class ChatChannel(
    val userIds: MutableList<String>
) {
    constructor() : this(mutableListOf())
}