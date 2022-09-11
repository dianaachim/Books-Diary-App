package com.example.booksdiary.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.booksdiary.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_start.*

class StartActivity : AppCompatActivity() {
    private lateinit var mFirebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        mFirebaseAuth = FirebaseAuth.getInstance()
        checkUserSession()

        main_activity_login_button.setOnClickListener {
            val intent =
                Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        main_activity_register_button.setOnClickListener {
            val intent =
                Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkUserSession() {
        val currentUser = mFirebaseAuth.currentUser
        if (currentUser != null) {

            val mainMenuIntent = Intent(this, MainActivity::class.java)
            startActivity(mainMenuIntent)

        }
    }

    override fun onBackPressed() {
        // this is empty so that it does not by default go back to LoginActivity while still connected to a session
    }

}