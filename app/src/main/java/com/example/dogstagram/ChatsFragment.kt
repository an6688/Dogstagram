package com.example.dogstagram

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adapter.ChatsAdapter
import com.example.listeners.ChatClickListener
import com.example.listeners.FailureCallback
import com.example.util.Chat
import com.example.util.DATA_CHATS
import com.example.util.DATA_USER_CHATS
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_chats.*

class ChatsFragment : Fragment(), ChatClickListener {

    private var chatsAdapter = ChatsAdapter(arrayListOf())
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var failureCallback: FailureCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(userId.isNullOrEmpty()) {
            failureCallback?.onUserError()
        }
    }

    fun setFailureCallbackListener(listener: FailureCallback) {
        failureCallback = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatsAdapter.setOnItemClickListener(this)
        chatsRV.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = chatsAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        firebaseDB.collection("users").document(userId!!).addSnapshotListener {
                documentSnapshot,
                firebaseFirestoreException ->
            if(firebaseFirestoreException == null) {
                refreshChats()
            }
        }
    }

    private fun refreshChats() {
        firebaseDB.collection("users")
            .document(userId!!)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.contains("userChats")) {
                    val partners = documentSnapshot["userChats"]
                    val chats = arrayListOf<String>()
                    if (partners != null)
                    {
                        for(partner in (partners as HashMap<Any, Any>).keys) {
                            if(partners[partner] != null) {
                                chats.add((partners[partner] as String?)!!)
                            }
                        }
                    }

                    chatsAdapter.updateChats(chats)
                }
            }
    }

    fun newChat(partnerId: String) {
        firebaseDB.collection("users")
            .document(userId!!)
            .get()
            .addOnSuccessListener {userDocument ->
                val userChatPartners = hashMapOf<String, String>()
                if(userDocument[DATA_USER_CHATS] != null && userDocument[DATA_USER_CHATS] is HashMap<*, *>) {
                    val userDocumentMap = userDocument[DATA_USER_CHATS] as HashMap<String, String>
                    if(userDocumentMap.containsKey(partnerId)) {
                        return@addOnSuccessListener
                    } else {
                        userChatPartners.putAll(userDocumentMap)
                    }
                }

                firebaseDB.collection("users")
                    .document(partnerId)
                    .get()
                    .addOnSuccessListener { partnerDocument ->
                        val partnerChatPartners = hashMapOf<String, String>()
                        if(partnerDocument[DATA_USER_CHATS] != null && partnerDocument[DATA_USER_CHATS] is HashMap<*, *>) {
                            val partnerDocumentMap = partnerDocument[DATA_USER_CHATS] as HashMap<String, String>
                            partnerChatPartners.putAll(partnerDocumentMap)
                        }

                        val chatParticipants = arrayListOf(userId, partnerId)
                        val chat = Chat(chatParticipants)
                        val chatRef = firebaseDB.collection(DATA_CHATS).document()
                        val userRef = firebaseDB.collection("users").document(userId)
                        val partnerRef = firebaseDB.collection("users").document(partnerId)

                        userChatPartners[partnerId] = chatRef.id
                        partnerChatPartners[userId] = chatRef.id

                        val batch = firebaseDB.batch()
                        batch.set(chatRef, chat)
                        batch.update(userRef, DATA_USER_CHATS, userChatPartners)
                        batch.update(partnerRef, DATA_USER_CHATS, partnerChatPartners)
                        batch.commit()
                    }
                    .addOnFailureListener {e ->
                        e.printStackTrace()
                    }
            }
            .addOnFailureListener {e ->
                e.printStackTrace()
            }

    }

    override fun onChatClicked(chatId: String?, otherUserId: String?, chatImageUrl: String?, chatName: String?) {
        startActivity(ConversationActivity.newIntent(context, chatId, chatImageUrl, otherUserId, chatName))
    }

}