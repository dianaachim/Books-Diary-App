package com.example.booksdiary.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.booksdiary.adapters.SearchedBookAdapter
import kotlinx.android.synthetic.main.activity_search_book.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import com.example.booksdiary.model.Book
import com.example.booksdiary.OnItemClickListener
import com.example.booksdiary.R


class SearchBookActivity : AppCompatActivity(), OnItemClickListener {
    private var mBooks = ArrayList<Book>()
    private lateinit var mRequestQueue: RequestQueue

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_book)

        val searchedText = intent.getStringExtra("text")

        if (searchedText != null) {
            //this means that we've searched a book by picture
            search_book_activity_progress_bar.visibility = View.VISIBLE
            search_book_activity_search_bar.setText(searchedText)
            getBooksInfo(searchedText)
        }

        search_book_activity_search_button.setOnClickListener {
            search_book_activity_progress_bar.visibility = View.VISIBLE

            if (search_book_activity_search_bar.text.isEmpty()) {
                search_book_activity_search_bar.error = "Please enter search query"
                search_book_activity_search_bar.requestFocus()
                return@setOnClickListener
            }
            mBooks.clear()
            getBooksInfo(search_book_activity_search_bar.text.toString())
        }
    }

    private fun getBooksInfo(query: String) {
        mRequestQueue = Volley.newRequestQueue(this)

        // below line is use to clear cache this
        // will be use when our data is being updated.
        mRequestQueue.cache.clear()

        // below is the url for getting data from API in json format.
        val url = "https://www.googleapis.com/books/v1/volumes?q=$query&maxResults=40"

        // below line we are  creating a new request queue.
        val queue = Volley.newRequestQueue(this)

        // below line is use to make json object request inside that we
        // are passing url, get method and getting json object. .
        val booksObjRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                if (response != null) {
                    search_book_activity_progress_bar.visibility = View.INVISIBLE
                    try {
                        val itemsArray: JSONArray = response.getJSONArray("items")
                        for (i in 0..itemsArray.length()) {
                            val itemsObj: JSONObject = itemsArray.getJSONObject(i)
                            val volumeObj: JSONObject = itemsObj.getJSONObject("volumeInfo")
                            val title = volumeObj.optString("title")
                            val authorsArray: JSONArray = volumeObj.getJSONArray("authors")
                            val description = volumeObj.optString("description")
                            val imageLinks = volumeObj.optJSONObject("imageLinks")
                            val thumbnail = imageLinks?.optString("thumbnail")

                            var author = ""
                            if (authorsArray.length() != 0) {
                                for (j in 1..authorsArray.length()) {
                                    author = author + " " + authorsArray.optString(i)
                                }
                            }

                            val book = Book(
                                "",
                                "",
                                title,
                                author,
                                description,
                                "",
                                favorites = false,
                                wishlist = false,
                                thumbnail = thumbnail,
                                pdf = "",
                                pdfFileName = ""
                            )
                            mBooks.add(book)

                            search_book_activity_book_list.apply {
                                layoutManager = LinearLayoutManager(this@SearchBookActivity)
                                adapter = SearchedBookAdapter(mBooks, this@SearchBookActivity)
                                (adapter as SearchedBookAdapter).notifyDataSetChanged()
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Log.w("SearchBookActivity", "Listen failed.", e)
                    }
                } else {
                    showMessage("No response")
                }
            },
            { error ->showMessage(error.message.toString()) })

        queue.add(booksObjRequest)
    }

    override fun onItemClicked(book: Book) {
        TODO("Not yet implemented")
    }
}