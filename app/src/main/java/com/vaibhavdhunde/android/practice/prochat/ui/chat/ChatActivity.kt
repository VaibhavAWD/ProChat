package com.vaibhavdhunde.android.practice.prochat.ui.chat

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ListenerRegistration
import com.vaibhavdhunde.android.practice.prochat.R
import com.vaibhavdhunde.android.practice.prochat.data.ImageMessage
import com.vaibhavdhunde.android.practice.prochat.data.TextMessage
import com.vaibhavdhunde.android.practice.prochat.data.User
import com.vaibhavdhunde.android.practice.prochat.ui.chat.imgfullsrcn.FullscreenImageActivity
import com.vaibhavdhunde.android.practice.prochat.util.*
import com.vanniktech.emoji.EmojiPopup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_chat.*
import org.jetbrains.anko.startActivity
import java.io.ByteArrayOutputStream
import java.util.*

@Suppress("DEPRECATION")
class ChatActivity : AppCompatActivity() {

    companion object {
        private const val PICK_IMG_RC = 8792
    }

    private lateinit var messagesListenerRegistration: ListenerRegistration

    private var shouldInitListMessages = true

    private lateinit var messageSection: Section

    private lateinit var currentChannelId: String

    private lateinit var currentUser: User

    private lateinit var otherUserId: String

    private lateinit var emojiPopup: EmojiPopup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // init emoji popup
        emojiPopup = EmojiPopup.Builder.fromRootView(chat_activity).build(input_message)

        FirestoreUtil.getCurrentUser { user ->
            currentUser = user
        }

        // get username from intent and set as activity title
        val username = intent.getStringExtra(USERNAME)
        title = username

        otherUserId = intent.getStringExtra(USER_ID)!!

        FirestoreUtil.getOrCreateChatChannel(otherUserId) { channelId ->
            currentChannelId = channelId
            messagesListenerRegistration = FirestoreUtil.addMessageListenerRegistration(
                channelId, this, this::updateListMessages)

            // send text message
            btn_send_msg.setOnClickListener {
                sendTextMessageToUser()
            }

            // send image message
            fab_send_img.setOnClickListener {
                pickImage()
            }
        }

        fab_scroll_down.setOnClickListener {
            list_messages.smoothScrollToPosition(list_messages.adapter?.itemCount!! - 1)
        }

        list_messages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // if the list is being scrolled UP then only show the fab to scroll DOWN
                if (dy < 0) {
                    fab_scroll_down.show()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                // if the list reaches the end then HIDE the fab
                if (!recyclerView.canScrollVertically(1)) {
                    fab_scroll_down.hide()
                }
            }
        })

        // toggle emoji keyboard
        fab_emoji.setOnClickListener {
            emojiPopup.toggle()
        }
    }

    override fun onStop() {
        super.onStop()

        if (::emojiPopup.isInitialized && emojiPopup.isShowing) {
            emojiPopup.dismiss()
        }
    }

    private fun sendTextMessageToUser() {
        val messageToSend = input_message.text.toString().trim()
        if (messageToSend.isBlank()) return
        val time = Calendar.getInstance().time
        val senderId = FirestoreUtil.currentUserId ?: return
        val message = TextMessage(messageToSend, time, senderId, otherUserId, currentUser.name)
        FirestoreUtil.sendMessage(message, currentChannelId)

        // reset message
        input_message.setText("")
    }

    private fun pickImage() {
        val intent = Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = ImageType.ANY
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(ImageType.JPEG, ImageType.PNG))
            }
        }
        startActivityForResult(Intent.createChooser(intent,
            getString(R.string.select_image)), PICK_IMG_RC)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMG_RC && resultCode == Activity.RESULT_OK &&
            data != null && data.data != null
        ) {
            val selectedImagePath = data.data!!
            val selectedImageBytes = getSelectedImageBytes(selectedImagePath)

            StorageUtil.uploadMessageImage(selectedImageBytes) { imagePath ->
                val time = Calendar.getInstance().time
                val senderId = FirestoreUtil.currentUserId ?: return@uploadMessageImage
                val imageMessageToSend = ImageMessage(imagePath, time, senderId, otherUserId,
                    currentUser.name)
                FirestoreUtil.sendMessage(imageMessageToSend, currentChannelId)
            }
        }
    }

    private fun getSelectedImageBytes(selectedImagePath: Uri): ByteArray {
        val selectedImageBitmap = MediaStore.Images.Media.getBitmap(
            contentResolver, selectedImagePath)
        val outputStream = ByteArrayOutputStream()
        selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        return outputStream.toByteArray()
    }

    private fun updateListMessages(messages: List<Item>) {
        fun init() {
            list_messages.apply {
                adapter = GroupAdapter<ViewHolder>().apply {
                    messageSection = Section(messages)
                    add(messageSection)
                    setOnItemClickListener(onItemClick)
                }
            }
            shouldInitListMessages = false
        }

        fun updateItems() {
            messageSection.update(messages)
        }

        if (shouldInitListMessages) {
            init()
        } else {
            updateItems()
        }

        list_messages.scrollToPosition(list_messages.adapter?.itemCount!! - 1)
    }

    private val onItemClick = OnItemClickListener { item, _ ->
        if (item is ImageMessageItem) {
            startActivity<FullscreenImageActivity>(
                IMAGE_PATH to item.message.imagePath
            )
        }
    }
}
