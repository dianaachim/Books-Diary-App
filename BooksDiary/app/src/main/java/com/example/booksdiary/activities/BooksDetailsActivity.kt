package com.example.booksdiary.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.booksdiary.adapters.BookDetailsCallback
import com.example.booksdiary.model.Book
import com.example.booksdiary.R
import com.example.booksdiary.ReadPdfActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_books_details.*
import kotlinx.android.synthetic.main.activity_main.*


class BooksDetailsActivity : AppCompatActivity() {
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mStorage: StorageReference
    private val db = Firebase.firestore
    var favoritesClicked = false //if the favorites button is clicked
    var wishlistClicked = false //if the wishlist button is clicked
    lateinit var currentBook: Book
    var imageThumbnail = ""
    //uri of picked pdf
    private var pdfUri: Uri? = null
    private var pdfLink: String? = null
    lateinit var dialog: ProgressDialog
    private var filename: String? = null
    private var pdfUploaded: Boolean = false
    private var oldFilename: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_books_details)

        mFirebaseAuth = FirebaseAuth.getInstance()
        val uid = mFirebaseAuth.currentUser?.uid
        mStorage = FirebaseStorage.getInstance().reference

        var bid = intent.getStringExtra("bid")
        if (bid != null) {
            //if we have a bid it means that we are viewing the details of a book in our database
            populateBookFields(bid, object : BookDetailsCallback {
                override fun onCallback(value: Book) {
                    currentBook = value
                    book_details_activity_book_title_et.setText(
                        currentBook.title,
                        TextView.BufferType.EDITABLE
                    )
                    book_details_activity_book_author_et.setText(
                        currentBook.author,
                        TextView.BufferType.EDITABLE
                    )
                    book_details_activity_book_description_et.setText(
                        currentBook.description,
                        TextView.BufferType.EDITABLE
                    )
                    book_details_activity_book_notes_et.setText(
                        currentBook.notes,
                        TextView.BufferType.EDITABLE
                    )

                    if (currentBook.favorites == true) {
                        favoritesClicked = true
                        book_details_activity_add_to_favorites_button.visibility = View.INVISIBLE
                        book_details_activity_add_to_favorites_button.isClickable = false
                        book_details_activity_added_to_favorites_button.visibility = View.VISIBLE
                        book_details_activity_added_to_favorites_button.isClickable = true
                    }

                    if (currentBook.wishlist == true) {
                        wishlistClicked = true
                        book_details_activity_add_to_wishlist_button.visibility = View.INVISIBLE
                        book_details_activity_add_to_wishlist_button.isClickable = false
                        book_details_activity_added_to_wishlist_button.visibility = View.VISIBLE
                        book_details_activity_added_to_wishlist_button.isClickable = true
                    }
                    if (currentBook.thumbnail != "") {
                        loadImageWithPicasso(
                            this@BooksDetailsActivity,
                            currentBook.thumbnail!!, book_details_activity_book_image
                        )
                        imageThumbnail = currentBook.thumbnail!!
                    }

                    if (currentBook.pdf.equals(null) or currentBook.pdf.equals("")) {
                        book_details_activity_open_pdf_button.visibility = View.INVISIBLE
                        book_details_activity_open_pdf_button.isClickable = false

                        book_details_activity_open_pdf_tv.visibility = View.INVISIBLE
                    } else {
                        pdfLink = currentBook.pdf
                        filename = currentBook.pdfFileName
                    }
                }
            })
        } else {
            // if we do not have a bid, it means we are trying to add a new book
            bid = db.collection("Books").document().id

            book_details_activity_delete_book_button.visibility = View.INVISIBLE
            book_details_activity_delete_book_button.isClickable = false

            book_details_activity_open_pdf_button.visibility = View.INVISIBLE
            book_details_activity_open_pdf_button.isClickable = false

            book_details_activity_open_pdf_tv.visibility = View.INVISIBLE

            book_details_activity_add_pdf_button.visibility = View.INVISIBLE
            book_details_activity_add_pdf_button.isClickable = false

            book_details_activity_add_pdf_tv.visibility = View.INVISIBLE

            val title = intent.getStringExtra("title")
            if (title!=null) {
                //this means that we have searched a book
                val author = intent.getStringExtra("author")
                val description = intent.getStringExtra("description")
                val thumbnail = intent.getStringExtra("thumbnail")

                book_details_activity_book_title_et.setText(title)
                book_details_activity_book_author_et.setText(author)
                book_details_activity_book_description_et.setText(description)
                imageThumbnail = if (thumbnail != "" && thumbnail != null) {
                    loadImageWithPicasso(this, thumbnail, book_details_activity_book_image)
                    thumbnail
                } else {
                    ""
                }
            }
        }

        book_details_activity_add_to_favorites_button.setOnClickListener {
            favoritesClicked = !favoritesClicked
            onStatusButtonsClicked(
                book_details_activity_add_to_favorites_button,
                book_details_activity_added_to_favorites_button
            )
        }

        book_details_activity_added_to_favorites_button.setOnClickListener {
            favoritesClicked = !favoritesClicked
            onStatusButtonsClicked(
                book_details_activity_added_to_favorites_button,
                book_details_activity_add_to_favorites_button
            )
        }

        book_details_activity_add_to_wishlist_button.setOnClickListener {
            wishlistClicked = !wishlistClicked
            onStatusButtonsClicked(
                book_details_activity_add_to_wishlist_button,
                book_details_activity_added_to_wishlist_button
            )
        }

        book_details_activity_added_to_wishlist_button.setOnClickListener {
            wishlistClicked = !wishlistClicked
            onStatusButtonsClicked(
                book_details_activity_added_to_wishlist_button,
                book_details_activity_add_to_wishlist_button
            )
        }

        book_details_activity_delete_book_button.setOnClickListener {
            if (filename != null) {
                deletePdfFromStorage(mFirebaseAuth.currentUser?.uid + filename.toString())
            }

            onDeleteButtonClicked(bid)
        }

        book_details_activity_add_pdf_button.setOnClickListener {
            if (currentBook.pdf.equals(null) or currentBook.pdf.equals("")) {
                selectPDF()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("PDF file already uploaded")
                    .setMessage("Do you want to replace the current PDF file with another one?")
                    .setPositiveButton("Yes") { _, _ ->

                        selectPDF()
                    }
                    .setNegativeButton("No") { _, _ -> }
                    .create()
                    .show()
            }
        }

        book_details_activity_open_pdf_button.setOnClickListener {
            val intent = Intent(this, ReadPdfActivity::class.java).putExtra("pdf", currentBook.pdf)
            startActivity(intent)
        }

        book_details_activity_save_book_button.setOnClickListener {
            val title = book_details_activity_book_title_et.text.toString()
            val author = book_details_activity_book_author_et.text.toString()
            val desc = book_details_activity_book_description_et.text.toString()
            val notes = book_details_activity_book_notes_et.text.toString()
            val favorites = favoritesClicked
            val wishlist = wishlistClicked

            when {
                TextUtils.isEmpty(title) -> {
                    book_details_activity_book_title_et.error = "Book title missing"
                    book_details_activity_book_title_et.requestFocus()
                }
                TextUtils.isEmpty(author) -> {
                    book_details_activity_book_author_et.error = "Book author missing"
                    book_details_activity_book_author_et.requestFocus()
                }
                else -> {
                    if (!oldFilename.equals(null) && !oldFilename.equals(currentBook.pdfFileName)) {
                        deletePdfFromStorage(mFirebaseAuth.currentUser?.uid + oldFilename.toString())
                    }

                    val book = Book(
                        bid,
                        uid,
                        title,
                        author,
                        desc,
                        notes,
                        favorites,
                        wishlist,
                        imageThumbnail,
                        pdfLink,
                        filename
                    )

                    db.collection("Books")
                        .document(bid)
                        .set(book)
                        .addOnSuccessListener {
                            Log.w("BookDetailsActivity", "book added successfully")
                            showMessage("Success")
                        }
                        .addOnFailureListener { e ->
                            Log.w("BookDetailsActivity", "error adding book")
                            showMessage(e.message.toString())
                        }

                    //refresh activity
                    finish()
                    overridePendingTransition(0, 0)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                }
            }
        }

        book_details_activity_cancel_button.setOnClickListener {
            //if we have added a pdf file, pushing the cancel button leads to deleting the uploaded pdf
            if (!filename.equals(null) && (currentBook.pdfFileName.equals(null) or !currentBook.pdfFileName.equals(filename))) {
                deletePdfFromStorage(mFirebaseAuth.currentUser?.uid + filename.toString())
            }

            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun deletePdfFromStorage(filename: String) {
        if (!filename.equals(null)) {
            mStorage.child(filename).delete().addOnSuccessListener {
            }.addOnFailureListener {
                showMessage(it.message.toString())
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode== Activity.RESULT_OK && requestCode == 12 && data != null && data.data != null){
                pdfUri= data.data
                upload(data.data)
        } else {
            showMessage("Error getting pdf file")
        }
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            cursor.use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }

        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result!!.substring(cut + 1)
            }
        }
        return result as String
    }

    @Suppress("DEPRECATION")
    private fun upload(data: Uri?) {
        dialog = ProgressDialog(this)
        dialog.setMessage("Uploading...")
        dialog.show()
        filename = getFileName(data!!)
        val filePath = data.path

        val ref: StorageReference = mStorage.child(mFirebaseAuth.currentUser?.uid + filename.toString())
        // Create file metadata including the content type
        val metadata = StorageMetadata.Builder()
            .setCustomMetadata("filename", filename.toString())
            .setCustomMetadata("storage_name", filename.toString())
            .setCustomMetadata("path", filePath)
            .build()
        ref.putFile(data, metadata).addOnSuccessListener {
            val uriTask: Task<Uri> = it.storage.downloadUrl
            while (!uriTask.isComplete) ;
            val uri = uriTask.result
            pdfLink = uri.toString()
            currentBook.pdf = pdfLink
            oldFilename = currentBook.pdfFileName
            //currentBook.pdfFileName = filename
            pdfUploaded = true
            dialog.dismiss()

        }.addOnProgressListener {
            val progress: Double = (100.0 * it.bytesTransferred)/it.totalByteCount
            dialog.setMessage("File uploaded...$progress%")
        }
    }

    private fun loadImageWithPicasso(context: Context, url: String, imageView: ImageView?) {
        Picasso.with(context).load(url).into(imageView)
    }

    private fun onDeleteButtonClicked(bid: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete book")
            .setMessage("Are you sure you want to delete this book?")
            .setPositiveButton("Delete") { _, _ ->
                db.collection("Books")
                    .document(bid)
                    .delete()
                    .addOnSuccessListener {
                        showMessage("Book deleted successfully")
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                    }
                    .addOnFailureListener {
                        showMessage("Book could not be deleted")
                        Log.w("BooksDetailsActivity", it.message.toString())
                    }
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .create()
            .show()
    }

    private fun populateBookFields(bid: String, callback: BookDetailsCallback) {
        db.collection("Books")
            .whereEqualTo("bid", bid)
            .limit(1)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w("BooksDetailsActivity", "Listen failed.", e)
                    return@addSnapshotListener
                }

                var book = Book()
                for (doc in value!!) {
                    book = doc.toObject(Book::class.java)
                }
                callback.onCallback(book)
                Log.d("BooksDetailsActivity", "Book found")
            }
    }

    private fun onStatusButtonsClicked(imageView: ImageView, imageViewReverse: ImageView) {
        imageView.visibility = View.INVISIBLE
        imageView.isClickable = false
        imageViewReverse.visibility = View.VISIBLE
        imageViewReverse.isClickable = true
    }

    private fun selectPDF() {
        Log.d("PDF_ADD_TAG", "pdfPickIntent: starting pdf pick intent")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, 12)
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Did you save your data?")
            .setMessage("Going back results in loosing your changes if not saved. Proceed?")
            .setPositiveButton("Yes") { _, _ ->
                if (!filename.equals(null) && (currentBook.pdfFileName.equals(null) or !currentBook.pdfFileName.equals(
                        filename
                    ))
                ) {
                    deletePdfFromStorage(mFirebaseAuth.currentUser?.uid + filename.toString())
                }
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton("No") { _, _ -> }
            .create()
            .show()
    }

}