package com.example.booksdiary.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.booksdiary.BooksAdapter
import com.example.booksdiary.model.Book
import com.example.booksdiary.OnItemClickListener
import com.example.booksdiary.R
import com.example.booksdiary.activities.BooksDetailsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_books.*
import kotlinx.android.synthetic.main.fragment_favorites.*


class FavoritesFragment : Fragment(), OnItemClickListener {
    private lateinit var mFirebaseAuth: FirebaseAuth
    private var mFavorites = ArrayList<Book>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = Firebase.firestore
        mFirebaseAuth = FirebaseAuth.getInstance()
        val uid = mFirebaseAuth.currentUser?.uid

        db.collection("Books")
            .whereEqualTo("uid", uid)
            .whereEqualTo("favorites", true)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w("FavoritesFragment", "Listen failed.", e)
                    return@addSnapshotListener
                }
                mFavorites.clear()

                for (doc in value!!) {
                    val book = doc.toObject(Book::class.java)
                    mFavorites.add(book)
                }
                favorites_fragment_books_list.apply {
                    layoutManager = GridLayoutManager(context, 3)
                    adapter = BooksAdapter(context, mFavorites, this@FavoritesFragment)
                }
                Log.d("FavoritesFragment", "Books added")
            }
    }

    override fun onItemClicked(book: Book) {
        val intent = Intent(context, BooksDetailsActivity::class.java).putExtra("bid", book.bid)
        startActivity(intent)
    }

}