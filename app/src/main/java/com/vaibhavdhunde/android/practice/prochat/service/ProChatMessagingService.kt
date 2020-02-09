package com.vaibhavdhunde.android.practice.prochat.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vaibhavdhunde.android.practice.prochat.util.FirestoreUtil
import java.lang.NullPointerException

class ProChatMessagingService : FirebaseMessagingService() {

    override fun onNewToken(newRegistrationToken: String?) {
        if (FirebaseAuth.getInstance().currentUser != null) {
            addRegistrationToken(newRegistrationToken)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        if (remoteMessage?.notification != null) {
            // TODO: Show notification
        }
    }

    companion object {
        fun addRegistrationToken(newRegistrationToken: String?) {
            if (newRegistrationToken == null) {
                throw NullPointerException("addRegistrationToken() was called but Registration token is null.")
            }

            FirestoreUtil.getRegistrationTokens { tokens ->
                if (tokens.contains(newRegistrationToken)) {
                    return@getRegistrationTokens
                }

                tokens.add(newRegistrationToken)
                FirestoreUtil.setRegistrationTokens(tokens)
            }
        }

        fun getRegistrationToken(onComplete: (token: String) -> Unit) {
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { result ->
                val token = result.token
                onComplete(token)
            }
        }
    }
}