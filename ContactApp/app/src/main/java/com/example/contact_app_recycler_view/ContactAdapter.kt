package com.example.contact_app_recycler_view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContactAdapter(
    private val contactList: MutableList<Contact>,
    private val listener: OnContactActionListener
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    interface OnContactActionListener {
        fun onItemClick(position: Int)
        fun onEditClick(position: Int)
        fun onDeleteClick(position: Int)
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvContactName: TextView = itemView.findViewById(R.id.tvContactName)
        val tvContactPhone: TextView = itemView.findViewById(R.id.tvContactPhone)
        val ivAvatar: ImageView = itemView.findViewById(R.id.ivAvatar)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contactList[position]
        holder.tvContactName.text = contact.name
        holder.tvContactPhone.text = contact.phone

        // Show real photo if available, else initials
        AvatarHelper.setAvatar(holder.ivAvatar, contact.name, contact.imagePath)

        holder.itemView.setOnClickListener { listener.onItemClick(position) }
        holder.btnEdit.setOnClickListener { listener.onEditClick(position) }
        holder.btnDelete.setOnClickListener { listener.onDeleteClick(position) }
    }

    override fun getItemCount(): Int = contactList.size
}