package com.example.booksdiary

import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.PDFView
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class ReadPdfActivity : AppCompatActivity() {
    private lateinit var pdfView: PDFView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_pdf)

        val pdfUrl = intent.getStringExtra("pdf")

        pdfView = findViewById(R.id.read_pdf_activity_pdfview)
        RetrivePDFFromURL(pdfView).execute(pdfUrl)
    }
}

class RetrivePDFFromURL(pdfView: PDFView?) : AsyncTask<String, Void, InputStream>() {
    private val mypdfView: PDFView = pdfView!!

    override fun doInBackground(vararg params: String?): InputStream? {
        // on below line we are creating a variable for our input stream.
        var inputStream: InputStream? = null
        try {
            // on below line we are creating an url
            // for our url which we are passing as a string.
            val url = URL(params[0])

            // on below line we are creating our http url connection.
            val urlConnection: HttpURLConnection = url.openConnection() as HttpsURLConnection

            // on below line we are checking if the response
            // is successful with the help of response code
            // 200 response code means response is successful
            if (urlConnection.responseCode == 200) {
                // on below line we are initializing our input stream
                // if the response is successful.
                inputStream = BufferedInputStream(urlConnection.inputStream)
            }
        }
        // on below line we are adding catch block to handle exception
        catch (e: Exception) {
            // on below line we are simply printing
            // our exception and returning null
            e.printStackTrace()
            return null
        }
        // on below line we are returning input stream.
        return inputStream
    }

    // on below line we are calling on post execute
    // method to load the url in our pdf view.
    override fun onPostExecute(result: InputStream?) {
        // on below line we are loading url within our
        // pdf view on below line using input stream.
        mypdfView.fromStream(result).load()
    }

}
