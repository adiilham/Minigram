package com.minigram.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.minigram.R

class Login : AppCompatActivity() {
    var section = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        generateView()

    }

    private fun generateView() {
        if (intent.hasExtra("section")) {
            when (intent.getStringExtra("section")) {
                "login" -> {
                    section = "login"
                }
                "register"-> {
                    section = "register"
                }
            }
        }
    }

    private fun handlingVisibility() {

    }
}