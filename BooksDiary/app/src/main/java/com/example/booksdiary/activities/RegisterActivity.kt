package com.example.booksdiary.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.booksdiary.model.User
import com.example.booksdiary.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var mFirebaseAuth: FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mFirebaseAuth = FirebaseAuth.getInstance()

        register_activity_register_button.setOnClickListener {
            val email = register_activity_email_edittext.text.toString()
            val username = register_activity_username_edittext.text.toString()
            val password = register_activity_password_edittext.text.toString()
            val confirmPassword = register_activity_confirm_password_edittext.text.toString()

            when {
                TextUtils.isEmpty(email) -> {
                    register_activity_email_edittext.error = "Email is required"
                    register_activity_email_edittext.requestFocus()
                }
                TextUtils.isEmpty(password) -> {
                    register_activity_password_edittext.error = "Password is required"
                    register_activity_password_edittext.requestFocus()
                }
                password != confirmPassword -> {
                    register_activity_confirm_password_edittext.error = "Passwords don't match"
                    register_activity_confirm_password_edittext.requestFocus()
                }
                password.length < 6 -> {
                    register_activity_password_edittext.error = "Password too short"
                    register_activity_password_edittext.requestFocus()
                }
                else -> {
                    mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) {
                            if (it.isSuccessful) {
                                mFirebaseAuth.currentUser?.sendEmailVerification()
                                    ?.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val userid = mFirebaseAuth.currentUser!!.uid
                                            createNewUserWithInfo(userid, username, email)
                                        } else {
                                            showMessage(it.exception?.message.toString())
                                        }
                                    }
                            } else {
                                showMessage(it.exception?.message.toString())
                            }
                        }
                }
            }
        }
    }

    private fun createNewUserWithInfo(userid: String, username: String, email: String) {
        val user = User(userid, username, email)
        db.collection("Users")
            .document(userid)
            .set(user)
            .addOnSuccessListener {
                Log.w("RegisterActivity", "user added successfully")
                showMessage("User added successfully")

                val intent =
                    Intent(applicationContext, MainActivity::class.java).putExtra("username", username)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Log.w("RegisterActivity", e.message.toString())
            }
    }

    private fun showMessage(message:String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}