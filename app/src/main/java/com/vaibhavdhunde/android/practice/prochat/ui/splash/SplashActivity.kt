package com.vaibhavdhunde.android.practice.prochat.ui.splash

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import com.vaibhavdhunde.android.practice.prochat.R
import com.vaibhavdhunde.android.practice.prochat.ui.auth.SignInActivity
import com.vaibhavdhunde.android.practice.prochat.ui.main.MainActivity
import org.jetbrains.anko.startActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            if (FirebaseAuth.getInstance().currentUser == null) {
                startActivity<SignInActivity>()
            } else {
                startActivity<MainActivity>()
                finish()
            }
        }, 1500L)
    }
}
