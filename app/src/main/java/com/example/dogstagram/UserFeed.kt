package com.example.dogstagram

import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Layout
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.parse.ParseFile
import com.parse.ParseObject
import com.parse.ParseQuery


class UserFeed : AppCompatActivity() {
    var linLayout: LinearLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_feed)
        val intent = intent
        val username = intent.getStringExtra("username")
        title = "$username's Photos"
        linLayout = findViewById(R.id.linLayout)
        val query = ParseQuery<ParseObject>("Image")
        query.whereEqualTo("username", username)
        query.orderByDescending("createdAt")
        query.findInBackground { objects, e ->
            if (e == null && objects.size > 0) {
                for (`object` in objects) {
                    val file = `object`["image"] as ParseFile
                    file.getDataInBackground { data, e ->
                        if (e == null && data != null) {
                            val bitmap =
                                BitmapFactory.decodeByteArray(data, 0, data.size)
                            val imageView =
                                ImageView(applicationContext)
                            imageView.layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            imageView.setImageBitmap(bitmap)
                            lateinit var linLayout : Layout
                        }
                    }
                }
            }
        }
    }
}
