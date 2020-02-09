package com.vaibhavdhunde.android.practice.prochat.data

object UserField {
    const val NAME = "name"
    const val BIO = "bio"
    const val PROFILE_PICTURE_PATH = "profilePicturePath"
    const val REGISTRATION_TOKENS = "registrationTokens"
}

data class User(
    val name: String,
    val bio: String,
    val profilePicturePath: String?,
    val registrationTokens: MutableList<String>
) {
    constructor() : this("", "", null, mutableListOf())
}