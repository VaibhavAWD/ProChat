package com.vaibhavdhunde.android.practice.prochat.util

import android.content.Context
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.vaibhavdhunde.android.practice.prochat.data.*
import com.vaibhavdhunde.android.practice.prochat.ui.chat.DateItem
import com.vaibhavdhunde.android.practice.prochat.ui.chat.ImageMessageItem
import com.vaibhavdhunde.android.practice.prochat.ui.chat.TextMessageItem
import com.vaibhavdhunde.android.practice.prochat.ui.people.PersonItem
import com.xwray.groupie.kotlinandroidextensions.Item
import java.util.*

object FirestoreUtil {

    private object Collections {
        const val USERS = "users"
        const val CHAT_CHANNELS = "chatChannels"
        const val MESSAGES = "messages"
        const val ENGAGED_CHAT_CHANNELS = "engagedChatChannels"
    }

    val currentUserId: String? = FirebaseAuth.getInstance().uid

    private val firestoreInstance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val chatChannelsCollectionRef =
        firestoreInstance.collection(Collections.CHAT_CHANNELS)

    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document(
            "${Collections.USERS}/${currentUserId
                ?: throw java.lang.NullPointerException("UID is null")}"
        )

    fun initCurrentUserIfFirstTime(onComplete: () -> Unit) {
        currentUserDocRef.get().addOnSuccessListener { user ->
            if (!user.exists()) {
                val newUser = User(
                    FirebaseAuth.getInstance().currentUser?.displayName ?: "", "", null,
                    mutableListOf()
                )
                currentUserDocRef.set(newUser).addOnSuccessListener {
                    onComplete()
                }
            } else {
                onComplete()
            }
        }
    }

    fun updateCurrentUser(
        name: String = "", bio: String = "", profilePicturePath: String? = null,
        onComplete: (message: String) -> Unit
    ) {
        val userFieldMap = mutableMapOf<String, Any>()
        if (name.isNotBlank()) {
            userFieldMap[UserField.NAME] = name
        }
        if (bio.isNotBlank()) {
            userFieldMap[UserField.BIO] = bio
        }
        if (profilePicturePath != null) {
            userFieldMap[UserField.PROFILE_PICTURE_PATH] = profilePicturePath
        }
        currentUserDocRef.update(userFieldMap).addOnCompleteListener {
            if (it.isSuccessful) {
                onComplete("Profile Saved")
            } else {
                onComplete(it.exception?.message!!)
            }
        }
    }

    fun getCurrentUser(onComplete: (User) -> Unit) {
        currentUserDocRef.get().addOnSuccessListener { user ->
            val currentUser = user.toObject(User::class.java)
            if (currentUser != null) {
                onComplete(currentUser)
            }
        }
    }

    fun addUserListenerRegistration(context: Context, onListen: (List<Item>) -> Unit): ListenerRegistration {
        return firestoreInstance.collection(Collections.USERS)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("FIRESTORE", "Users listener error.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                val items = mutableListOf<Item>()
                querySnapshot?.documents?.forEach { user ->
                    if (user.id != currentUserId) { // don't add current user
                        val person = user.toObject(User::class.java)
                        val userId = user.id
                        if (person != null) {
                            items.add(PersonItem(person, userId, context))
                        }
                    }
                }
                onListen(items)
            }
    }

    fun removeListener(registration: ListenerRegistration) {
        registration.remove()
    }

    fun getOrCreateChatChannel(otherUserId: String, onComplete: (channelId: String) -> Unit) {
        currentUserDocRef.collection(Collections.ENGAGED_CHAT_CHANNELS)
            .document(otherUserId).get().addOnSuccessListener { engagedChatChannel ->
                if (engagedChatChannel.exists()) {
                    // if the engaged chat channel already exists then simply return the channel id
                    onComplete(engagedChatChannel[ChatChannelField.CHANNEL_ID] as String)
                    return@addOnSuccessListener
                }

                // if the current user id is null then don't execute any further operations
                if (currentUserId == null) {
                    return@addOnSuccessListener
                }

                // create new chat channel
                val newChannel = chatChannelsCollectionRef.document()
                newChannel.set(ChatChannel(mutableListOf(currentUserId, otherUserId)))

                // get new channel id
                val newChannelId = newChannel.id

                // add channelId to the engagedChatChannels of the current user
                currentUserDocRef.collection(Collections.ENGAGED_CHAT_CHANNELS)
                    .document(otherUserId)
                    .set(mapOf(ChatChannelField.CHANNEL_ID to newChannelId))

                // add channelId to the engagedChatChannels of the other user
                firestoreInstance.collection(Collections.USERS)
                    .document(otherUserId)
                    .collection(Collections.ENGAGED_CHAT_CHANNELS)
                    .document(currentUserId)
                    .set(mapOf("channelId" to newChannelId))
            }
    }

    fun addMessageListenerRegistration(
        channelId: String, context: Context,
        onListen: (List<Item>) -> Unit
    ): ListenerRegistration {
        return chatChannelsCollectionRef.document(channelId).collection(Collections.MESSAGES)
            .orderBy(MessageField.TIME)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e("FIRESTORE", "Messages error.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                var currentDate: Date? = null

                val items = mutableListOf<Item>()
                querySnapshot?.documents?.forEach { message ->
                    if (message == null) return@forEach

                    // show date if its new
                    val date: Date = getOnlyDate(message[MessageField.TIME] as Timestamp)
                    if (currentDate == null || currentDate != date) {
                        currentDate = date
                        items.add(DateItem(date))
                    }

                    if (message[MessageField.TYPE] == MessageType.TEXT) {
                        val textMessage = message.toObject(TextMessage::class.java)
                        if (textMessage != null) {
                            items.add(TextMessageItem(textMessage, context))
                        }
                    } else {
                        val imageMessage = message.toObject(ImageMessage::class.java)
                        if (imageMessage != null) {
                            items.add(ImageMessageItem(imageMessage, context))
                        }
                    }
                }
                onListen(items)
            }
    }

    private fun getOnlyDate(timestamp: Timestamp): Date {
        val tempDate = timestamp.toDate()
        val calendar = Calendar.getInstance()
        calendar.time = tempDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    fun sendMessage(message: Message, channelId: String) {
        chatChannelsCollectionRef.document(channelId)
            .collection(Collections.MESSAGES)
            .add(message)
    }

    // FCM

    fun setRegistrationTokens(tokens: MutableList<String>) {
        currentUserDocRef.update(mapOf(UserField.REGISTRATION_TOKENS to tokens))
    }

    fun getRegistrationTokens(onComplete: (tokens: MutableList<String>) -> Unit) {
        currentUserDocRef.get().addOnSuccessListener { user ->
            val currentUser = user.toObject(User::class.java)
            if (currentUser != null) {
                onComplete(currentUser.registrationTokens)
            }
        }
    }

    // end FCM
}