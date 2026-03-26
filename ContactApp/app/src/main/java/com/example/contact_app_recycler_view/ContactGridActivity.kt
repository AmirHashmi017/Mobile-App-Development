package com.example.contact_app_recycler_view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ContactGridActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_contact_grid)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val contacts = intent.getParcelableArrayListExtra<Contact>("contacts") ?: arrayListOf()

        val tvCount = findViewById<TextView>(R.id.tvGridCount)
        tvCount.text = "${contacts.size} Contacts"

        val recycler = findViewById<RecyclerView>(R.id.recyclerGrid)
        recycler.layoutManager = GridLayoutManager(this, 3)
        recycler.adapter = GridAdapter(contacts)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
    }

    class GridAdapter(private val contacts: List<Contact>) :
        RecyclerView.Adapter<GridAdapter.GridViewHolder>() {

        class GridViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivAvatar: ImageView = view.findViewById(R.id.ivGridAvatar)
            val tvName: TextView = view.findViewById(R.id.tvGridName)
            val tvPhone: TextView = view.findViewById(R.id.tvGridPhone)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_contact_grid, parent, false)
            return GridViewHolder(view)
        }

        override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
            val contact = contacts[position]
            holder.tvName.text = contact.name
            holder.tvPhone.text = contact.phone
            AvatarHelper.setAvatarInitial(holder.ivAvatar, contact.name)
        }

        override fun getItemCount(): Int = contacts.size
    }
}