package com.snapchat.kit.snapload


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Activity2 : AppCompatActivity() {

    private lateinit var buttonn: ImageButton
    var TAKE_PHOTO_CODE = 0
    var count = 0
    lateinit var imageView: ImageView
    lateinit var btOpen: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_2)

        buttonn = findViewById(R.id.buttonn)
        buttonn.setOnClickListener {
            openActivityMain()
        }
    }

    private fun openActivityMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
