package com.example.yangstagram.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.example.yangstagram.R
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    val PICK_IMAGE_FROM_ALBUM = 0

    lateinit var storage: FirebaseStorage
    lateinit var photoUri: Uri
    lateinit var photoImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        photoImage = findViewById(R.id.addphoto_image)

        // Initiate storage
        storage = FirebaseStorage.getInstance()

        // Open the album
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        // Add image upload event
        val addphotoUploadButton = findViewById<Button>(R.id.addphoto_btn_upload)
        addphotoUploadButton.setOnClickListener {
            contentUpload()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            if (resultCode == Activity.RESULT_OK) {
                // This is path to the selected image
                photoUri = data?.data!!
                photoImage.setImageURI(photoUri)
            } else {
                // Exit the addPhotoActivity if you leave the album without selecting it
                finish()
            }
        }
    }

    fun contentUpload() {
        // Make filename
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "IMAGE_" + timestamp + "_.png"

        val storageRef = storage.reference.child("images").child(imageFileName)

        // File Upload
        storageRef.putFile(photoUri).addOnSuccessListener {
            Toast.makeText(this, getString(R.string.upload_success), Toast.LENGTH_LONG).show()
            finish()
        }
    }
}