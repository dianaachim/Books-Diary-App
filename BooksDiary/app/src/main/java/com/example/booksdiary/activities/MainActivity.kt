package com.example.booksdiary.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.viewpager2.widget.ViewPager2
import com.example.booksdiary.adapters.PagerAdapter
import com.example.booksdiary.R
import com.example.booksdiary.model.User
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_favorites.*

class MainActivity : AppCompatActivity() {
    private lateinit var username: String
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var viewPager2: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var tabLayoutMediator: TabLayoutMediator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager2 = findViewById(R.id.view_pager)
        viewPager2.adapter = PagerAdapter(this)

        val db = Firebase.firestore
        mFirebaseAuth = FirebaseAuth.getInstance()
        val uid = mFirebaseAuth.currentUser?.uid

        db.collection("Users")
            .whereEqualTo("uid", uid)
            .limit(1)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w("MainActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }
                for (doc in value!!) {
                    val user = doc.toObject(User::class.java)
                    username = user.username!!

                }
                main_activity_username_text.text = username
                Log.d("MainActivity", "Username set")
            }

        main_activity_exit_button.setOnClickListener{
            mFirebaseAuth.signOut()
            val intent = Intent(this, StartActivity::class.java)
            startActivity(intent)
        }

        tabLayout = findViewById(R.id.tab_layout)
        tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager2
        ) { tab, position -> // Styling each tab here
            when (position) {
                1 -> {
                    tab.text = "Wishlist"
                    tab.setIcon(R.drawable.ic_star)
                }
                2 -> {
                    tab.text = "Favorites"
                    tab.setIcon(R.drawable.ic_favorite)
                }
                else -> {
                    tab.text = "Books"
                    tab.setIcon(R.drawable.ic_book)
                }
            }
        }
        tabLayoutMediator.attach()
    }

    override fun onBackPressed() {
        // this is empty so that it does not by default go back to LoginActivity while still connected to a session
    }

}