package com.minigram

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.minigram.data.SharedPreferenceData
import com.minigram.data.retrofit.ApiClient
import com.minigram.data.retrofit.response.LoginRequest
import com.minigram.ui.Menu
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_splash_screen)
        logo.startAnimation(myFadeInAnimation)

        generateView()
    }

    private fun generateView() {
        // Login
        btn_open_login.setOnClickListener {
            login_lay.visibility = View.VISIBLE
            register_lay.visibility = View.GONE
            btn_open_login.visibility = View.GONE
            btn_open_register.visibility = View.GONE
        }

        btn_login.setOnClickListener {
            if (formValidation(true)) {
                login()
            }
        }

        btn_login_cancel.setOnClickListener {
            login_lay.visibility = View.GONE
            register_lay.visibility = View.GONE
            btn_open_login.visibility = View.VISIBLE
            btn_open_register.visibility = View.VISIBLE
        }

        // Register
        btn_open_register.setOnClickListener {
            login_lay.visibility = View.GONE
            register_lay.visibility = View.VISIBLE
            btn_open_login.visibility = View.GONE
            btn_open_register.visibility = View.GONE
        }

        btn_register.setOnClickListener {
            if (formValidation(false)) {
                register()
            }
        }

        btn_register_cancel.setOnClickListener {
            login_lay.visibility = View.GONE
            register_lay.visibility = View.GONE
            btn_open_login.visibility = View.VISIBLE
            btn_open_register.visibility = View.VISIBLE
        }
    }

    private fun formValidation(isLogin: Boolean): Boolean {
        var cek = 0

        if (isLogin) {
            if(login_user_value.text.toString() == ""){
                cek++
                Toast.makeText(this, "Please enter your username", Toast.LENGTH_LONG).show()
            }

            if(login_pass_value.text.toString() == ""){
                cek++
                Toast.makeText(this, "Please enter your passwword", Toast.LENGTH_LONG).show()
            }
        }
        else {
            if(register_user_value.text.toString() == ""){
                cek++
                Toast.makeText(this, "Please enter your username", Toast.LENGTH_LONG).show()
            }

            if(register_pass_value.text.toString() == ""){
                cek++
                Toast.makeText(this, "Please enter your passwword", Toast.LENGTH_LONG).show()
            }
        }

        if (cek == 0){
            return true
        }
        return false
    }

    private fun login() {
        btn_login.isEnabled = false

        val params = HashMap<String, String>()
        params["username"] = login_user_value.text.toString()
        params["password"] = login_pass_value.text.toString()

        ApiClient.instance.loginRequest(params).enqueue(object : Callback<LoginRequest> {
            override fun onResponse(call: Call<LoginRequest>, response: Response<LoginRequest>?) {
                btn_login.isEnabled = true
                val message = response?.body()?.message ?: ""

                if (response != null && response.isSuccessful) {
                    if (!response.body()?.data.isNullOrEmpty()) {
                        val userId = response.body()?.data?.first()?.id ?: ""
                        val userName = response.body()?.data?.first()?.username ?: ""

                        SharedPreferenceData.setString(this@MainActivity, 1, userId ?: "")
                        SharedPreferenceData.setString(this@MainActivity, 2, userName ?: "")

                        val i = Intent(this@MainActivity, Menu::class.java)
                        startActivity(i)
                        finish()
                    } else {
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<LoginRequest>, t: Throwable) {
                btn_login.isEnabled = true
                Toast.makeText(this@MainActivity, "Connection failed", Toast.LENGTH_LONG).show()
            }

        })
    }

    private fun register() {
        btn_register.isEnabled = false

        val params = HashMap<String, String>()
        params["username"] = register_user_value.text.toString()
        params["password"] = register_pass_value.text.toString()

        ApiClient.instance.registerRequest(params).enqueue(object : Callback<LoginRequest> {
            override fun onResponse(call: Call<LoginRequest>, response: Response<LoginRequest>?) {
                btn_register.isEnabled = true
                val message = response?.body()?.message ?: ""

                if (response != null && response.isSuccessful) {
                    if (!response.body()?.data.isNullOrEmpty()) {
                        val userId = response.body()?.data?.first()?.id ?: ""
                        val userName = response.body()?.data?.first()?.username ?: ""

                        SharedPreferenceData.setString(this@MainActivity, 1, userId ?: "")
                        SharedPreferenceData.setString(this@MainActivity, 2, userName ?: "")

                        val i = Intent(this@MainActivity, Menu::class.java)
                        startActivity(i)
                        finish()
                    } else {
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<LoginRequest>, t: Throwable) {
                btn_register.isEnabled = true
                Toast.makeText(this@MainActivity, "Connection failed", Toast.LENGTH_LONG).show()
            }

        })
    }
}