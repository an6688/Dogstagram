package com.example.dogstagram

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.listeners.ProgressListener
import com.example.util.StatusListElement
import com.example.util.populateImage
import kotlinx.android.synthetic.main.activity_status.*

class StatusActivity : AppCompatActivity(), ProgressListener {

    private lateinit var statusElement: StatusListElement

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status)

        if(intent.hasExtra(PARAM_STATUS_ELEMENT)) {
            statusElement = intent.getParcelableExtra(PARAM_STATUS_ELEMENT)
        } else {
            Toast.makeText(this, "Unable to get status", Toast.LENGTH_SHORT).show()
            finish()
        }

        statusTV.text = statusElement.status
        populateImage(this, statusElement.statusUrl, statusIV)

        progressBar.max = 500
        TimerTask(this).execute("")
    }

    override fun onProgressUpdate(progress: Int) {
        progressBar.progress = progress
        if(progress == 500) {
            finish()
        }
    }

    private class TimerTask(val listener: ProgressListener): AsyncTask<String, Int, Any>() {
        override fun doInBackground(vararg params: String?) {
            var i = 0
            val sleep = 20L
            while (i < 500) {
                i++
                publishProgress(i)
                Thread.sleep(sleep)
            }
        }

        override fun onProgressUpdate(vararg values: Int?) {
            if(values[0] != null) {
                listener.onProgressUpdate(values[0]!!)
            }
        }
    }

    companion object {
        val PARAM_STATUS_ELEMENT = "element"

        fun getIntent(context: Context?, statusElement: StatusListElement): Intent {
            val intent = Intent(context, StatusActivity::class.java)
            intent.putExtra(PARAM_STATUS_ELEMENT, statusElement)
            return intent
        }
    }
}