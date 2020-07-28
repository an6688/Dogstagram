package com.fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.dogstagram.MainView
import com.example.dogstagram.R
import com.example.util.getTime
import com.example.util.populateImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_status_update.*


class StatusUpdateFragment : Fragment() {

    private val firebaseDB = FirebaseFirestore.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance().reference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var imageUrl = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_status_update, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressLayout.setOnTouchListener { v, event -> true }
        sendStatusButton.setOnClickListener { onUpdate() }
        populateImage(context, imageUrl, statusIV)

        statusLayout.setOnClickListener {
            if(isAdded) {
                (activity as MainView).startNewActivity(23764)
            }
        }
    }

    fun storeImage(imageUri: Uri?) {
        if(imageUri != null && userId != null) {
            Toast.makeText(activity, "Uploading...", Toast.LENGTH_SHORT).show()
            progressLayout.visibility = View.VISIBLE
            val filePath = firebaseStorage.child("Images").child("${userId}_status")

            filePath.putFile(imageUri)
                .addOnSuccessListener {
                    filePath.downloadUrl
                        .addOnSuccessListener { taskSnapshot ->
                            val url = taskSnapshot.toString()
                            firebaseDB.collection("users")
                                .document(userId)
                                .update("status", url)
                                .addOnSuccessListener {
                                    imageUrl = url
                                    populateImage(context, imageUrl, statusIV)
                                }
                            progressLayout.visibility = View.GONE
                        }
                        .addOnFailureListener { onUploadFailure() }
                }
                .addOnFailureListener { onUploadFailure() }
        }
    }

    fun onUpdate() {
        progressLayout.visibility = View.VISIBLE
        val map = HashMap<String, Any>()
        map["status"] = statusET.text.toString()
        map["statusUrl"] = imageUrl
        map["statusTime"] = getTime()

        firebaseDB.collection("users")
            .document(userId!!)
            .update(map)
            .addOnSuccessListener {
                progressLayout.visibility = View.GONE
                Toast.makeText(activity, "Status updated.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                progressLayout.visibility = View.GONE
                Toast.makeText(activity, "Status update failed.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun onUploadFailure() {
        Toast.makeText(activity, "Image upload failed. Please try again later", Toast.LENGTH_SHORT).show()
        progressLayout.visibility = View.GONE
    }


}