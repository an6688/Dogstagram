package com.example.dogstagram

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.adapter.StatusListAdapter
import com.example.listeners.StatusItemClickListener

import com.example.util.StatusListElement
import com.example.util.User

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_status_list.*

class StatusListFragment : Fragment(), StatusItemClickListener {

    private val firebaseDb = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var statusListAdapter = StatusListAdapter(arrayListOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_status_list, container, false)
    }

    override fun onItemClicked(statusElement: StatusListElement) {
        startActivity(StatusActivity.getIntent(context, statusElement))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        statusListAdapter.setOnItemClickListener(this)
        statusListRV.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = statusListAdapter
            addItemDecoration(DividerItemDecoration(this@StatusListFragment.context, DividerItemDecoration.VERTICAL))
        }
    }

    fun onVisible() {
        statusListAdapter.onRefresh()
        refreshList()
    }

    fun refreshList() {
        firebaseDb.collection("users")
            .document(userId!!)
            .get()
            .addOnSuccessListener { doc ->
                if(doc.contains("userChats")) {
                    val partners = doc["userChats"]
                    if (partners != null)
                    {
                        for(partner in (partners as HashMap<String, String>).keys) {
                            firebaseDb.collection("users")
                                .document(partner)
                                .get()
                                .addOnSuccessListener { documentSnapshot ->
                                    val partner = documentSnapshot.toObject(User::class.java)
                                    if(partner != null) {
                                        if(!partner.status.isNullOrEmpty() || !partner.statusUrl.isNullOrEmpty()) {
                                            val newElement =
                                                StatusListElement(
                                                    partner.name,
                                                    partner.imageUrl,
                                                    partner.status,
                                                    partner.statusUrl,
                                                    partner.statusTime
                                                )
                                            statusListAdapter.addElement(newElement)
                                        }
                                    }
                                }
                        }
                    }

                }
            }
    }
}