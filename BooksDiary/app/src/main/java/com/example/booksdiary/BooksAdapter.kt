package com.example.booksdiary

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.booksdiary.model.Book
import com.squareup.picasso.Picasso

class BooksAdapter(mainContext : Context, private val list: ArrayList<Book>, private val itemClickListener: OnItemClickListener) : RecyclerView.Adapter<BooksViewHolder>() {
    private var mContext = mainContext

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BooksViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return BooksViewHolder(inflater, parent)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: BooksViewHolder, position: Int) {
        val book = list[holder.adapterPosition]
        holder.bind(mContext, book, itemClickListener)
    }
}

class BooksViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.book_card, parent, false)) {
    private var mTitle: TextView? = null
    private var mAuthor: TextView? = null
    private var mFavorites: ImageView? = null
    private var mCover: ImageView? = null

    init {
        mTitle = itemView.findViewById(R.id.book_card_book_title_tv)
        mAuthor= itemView.findViewById(R.id.book_card_book_author_tv)
        mFavorites= itemView.findViewById(R.id.book_card_book_in_favorites)
        mCover = itemView.findViewById(R.id.book_card_book_cover_image)

    }

    private fun loadImageWithPicasso(context: Context, url: String, imageView: ImageView?) {
        Picasso.with(context).load(url).into(imageView)
    }

    fun bind(context: Context, book: Book, clickListener: OnItemClickListener) {
        mTitle?.text = book.title
        mAuthor?.text = book.author
        if (book.thumbnail != "") {
            loadImageWithPicasso(context, book.thumbnail.toString(), mCover)
        }
        if (book.favorites == true) {
            mFavorites?.visibility = View.VISIBLE
        } else {
            mFavorites!!.visibility = View.INVISIBLE
        }
        itemView.setOnClickListener {
            clickListener.onItemClicked(book)
        }
    }
}

interface OnItemClickListener{
    fun onItemClicked(book: Book)
}