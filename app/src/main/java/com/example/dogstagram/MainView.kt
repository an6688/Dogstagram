package com.example.dogstagram

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import com.example.listeners.FailureCallback
import com.example.util.*


import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_user_list.*



class MainView : AppCompatActivity(), FailureCallback {

    private val firebaseDB = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var mSectionsPagerAdapter: SectionsPagerAdapter? = null

    private val statusUpdateFragment =
        StatusUpdateFragment() // this is the cmaera icon
    private val statusListFragment =
        StatusListFragment() // this is to see other users updates and such
    private val chatsFragment = ChatsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        chatsFragment.setFailureCallbackListener(this)

        setSupportActionBar(toolbar)
        mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        container.adapter = mSectionsPagerAdapter
        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))

        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
        resizeTabs()
        tabs.getTabAt(1)?.select()

       tabs.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }

           override fun onTabSelected(tab: TabLayout.Tab?) {
               when(tab?.position) {
                   0 -> {fab.hide()}
                   1 -> {fab.show()}
                   2 -> {
                   fab.hide()
                   statusListFragment.onVisible()
               }}}
       })
    }

    override fun onUserError() {
        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
        startActivity(LoginActivity.newIntent(this))
        finish()
    }

    private fun resizeTabs() {
        val layout = (tabs.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
        val layoutParams = layout.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 0.4f
        layout.layoutParams = layoutParams
    }

    fun onNewChat(view: View) {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                AlertDialog.Builder(this)
                    .setTitle("Contacts permission")
                    .setMessage("This app requires access to your contacts to initiate a conversation.")
                    .setPositiveButton("Ask me") { dialog, which -> requestContactsPermission() }
                    .setNegativeButton("No") { dialog, which ->  }
                    .show()
            } else {
                requestContactsPermission()
            }
        } else {
            // Permission granted
            startNewActivity(REQUEST_NEW_CHAT)
        }
    }

    fun requestContactsPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), PERMISSIONS_REQUEST_READ_CONTACTS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            PERMISSIONS_REQUEST_READ_CONTACTS -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startNewActivity(REQUEST_NEW_CHAT)
                }
            }
        }
    }

    fun startNewActivity(requestCode: Int) {
        when(requestCode) {
            REQUEST_NEW_CHAT -> startActivityForResult(ContactsActivity.newIntent(this), REQUEST_NEW_CHAT)
            REQUEST_CODE_PHOTO -> {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, REQUEST_CODE_PHOTO)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_PHOTO -> statusUpdateFragment.storeImage(data?.data)
                REQUEST_NEW_CHAT -> {
                    val name = data?.getStringExtra(PARAM_NAME) ?: ""
                    val phone = data?.getStringExtra(PARAM_PHONE) ?: ""
                    checkNewChatUser(name, phone)
                }
            }
        }
    }

    private fun checkNewChatUser(name: String, phone: String) {
        if(!name.isNullOrEmpty() && !phone.isNullOrEmpty()) {
            firebaseDB.collection("users")
                .whereEqualTo(DATA_USER_PHONE, phone)
                .get()
                .addOnSuccessListener { result ->
                    if(result.documents.size > 0) {
                        chatsFragment.newChat(result.documents[0].id)
                    } else {
                        AlertDialog.Builder(this)
                            .setTitle("User not found")
                            .setMessage("$name does not have an account. Send them an SMS to install this app.")
                            .setPositiveButton("OK") { dialog, which ->
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.data = Uri.parse("sms:$phone")
                                intent.putExtra("sms_body", "Hi. I'm using this new app. You should install it too so we can bark together.")
                                startActivity(intent)
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "An error occurred. Please try again later", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
        }

    }

    override fun onResume() {
        super.onResume()

        if(firebaseAuth.currentUser == null) {
            startActivity(LoginActivity.newIntent(this))
            finish()
        }
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

    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            return when(position) {

                0 -> statusUpdateFragment
                1 -> chatsFragment
                2 -> statusListFragment
                else -> statusListFragment
            }
        }

        override fun getCount(): Int {
            return 3
        }
    }

    companion object {
        val PARAM_NAME = "Param name"
        val PARAM_PHONE = "Param phone"

        fun newIntent(context: Context) = Intent(context, MainView::class.java)
    }
}