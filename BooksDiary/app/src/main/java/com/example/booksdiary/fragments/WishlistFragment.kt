package com.example.booksdiary.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.booksdiary.BooksAdapter
import com.example.booksdiary.activities.BooksDetailsActivity
import com.example.booksdiary.model.Book
import com.example.booksdiary.OnItemClickListener
import com.example.booksdiary.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_favorites.*
import kotlinx.android.synthetic.main.fragment_wishlist.*


class WishlistFragment : Fragment(), OnItemClickListener {
    private lateinit var mFirebaseAuth: FirebaseAuth
    private var mWishlist = ArrayList<Book>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wishlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = Firebase.firestore
        mFirebaseAuth = FirebaseAuth.getInstance()
        val uid = mFirebaseAuth.currentUser?.uid

        db.collection("Books")
            .whereEqualTo("uid", uid)
            .whereEqualTo("wishlist", true)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w("WishlistFragment", "Listen failed.", e)
                    return@addSnapshotListener
                }
                mWishlist.clear()

                for (doc in value!!) {
                    val book = doc.toObject(Book::class.java)
                    mWishlist.add(book)
                }
                wishlist_fragment_books_list.apply {
                    layoutManager = GridLayoutManager(context, 3)
                    adapter = BooksAdapter(context, mWishlist, this@WishlistFragment)
                }
                Log.d("WishlistFragment", "Books added")
            }
    }

    override fun onItemClicked(book: Book) {
        val intent = Intent(context, BooksDetailsActivity::class.java).putExtra("bid", book.bid)
        startActivity(intent)
    }

}