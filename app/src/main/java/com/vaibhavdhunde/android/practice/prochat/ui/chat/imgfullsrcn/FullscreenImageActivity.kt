package com.vaibhavdhunde.android.practice.prochat.ui.chat.imgfullsrcn

import android.os.Build
import android.os.Bundle
import android.view.View.*
import android.view.Window.FEATURE_NO_TITLE
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.vaibhavdhunde.android.practice.prochat.util.GlideApp
import com.vaibhavdhunde.android.practice.prochat.util.IMAGE_PATH
import com.vaibhavdhunde.android.practice.prochat.util.StorageUtil
import kotlinx.android.synthetic.main.activity_fullscreen_image.*


class FullscreenImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.vaibhavdhunde.android.practice.prochat.R.layout.activity_fullscreen_image)

        hideSystemUI()

        val imagePath = intent.getStringExtra(IMAGE_PATH)!!

        showImage(imagePath)
    }

    private fun showImage(imagePath: String) {
        GlideApp.with(this)
            .load(StorageUtil.pathToReference(imagePath))
            .into(fullscreen_image)
    }

    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }*/

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val decorView = window.decorView
            decorView.systemUiVisibility = SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                SYSTEM_UI_FLAG_LAYOUT_STABLE or
                SYSTEM_UI_FLAG_FULLSCREEN or
                SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        } else {
            requestWindowFeature(FEATURE_NO_TITLE)
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }
}
