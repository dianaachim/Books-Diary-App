package com.example.booksdiary.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.booksdiary.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {
    private lateinit var mFirebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mFirebaseAuth = FirebaseAuth.getInstance()

        login_activity_login_button.setOnClickListener {
            val email = login_activity_email_edittext.text.toString()
            val password = login_activity_password_edittext.text.toString()

            mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this
                ) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        showMessage("Login success")
                        val user: FirebaseUser? = mFirebaseAuth.currentUser

                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        showMessage("Login failed")
                        updateUI(null)
                    }
                }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        } else {
            showMessage("Please sign in to continue!")
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}