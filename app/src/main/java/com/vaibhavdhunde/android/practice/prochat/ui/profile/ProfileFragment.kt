package com.vaibhavdhunde.android.practice.prochat.ui.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.vaibhavdhunde.android.practice.prochat.R
import com.vaibhavdhunde.android.practice.prochat.data.User
import com.vaibhavdhunde.android.practice.prochat.ui.auth.SignInActivity
import com.vaibhavdhunde.android.practice.prochat.util.FirestoreUtil
import com.vaibhavdhunde.android.practice.prochat.util.GlideApp
import com.vaibhavdhunde.android.practice.prochat.util.ImageType
import com.vaibhavdhunde.android.practice.prochat.util.StorageUtil
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.newTask
import org.jetbrains.anko.support.v4.indeterminateProgressDialog
import org.jetbrains.anko.support.v4.intentFor
import java.io.ByteArrayOutputStream

@Suppress("DEPRECATION")
class ProfileFragment : Fragment() {

    companion object {
        private const val PICK_IMG_RC = 8792
    }

    private lateinit var selectedImageBytes: ByteArray

    private var pictureJustChanged = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        setHasOptionsMenu(true)

        view.apply {
            profile_img.setOnClickListener {
                pickProfileImage()
            }
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        FirestoreUtil.getCurrentUser(this::updateUI)
    }

    private fun updateUI(user: User) {
        if (this@ProfileFragment.isVisible) {
            input_name.setText(user.name)
            input_bio.setText(user.bio)
            if (!pictureJustChanged && user.profilePicturePath != null) {
                GlideApp.with(this)
                    .load(StorageUtil.pathToReference(user.profilePicturePath))
                    .placeholder(R.drawable.ic_default_profile_img)
                    .into(profile_img)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.fragment_profile, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_save -> {
                saveUser()
                return true
            }
            R.id.action_sign_out -> {
                signOutUser()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun pickProfileImage() {
        val intent = Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = ImageType.ANY
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(ImageType.JPEG, ImageType.PNG))
            }
        }
        startActivityForResult(Intent.createChooser(intent,
            getString(R.string.chooser_select_profile_img)), PICK_IMG_RC)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMG_RC && resultCode == Activity.RESULT_OK &&
            data != null && data.data != null
        ) {
            val selectedImagePath = data.data!!
            selectedImageBytes = getSelectedImageBytes(selectedImagePath)

            GlideApp.with(this)
                .load(selectedImageBytes)
                .into(profile_img)

            pictureJustChanged = true
        }
    }

    private fun getSelectedImageBytes(selectedImagePath: Uri): ByteArray {
        val selectedImageBitmap = MediaStore.Images.Media.getBitmap(
            activity?.contentResolver, selectedImagePath
        )
        val outputStream = ByteArrayOutputStream()
        selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        return outputStream.toByteArray()
    }

    private fun saveUser() {
        if (::selectedImageBytes.isInitialized) {
            StorageUtil.uploadProfileImage(selectedImageBytes) { imagePath ->
                updateUserProfile(imagePath)
            }
        } else {
            updateUserProfile(null)
        }
    }

    private fun updateUserProfile(imagePath: String?) {
        val name = input_name.text.toString().trim()
        val bio = input_bio.text.toString().trim()

        val progressDialog = indeterminateProgressDialog(getString(R.string.dialog_saving_profile))

        FirestoreUtil.updateCurrentUser(name, bio, imagePath) { message ->
            progressDialog.dismiss()
            view?.snackbar(message)
        }
    }

    private fun signOutUser() {
        AuthUI.getInstance()
            .signOut(requireContext())
            .addOnCompleteListener {
                startActivity(intentFor<SignInActivity>().newTask().clearTask())
            }
    }
}
