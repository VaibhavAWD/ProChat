package com.vaibhavdhunde.android.practice.prochat.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.lang.NullPointerException
import java.util.*

object StorageUtil {

    private val currentUserId: String? = FirebaseAuth.getInstance().uid

    private val storageInstance: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    private val currentUserRef: StorageReference
        get() = storageInstance.reference.child(currentUserId
            ?: throw NullPointerException("UID is null."))

    fun uploadProfileImage(imageBytes: ByteArray, onSuccess: (imagepath: String) -> Unit) {
        val ref = currentUserRef.child("profilePictures/${UUID.nameUUIDFromBytes(imageBytes)}")
        ref.putBytes(imageBytes).addOnSuccessListener {
            onSuccess(ref.path)
        }
    }

    fun uploadMessageImage(imageBytes: ByteArray, onSuccess: (imagepath: String) -> Unit) {
        val ref = currentUserRef.child("messages/${UUID.nameUUIDFromBytes(imageBytes)}")
        ref.putBytes(imageBytes).addOnSuccessListener {
            onSuccess(ref.path)
        }
    }

    fun pathToReference(path: String) = storageInstance.getReference(path)
}