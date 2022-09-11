package com.example.booksdiary.fragments

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.booksdiary.*
import com.example.booksdiary.model.Book
import com.example.booksdiary.activities.BooksDetailsActivity
import com.example.booksdiary.activities.SearchBookActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import kotlinx.android.synthetic.main.activity_view_photo.*
import kotlinx.android.synthetic.main.fragment_books.*


class BooksFragment : Fragment(), OnItemClickListener {

    private lateinit var mFirebaseAuth: FirebaseAuth
    private var mBooks = ArrayList<Book>()
    private val CAMERA_PERMISSION_CODE = 1000
    private val IMAGE_CAPTURE_CODE = 1001
    private var imageUri: Uri? = null
    private var clicked = false

    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(
        context,
        R.anim.rotate_open_anim
    )}
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(
        context,
        R.anim.rotate_close_anim
    )}
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(
        context,
        R.anim.from_bottom_anim
    )}
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(
        context,
        R.anim.to_bottom_anim
    )}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_books, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = Firebase.firestore
        mFirebaseAuth = FirebaseAuth.getInstance()
        val uid = mFirebaseAuth.currentUser?.uid

        db.collection("Books")
            .whereEqualTo("uid", uid)
            .whereNotEqualTo("wishlist", true)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w("BooksFragment", "Listen failed.", e)
                    return@addSnapshotListener
                }
                mBooks.clear()

                for (doc in value!!) {
                    val book = doc.toObject(Book::class.java)
                    mBooks.add(book)
                }
                books_fragment_books_list.apply {
                    layoutManager = GridLayoutManager(context, 3)
                    adapter = BooksAdapter(context, mBooks, this@BooksFragment)
                }
                Log.d("BooksFragment", "Books added")
            }

        books_fragment_add_book_button.setOnClickListener {
            onAddButtonClicked()
        }

        books_fragment_add_book_manually_button.setOnClickListener {
            val intent = Intent(context, BooksDetailsActivity::class.java)
            startActivity(intent)
        }

        books_fragment_add_book_by_search_button.setOnClickListener {
            val intent = Intent(context, SearchBookActivity::class.java)
            startActivity(intent)
        }

        books_fragment_add_book_by_picture_button.setOnClickListener {
            // Request permission
            val permissionGranted = requestCameraPermission()
            if (permissionGranted) {
                val mDialogView = LayoutInflater.from(context).inflate(R.layout.rotate_phone_alert, null)
                // Open the camera interface
                    AlertDialog.Builder(context!!)
                        .setView(mDialogView)
                        .setTitle("Rotate phone")
                        .setPositiveButton("OK") { _, _ ->
                            showMessage("Take a picture of the book handle")
                            openCameraInterface()
                        }
                        .setNegativeButton("Cancel") { _, _ -> }
                        .create()
                        .show()
            }
//            val intent = Intent(context, ViewPhotoActivity::class.java)
//            startActivity(intent)
        }
    }

    private fun requestCameraPermission(): Boolean {
        var permissionGranted = false

        // If system os is Marshmallow or Above, we need to request runtime permission
        val cameraPermissionNotGranted = ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_DENIED
        if (cameraPermissionNotGranted){
            val permission = arrayOf(Manifest.permission.CAMERA)

            // Display permission dialog
            requestPermissions(permission, CAMERA_PERMISSION_CODE)
        }
        else{
            // Permission already granted
            permissionGranted = true
        }

        return permissionGranted
    }

    // Handle Allow or Deny response from the permission dialog
    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode === CAMERA_PERMISSION_CODE) {
            if (grantResults.size === 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // Permission was granted
                openCameraInterface()
            }
            else{
                // Permission was denied
                showAlert("Camera permission was denied. Unable to take a picture.")
            }
        }
    }

    private fun openCameraInterface() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Take picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Camera picture for book app")

        imageUri = context?.contentResolver?.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )

        // Create camera intent
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, IMAGE_CAPTURE_CODE)
    }

    private fun showAlert(message: String) {
        val builder = AlertDialog.Builder(context!!)
        builder.setMessage(message)
        builder.setPositiveButton("OK", null)

        val dialog = builder.create()
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Callback from camera intent
        if (resultCode == Activity.RESULT_OK){
            // Set image captured to image view
            @Suppress("DEPRECATION") val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, Uri.parse(
                imageUri.toString()
            ))

            //Create a FirebaseVisionImage object from your image/bitmap.
            val firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap)
            val firebaseVision = FirebaseVision.getInstance()
            val firebaseVisionTextRecognizer = firebaseVision.onDeviceTextRecognizer

            //process the image
            val task: Task<FirebaseVisionText> = firebaseVisionTextRecognizer.processImage(firebaseVisionImage)

            task.addOnSuccessListener { firebaseVisionText ->
                val text: String = firebaseVisionText.text.toString()
                if (text == "") {
                    showAlert("Error getting book info. Please retry")
                } else {
                    val intent = Intent(context, SearchBookActivity::class.java).putExtra("text", text)
                    startActivity(intent)
                }
            }

            task.addOnFailureListener { e ->
                showMessage(e.message.toString())
            }
        }
        else {
            // Failed to take picture
            showAlert("Failed to take camera picture")
        }
    }

    override fun onItemClicked(book: Book) {
        val intent = Intent(context, BooksDetailsActivity::class.java).putExtra("bid", book.bid)
        startActivity(intent)
    }

    private fun onAddButtonClicked() {
        setButtonsVisibility(clicked)
        setButtonsAnimation(clicked)
        setButtonsClickable(clicked)
        clicked = !clicked
    }

    private fun setButtonsVisibility(clicked: Boolean) {
        if (!clicked) {
            books_fragment_add_book_manually_button.visibility = View.VISIBLE
            books_fragment_add_book_by_search_button.visibility = View.VISIBLE
            books_fragment_add_book_by_picture_button.visibility = View.VISIBLE
        } else {
            books_fragment_add_book_manually_button.visibility = View.INVISIBLE
            books_fragment_add_book_by_search_button.visibility = View.INVISIBLE
            books_fragment_add_book_by_picture_button.visibility = View.INVISIBLE
        }
    }

    private fun setButtonsAnimation(clicked: Boolean) {
        if (!clicked) {
            books_fragment_add_book_manually_button.startAnimation(fromBottom)
            books_fragment_add_book_by_search_button.startAnimation(fromBottom)
            books_fragment_add_book_by_picture_button.startAnimation(fromBottom)
            books_fragment_add_book_button.startAnimation(rotateOpen)
        } else {
            books_fragment_add_book_manually_button.startAnimation(toBottom)
            books_fragment_add_book_by_search_button.startAnimation(toBottom)
            books_fragment_add_book_by_picture_button.startAnimation(toBottom)
            books_fragment_add_book_button.startAnimation(rotateClose)
        }
    }

    private fun setButtonsClickable(clicked: Boolean) {
        if (!clicked) {
            books_fragment_add_book_manually_button.isClickable = true
            books_fragment_add_book_by_search_button.isClickable = true
            books_fragment_add_book_by_picture_button.isClickable = true
        } else {
            books_fragment_add_book_manually_button.isClickable = false
            books_fragment_add_book_by_search_button.isClickable = false
            books_fragment_add_book_by_picture_button.isClickable = false
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


}