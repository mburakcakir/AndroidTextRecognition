package com.mburakcakir.androidtextrecognition

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import kotlinx.android.synthetic.main.activity_main.*
import java.io.FileNotFoundException

class KotlinTextRecognitionActivity : AppCompatActivity() {
    var imageBitmap: Bitmap? = null
    var selectedImage: Bitmap? = null
    var detectedText: String = ""

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_OPEN_IMAGE = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    fun init() {
        btnCaptureImage?.setOnClickListener {
            dispatchTakePictureIntent()
            txtDisplay!!.text = ""
        }
        btnDetectTextImage?.setOnClickListener {
            runTextRecognition()
        }
        btnOpenImage?.setOnClickListener {
            openGallery()
        }
        btnShareText?.setOnClickListener {
            shareText(detectedText)
        }
    }

    private fun shareText(text: String?) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    fun runTextRecognition() {
        val firebaseVisionImage = FirebaseVisionImage.fromBitmap((imgRecognition.drawable as BitmapDrawable).bitmap)
        FirebaseVision.getInstance().onDeviceTextRecognizer.apply {
            this.processImage(firebaseVisionImage).addOnSuccessListener { firebaseVisionText ->
                processTextRecognitionResult(firebaseVisionText)
            }.addOnFailureListener { e ->
                Toast.makeText(this@KotlinTextRecognitionActivity, "Error : " + e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun processTextRecognitionResult(firebaseVisionText: FirebaseVisionText) {
        val textBlockList = firebaseVisionText.textBlocks
        if (textBlockList.size == 0) {
            Toast.makeText(this, "No Text Found in image", Toast.LENGTH_SHORT).show()
        } else {
            for (textBlock in textBlockList) {
                detectedText += textBlock.text
            }
            txtDisplay!!.text = detectedText
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun openGallery() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(photoPickerIntent, REQUEST_OPEN_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val extras = data?.extras
            imageBitmap = extras?.get("data") as Bitmap?
            imgRecognition.setImageBitmap(imageBitmap)
            detectedText = ""
        } else if (requestCode == REQUEST_OPEN_IMAGE && resultCode == Activity.RESULT_OK) {
            try {
                val imageUri = data?.data
                val imageStream = imageUri?.let { contentResolver.openInputStream(it) }
                selectedImage = BitmapFactory.decodeStream(imageStream)
                imgRecognition.setImageBitmap(selectedImage)
                detectedText = ""
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
            }
        }
    }


}