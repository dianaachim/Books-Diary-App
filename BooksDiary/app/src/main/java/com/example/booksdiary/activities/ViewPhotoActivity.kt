@file:Suppress("DEPRECATION")

package com.example.booksdiary.activities

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.checkSelfPermission
import com.example.booksdiary.R
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_view_photo.*
import okhttp3.*
import java.io.IOException
import java.util.*


@Suppress("DEPRECATED_IDENTITY_EQUALS")
class ViewPhotoActivity : AppCompatActivity() {
    private val CAMERA_PERMISSION_CODE = 1000
    private val IMAGE_CAPTURE_CODE = 1001
    private var imageUri: Uri? = null
    private val url = ""
    private val POST = "/image"
    // creating a client
    private val okHttpClient = OkHttpClient()
    private lateinit var mStorage: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_photo)

        mStorage = FirebaseStorage.getInstance().reference
        val requestServer: Request = Request.Builder().url(url).build()

        // making call asynchronously
        okHttpClient.newCall(requestServer).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { showMessage("api down") }
            }

            override fun onResponse(call: Call, response: Response) {
                this@ViewPhotoActivity.runOnUiThread {
                    showMessage("Connected")
                }
            }
        })

        view_photo_activity_button_take_picture.setOnClickListener {
            // Request permission
            val permissionGranted = requestCameraPermission()
            if (permissionGranted) {
                val mDialogView = LayoutInflater.from(this).inflate(
                    R.layout.rotate_phone_alert,
                    null
                )
                // Open the camera interface
                AlertDialog.Builder(this)
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
        }
    }

    private fun requestCameraPermission(): Boolean {
        var permissionGranted = false

        // If system os is Marshmallow or Above, we need to request runtime permission
        val cameraPermissionNotGranted = checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
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

        imageUri = this.contentResolver?.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )

        // Create camera intent
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        // Launch intent
        startActivityForResult(intent, IMAGE_CAPTURE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Callback from camera intent
        if (resultCode == Activity.RESULT_OK){
            // Set image captured to image view
            view_photo_activity_imageview_picture?.setImageURI(imageUri)
            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(
                this.contentResolver, Uri.parse(
                    imageUri.toString()
                )
            )

            //Create a FirebaseVisionImage object from your image/bitmap.
            val firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap)
            val firebaseVision = FirebaseVision.getInstance()
            val firebaseVisionTextRecognizer = firebaseVision.onDeviceTextRecognizer

            //process the image
            val task: Task<FirebaseVisionText> = firebaseVisionTextRecognizer.processImage(
                firebaseVisionImage
            )

            task.addOnSuccessListener { firebaseVisionText ->
                val text: String = firebaseVisionText.text.toString()
                //showMessage(text)
                view_photo_activity_image_text_with_firebase_ml.text = text
            }

            task.addOnFailureListener { e ->
                showMessage(e.message.toString())
            }
            imageToText(imageUri)
        }
        else {
            // Failed to take picture
            showAlert("Failed to take camera picture")
        }
    }

    private fun deleteImageFromStorage(filename: String) {
        if (!filename.equals(null)) {
            mStorage.child("/images/$filename").delete().addOnSuccessListener {
            }.addOnFailureListener {
                showMessage(it.message.toString())
            }
        }
    }

    private fun imageToText(data: Uri?) {
        val dialog = ProgressDialog(this)
        dialog.setMessage("Uploading...")
        dialog.show()
        var imageLink: String
        val filename = UUID.randomUUID().toString()
        val ref: StorageReference = mStorage.child("/images/$filename")

        ref.putFile(data!!).addOnSuccessListener {
            val uriTask: Task<Uri> = it.storage.downloadUrl
            while (!uriTask.isComplete) ;
            val uri = uriTask.result
            imageLink = uri.toString()
            dialog.dismiss()

            if (imageLink == "") {
                showMessage("Image not uploaded")
            } else {
                val requestBody = FormBody.Builder().add("image", imageLink).build()
                val request: Request = Request.Builder().url(url+POST).post(requestBody).build()

                // making call asynchronously
                okHttpClient.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        runOnUiThread { showMessage("server error") }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        this@ViewPhotoActivity.runOnUiThread {
                            view_photo_activity_image_text_with_python_api.text =
                                response.body!!.string()
                            deleteImageFromStorage(filename)
                        }
                    }
                })
            }
        }.addOnProgressListener {
            val progress: Double = (100.0 * it.bytesTransferred)/it.totalByteCount
            dialog.setMessage("File uploaded...$progress%")
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showAlert(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
        builder.setPositiveButton("OK", null)

        val dialog = builder.create()
        dialog.show()
    }

}