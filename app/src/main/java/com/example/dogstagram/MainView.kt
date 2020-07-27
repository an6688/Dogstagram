package com.example.dogstagram

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.parse.ParseFile
import com.parse.ParseObject
import com.parse.ParseUser
import java.io.ByteArrayOutputStream
import java.util.*

class MainView : AppCompatActivity() {

    private val firebaseDB = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    val photo: Unit
        get() {
            val intent =
                Intent(ACTION_PICK, EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 1)
        }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater: MenuInflater = menuInflater
        menuInflater.inflate(R.menu.share_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            R.id.profile -> onProfile()
            R.id.logout -> onLogout()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun onProfile(){
        startActivity(ProfileActivity.newIntent(this)) // it just starts profile activity
    }

    private fun onLogout() {
        firebaseAuth.signOut()
        startActivity(LoginActivity.newIntent(this))
        finish()
    }

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)
        title = "User Feed"
        val listView: ListView = findViewById(R.id.listView)
        val usernames = ArrayList<String?>()
        /*val arrayAdapter: ArrayAdapter<*> =
            ArrayAdapter<Any?>(this, R.layout.simple_list_item_1, usernames)*/
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, usernames)
        listView.onItemClickListener =
            OnItemClickListener { _, _, i, _ ->
                val intent = Intent(applicationContext, UserFeed::class.java)
                intent.putExtra("username", usernames[i])
                startActivity(intent)
            }
       /* val query = ParseUser.getQuery()
        query.whereNotEqualTo("username", ParseUser.getCurrentUser().username)
        query.addAscendingOrder("username")
        query.findInBackground { objects, e ->
            if (e == null) {
                if (objects.size > 0) {
                    for (user in objects) {
                        usernames.add(user.username)
                    }
                    listView.adapter = adapter
                }
            } else {
                e.printStackTrace()
            }
        }*/
    }
}