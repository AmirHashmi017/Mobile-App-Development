package com.example.contact_app_recycler_view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity(), ContactAdapter.OnContactActionListener {

    private lateinit var etName: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnSave: Button
    private lateinit var btnLoadContacts: Button
    private lateinit var btnGridView: Button
    private lateinit var btnSortAsc: ImageButton
    private lateinit var btnSortDesc: ImageButton
    private lateinit var etSearch: EditText
    private lateinit var recyclerViewContacts: RecyclerView
    private lateinit var ivAddAvatar: ImageView
    private lateinit var tvAddAvatarHint: TextView

    private lateinit var contactAdapter: ContactAdapter
    private val contactList = mutableListOf<Contact>()
    private val filteredList = mutableListOf<Contact>()

    private var currentSortOrder: SortOrder = SortOrder.NONE

    // Tracks which image picker call is active: "add" or "edit"
    private var pickerMode: String = "add"
    private var editDialogImageView: ImageView? = null
    private var pendingAddImagePath: String? = null
    private var pendingEditImagePath: String? = null

    enum class SortOrder { NONE, ASC, DESC }

    // ── Image picker launcher ──────────────────────────────────────────────────
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri == null) return@registerForActivityResult
            val savedPath = saveImageToInternalStorage(uri) ?: return@registerForActivityResult
            if (pickerMode == "add") {
                pendingAddImagePath = savedPath
                AvatarHelper.setAvatar(ivAddAvatar, etName.text.toString(), savedPath)
                tvAddAvatarHint.text = "Tap to change photo"
            } else {
                pendingEditImagePath = savedPath
                editDialogImageView?.let {
                    AvatarHelper.setAvatar(it, "", savedPath)
                }
            }
        }

    // ── Contacts permission ────────────────────────────────────────────────────
    private val requestContactsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) loadContactsFromPhone()
            else Toast.makeText(this, "Contacts permission denied", Toast.LENGTH_SHORT).show()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        etName = findViewById(R.id.etName)
        etPhone = findViewById(R.id.etPhone)
        btnSave = findViewById(R.id.btnSave)
        btnLoadContacts = findViewById(R.id.btnLoadContacts)
        btnGridView = findViewById(R.id.btnGridView)
        btnSortAsc = findViewById(R.id.btnSortAsc)
        btnSortDesc = findViewById(R.id.btnSortDesc)
        etSearch = findViewById(R.id.etSearch)
        recyclerViewContacts = findViewById(R.id.recyclerViewContacts)
        ivAddAvatar = findViewById(R.id.ivAddAvatar)
        tvAddAvatarHint = findViewById(R.id.tvAddAvatarHint)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        contactAdapter = ContactAdapter(filteredList, this)
        recyclerViewContacts.layoutManager = LinearLayoutManager(this)
        recyclerViewContacts.adapter = contactAdapter

        // Add-contact avatar picker
        ivAddAvatar.setOnClickListener {
            pickerMode = "add"
            imagePickerLauncher.launch("image/*")
        }

        // Update avatar preview live as name is typed (only when no photo selected)
        etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (pendingAddImagePath == null) {
                    AvatarHelper.setAvatarInitial(ivAddAvatar, s.toString())
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnSave.setOnClickListener { saveContact() }
        btnLoadContacts.setOnClickListener { checkPermissionAndLoadContacts() }

        btnGridView.setOnClickListener {
            val intent = Intent(this, ContactGridActivity::class.java)
            intent.putParcelableArrayListExtra("contacts", ArrayList(contactList))
            startActivity(intent)
        }

        btnSortAsc.setOnClickListener {
            currentSortOrder = SortOrder.ASC
            btnSortAsc.alpha = 1.0f
            btnSortDesc.alpha = 0.4f
            applySearchAndSort()
        }

        btnSortDesc.setOnClickListener {
            currentSortOrder = SortOrder.DESC
            btnSortDesc.alpha = 1.0f
            btnSortAsc.alpha = 0.4f
            applySearchAndSort()
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { applySearchAndSort() }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Show default avatar placeholder on launch
        AvatarHelper.setAvatarInitial(ivAddAvatar, "")
    }

    // ── Save contact ───────────────────────────────────────────────────────────
    private fun saveContact() {
        val name = etName.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        if (!validateInputs(name, phone, etName, etPhone)) return

        val newContact = Contact(name, phone, pendingAddImagePath)
        contactList.add(newContact)
        applySearchAndSort()
        recyclerViewContacts.scrollToPosition(filteredList.size - 1)

        Toast.makeText(this, "Contact saved!", Toast.LENGTH_SHORT).show()
        etName.text.clear()
        etPhone.text.clear()
        pendingAddImagePath = null
        AvatarHelper.setAvatarInitial(ivAddAvatar, "")
        tvAddAvatarHint.text = "Tap to add photo"
        etName.requestFocus()
    }

    // ── Search & Sort ──────────────────────────────────────────────────────────
    private fun applySearchAndSort() {
        val query = etSearch.text.toString().trim().lowercase()
        var result = if (query.isEmpty()) contactList.toMutableList()
        else contactList.filter { it.name.lowercase().contains(query) || it.phone.contains(query) }.toMutableList()

        result = when (currentSortOrder) {
            SortOrder.ASC -> result.sortedBy { it.name.lowercase() }.toMutableList()
            SortOrder.DESC -> result.sortedByDescending { it.name.lowercase() }.toMutableList()
            SortOrder.NONE -> result
        }
        filteredList.clear()
        filteredList.addAll(result)
        contactAdapter.notifyDataSetChanged()
    }

    // ── Validation ─────────────────────────────────────────────────────────────
    private fun validateInputs(name: String, phone: String, nameInput: EditText, phoneInput: EditText): Boolean {
        var isValid = true
        if (name.isEmpty()) { nameInput.error = "Name is required"; isValid = false }
        if (phone.isEmpty()) { phoneInput.error = "Phone number is required"; isValid = false }
        else if (phone.length < 10 || !phone.all { it.isDigit() || it == '+' }) {
            phoneInput.error = "Enter valid phone number"; isValid = false
        }
        return isValid
    }

    // ── Adapter callbacks ──────────────────────────────────────────────────────
    override fun onItemClick(position: Int) {
        val contact = filteredList[position]
        Toast.makeText(this, "${contact.name}\n${contact.phone}", Toast.LENGTH_SHORT).show()
    }

    override fun onEditClick(position: Int) { showEditDialog(position) }
    override fun onDeleteClick(position: Int) { showDeleteDialog(position) }

    // ── Delete dialog ──────────────────────────────────────────────────────────
    private fun showDeleteDialog(position: Int) {
        val contact = filteredList[position]
        AlertDialog.Builder(this)
            .setTitle("Delete Contact")
            .setMessage("Delete ${contact.name}?")
            .setPositiveButton("Delete") { _, _ ->
                contactList.remove(contact)
                applySearchAndSort()
                Toast.makeText(this, "Contact deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ── Edit dialog ────────────────────────────────────────────────────────────
    private fun showEditDialog(position: Int) {
        val contact = filteredList[position]
        val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_dialog_edit_item, null)
        val etEditName = dialogView.findViewById<EditText>(R.id.etEditName)
        val etEditPhone = dialogView.findViewById<EditText>(R.id.etEditPhone)
        val ivEditAvatar = dialogView.findViewById<ImageView>(R.id.ivEditAvatar)
        val tvEditAvatarHint = dialogView.findViewById<TextView>(R.id.tvEditAvatarHint)

        etEditName.setText(contact.name)
        etEditPhone.setText(contact.phone)

        // Reset pending path for this edit session
        pendingEditImagePath = contact.imagePath
        editDialogImageView = ivEditAvatar

        // Show current avatar
        AvatarHelper.setAvatar(ivEditAvatar, contact.name, contact.imagePath)
        tvEditAvatarHint.text = if (contact.imagePath != null) "Tap to change photo" else "Tap to add photo"

        // Tap avatar to pick image
        ivEditAvatar.setOnClickListener {
            pickerMode = "edit"
            imagePickerLauncher.launch("image/*")
        }

        // Update initials preview live only if no photo
        etEditName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (pendingEditImagePath == null) {
                    AvatarHelper.setAvatarInitial(ivEditAvatar, s.toString())
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Contact")
            .setView(dialogView)
            .setPositiveButton("Update", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val updatedName = etEditName.text.toString().trim()
            val updatedPhone = etEditPhone.text.toString().trim()
            if (validateInputs(updatedName, updatedPhone, etEditName, etEditPhone)) {
                val masterIdx = contactList.indexOf(contact)
                if (masterIdx >= 0) {
                    contactList[masterIdx].name = updatedName
                    contactList[masterIdx].phone = updatedPhone
                    contactList[masterIdx].imagePath = pendingEditImagePath
                }
                applySearchAndSort()
                Toast.makeText(this, "Contact updated", Toast.LENGTH_SHORT).show()
                editDialogImageView = null
                dialog.dismiss()
            }
        }

        dialog.setOnDismissListener { editDialogImageView = null }
    }

    // ── Load phone contacts ────────────────────────────────────────────────────
    private fun checkPermissionAndLoadContacts() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED -> loadContactsFromPhone()
            shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) -> {
                AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("This app needs permission to read your contacts.")
                    .setPositiveButton("Grant") { _, _ -> requestContactsPermission.launch(Manifest.permission.READ_CONTACTS) }
                    .setNegativeButton("Deny", null).show()
            }
            else -> requestContactsPermission.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun loadContactsFromPhone() {
        val loaded = mutableListOf<Contact>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection,
            null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        cursor?.use {
            val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val name = it.getString(nameIdx) ?: ""
                val phone = it.getString(phoneIdx) ?: ""
                if (name.isNotBlank() && phone.isNotBlank()) loaded.add(Contact(name, phone))
            }
        }
        if (loaded.isEmpty()) { Toast.makeText(this, "No contacts found", Toast.LENGTH_SHORT).show(); return }
        contactList.clear(); contactList.addAll(loaded)
        applySearchAndSort()
        Toast.makeText(this, "${loaded.size} contacts loaded", Toast.LENGTH_SHORT).show()
    }

    // ── Image storage helper ───────────────────────────────────────────────────
    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: filesDir
            val file = File(dir, "contact_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}