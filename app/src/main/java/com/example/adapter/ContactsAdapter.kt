package com.example.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dogstagram.R
import com.example.listeners.ContactClickedListener
import com.example.util.Contact

class ContactsAdapter(val contacts: ArrayList<Contact>) : RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>() {

    private var clickListener: ContactClickedListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, p0: Int) = ContactsViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
    )

    override fun getItemCount() = contacts.size

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        holder.bind(contacts[position], clickListener)
    }

    fun setOnItemClickListener(listener: ContactClickedListener) {
        clickListener = listener
        notifyDataSetChanged()
    }

    class ContactsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private var layout = view.findViewById<LinearLayout>(R.id.contactLayout)
        private var nameTV = view.findViewById<TextView>(R.id.contactNameTV)
        private var phoneTV = view.findViewById<TextView>(R.id.contactNumberTV)

        fun bind(contact: Contact, listener: ContactClickedListener?) {
            nameTV.text = contact.name
            phoneTV.text = contact.phone
            layout.setOnClickListener { listener?.onContactClicked(contact.name, contact.phone) }
        }
    }
}