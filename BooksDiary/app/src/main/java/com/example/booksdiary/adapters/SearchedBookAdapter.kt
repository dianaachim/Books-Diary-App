package com.example.booksdiary.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.booksdiary.activities.BooksDetailsActivity
import com.example.booksdiary.model.Book
import com.example.booksdiary.R
import com.squareup.picasso.Picasso
import java.util.*


class SearchedBookAdapter(private val bookInfoArrayList: ArrayList<Book>,
                          private val mcontext: Context
) :
    RecyclerView.Adapter<SearchedBookAdapter.BookViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        // inflating our layout for item of recycler view item.
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.searched_book_card, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        // inside on bind view holder method we are
        // setting ou data to each UI component.
        val bookInfo: Book = bookInfoArrayList[position]
        holder.name.text = bookInfo.title
        holder.author.text = bookInfo.author
        holder.description.text = bookInfo.description

        // below line is use to set image from URL in our image view.
        loadImageWithPicasso(mcontext, bookInfo.thumbnail.toString(), holder.thumbnail)

        // below line is use to add on click listener for our item of recycler view.
        holder.itemView.setOnClickListener { // inside on click listener method we are calling a new activity
            // and passing all the data of that item in next intent.
            val i = Intent(mcontext, BooksDetailsActivity::class.java)
            i.putExtra("title", bookInfo.title)
            i.putExtra("author", bookInfo.author)
            i.putExtra("description", bookInfo.description)
            i.putExtra("thumbnail", bookInfo.thumbnail)

            // after passing that data we are
            // starting our new intent.
            mcontext.startActivity(i)
        }
    }

    private fun loadImageWithPicasso(context: Context, url: String, imageView: ImageView?) {
        Picasso.with(context).load(url).into(imageView)
    }

    override fun getItemCount(): Int {
        return bookInfoArrayList.size
    }

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView = itemView.findViewById(R.id.searched_book_card_book_title)
        var author: TextView = itemView.findViewById(R.id.searched_book_card_book_author)
        var description: TextView = itemView.findViewById(R.id.searched_book_card_book_description)
        var thumbnail: ImageView = itemView.findViewById(R.id.searched_book_card_book_image)

    }

}
