package com.example.dogstagram

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
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_login.*
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
        populateInfo()
    }

    private fun populateInfo() {
        progressLayout.visibility = View.GONE
        firebaseDB.collection("users")
            .document(userId!!)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                // imageUrl = user?.imageUrl
                nameET.setText(user?.name, TextView.BufferType.EDITABLE)
                emailET.setText(user?.email, TextView.BufferType.EDITABLE)
                phoneET.setText(user?.phone, TextView.BufferType.EDITABLE)
                progressLayout.visibility = View.GONE
                /*if (imageUrl != null) {
                    populateImage(this, user?.imageUrl, photoIV, R.drawable.default_user)
                }*/
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                finish()
            }
    }

    fun onApply(view: View) {
        // progressLayout.visibility = View.GONE
        progressLayout.visibility = View.GONE
        val name = nameET.text.toString()
        val email = emailET.text.toString()
        val phone = phoneET.text.toString()
        val map = HashMap<String, Any>()
        map["name"] = name
        map["email"] = email
        map["phone"] = phone

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

    companion object {
        fun newIntent(context: Context) = Intent(context, ProfileActivity::class.java)
    }
}