package com.example.dogstagram

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_profile.*
import android.app.AlertDialog
import android.net.Uri
import android.widget.TextView
import com.example.util.*
import kotlinx.android.synthetic.main.activity_profile.emailET
import kotlinx.android.synthetic.main.activity_profile.progressLayout


class ProfileActivity : AppCompatActivity() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance().reference
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var imageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        if (userId.isNullOrEmpty()) {
            finish()
        }

        progressLayout.setOnTouchListener { v, event -> true  }

        photoIV.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_PHOTO)
        }
        populateInfo()
    }

    private fun populateInfo() {
        progressLayout.visibility = View.GONE
        firebaseDB.collection("users")
            .document(userId!!)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                imageUrl = user?.imageUrl
                nameET.setText(user?.name, TextView.BufferType.EDITABLE)
                emailET.setText(user?.email, TextView.BufferType.EDITABLE)
                phoneET.setText(user?.phone, TextView.BufferType.EDITABLE)
                if (imageUrl != null) {
                    populateImage(this, user?.imageUrl, photoIV, R.drawable.default_user)
                }
                progressLayout.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                finish()
            }
    }

    fun onApply(view: View) {
        progressLayout.visibility = View.GONE
        val name = nameET.text.toString()
        val email = emailET.text.toString()
        val phone = phoneET.text.toString()
        val map = HashMap<String, Any>()
        map["name"] = name
        map["email"] = email
        map["phone"] = phone
        map["imageUrl"] = imageUrl!!

        firebaseDB.collection("users")
            .document(userId!!)
            .update(map)
            .addOnSuccessListener {
                Toast.makeText(this, "Update successful", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                progressLayout.visibility = View.GONE
            }
    }
    fun onDelete(view: View) {
        progressLayout.visibility = View.VISIBLE
        AlertDialog.Builder(this)
            .setTitle("Delete account")
            .setMessage("This will delete your profile information. Are you sure?")
            .setPositiveButton("Yes") { dialog, which ->
                Toast.makeText(this, "Profile deleted", Toast.LENGTH_SHORT).show()
                firebaseDB.collection("Users").document(userId!!).delete()
                firebaseStorage.child("images").child(userId).delete()
                firebaseAuth.currentUser?.delete()
                    ?.addOnSuccessListener {
                        finish()
                    }
                    ?.addOnFailureListener {
                        finish()
                    }
            }
            .setNegativeButton("No") { dialog, which ->
                progressLayout.visibility = View.GONE
            }
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PHOTO) {
            storeImage(data?.data)
        }
    }

    private fun storeImage(imageUri: Uri?) {
        if (imageUri != null) {
            progressLayout.visibility = View.VISIBLE
            val filePath = firebaseStorage.child(DATA_IMAGES).child(userId!!)
            filePath.putFile(imageUri)
                .addOnSuccessListener {
                    Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show()
                    it.storage.downloadUrl.addOnSuccessListener { uri ->
                        val url = uri.toString()
                        firebaseDB.collection(DATA_USERS)
                            .document(userId)
                            .update(DATA_USER_IMAGE_URL, url)

                        this.imageUrl = url
                        populateImage(this, this.imageUrl, photoIV, R.drawable.default_user)

                    }.addOnFailureListener{
                        onUploadFailure()
                    }
                }
                .addOnFailureListener{
                    onUploadFailure()
                }
            progressLayout.visibility = View.GONE
        }
    }

    private fun onUploadFailure() {
        Toast.makeText(this, "Image upload failed. Please try again later.", Toast.LENGTH_SHORT).show()
        progressLayout.visibility = View.GONE
    }


    companion object {
        fun newIntent(context: Context) = Intent(context, ProfileActivity::class.java)
    }
}