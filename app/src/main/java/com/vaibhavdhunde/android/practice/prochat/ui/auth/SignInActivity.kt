package com.vaibhavdhunde.android.practice.prochat.ui.auth

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.vaibhavdhunde.android.practice.prochat.R
import com.vaibhavdhunde.android.practice.prochat.service.ProChatMessagingService
import com.vaibhavdhunde.android.practice.prochat.ui.main.MainActivity
import com.vaibhavdhunde.android.practice.prochat.util.FirestoreUtil
import kotlinx.android.synthetic.main.activity_sign_in.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask

@Suppress("DEPRECATION")
class SignInActivity : AppCompatActivity() {

    companion object {
        private const val SIGN_IN_RC = 9831
    }

    private val signInProviders = listOf(
        AuthUI.IdpConfig.EmailBuilder()
            .setAllowNewAccounts(true)
            .setRequireName(true)
            .build()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        btn_sign_in.setOnClickListener {
            val intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(signInProviders)
                .setLogo(R.drawable.ic_pro_chat)
                .build()
            startActivityForResult(intent, SIGN_IN_RC)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SIGN_IN_RC) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                val progressDialog = indeterminateProgressDialog(getString(R.string.dialog_setting_account))
                FirestoreUtil.initCurrentUserIfFirstTime {
                    startActivity(intentFor<MainActivity>().newTask().clearTask())
                    // add the registration token
                    ProChatMessagingService.getRegistrationToken { token ->
                        ProChatMessagingService.addRegistrationToken(token)
                    }
                    progressDialog.dismiss()
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                if (response == null) {
                    return
                }

                when (response.error?.errorCode) {
                    ErrorCodes.NO_NETWORK ->
                        sign_in_activity?.longSnackbar(getString(R.string.error_no_network))
                    ErrorCodes.UNKNOWN_ERROR ->
                        sign_in_activity?.longSnackbar(getString(R.string.error_unknown))
                }
            }
        }
    }

}
