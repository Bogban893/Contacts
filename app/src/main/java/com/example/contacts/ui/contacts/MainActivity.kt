package com.example.contacts

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ContactsAdapter

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.READ_CONTACTS] != true) {
            Toast.makeText(this, "Нет доступа к контактам", Toast.LENGTH_SHORT).show()
        } else {
            loadContacts()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Contacts"
        toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white))

        val btnSearch = findViewById<ImageButton>(R.id.btnSearch)
        val searchView = findViewById<SearchView>(R.id.searchView)

        btnSearch.setOnClickListener {
            if (searchView.visibility == View.GONE) {
                searchView.visibility = View.VISIBLE
                searchView.isIconified = false
                searchView.requestFocus()
            } else {
                searchView.visibility = View.GONE
                searchView.setQuery("", false)
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })

        adapter = ContactsAdapter { contact -> openDialer(contact) }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )

        checkPermissions()
    }

    private fun checkPermissions() {
        val needed = arrayOf(Manifest.permission.READ_CONTACTS)
            .filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }

        if (needed.isEmpty()) loadContacts() else permissionLauncher.launch(needed.toTypedArray())
    }

    private fun loadContacts() {
        lifecycleScope.launch {
            val contacts = withContext(Dispatchers.IO) { fetchContacts() }
            adapter.submitContacts(contacts)
        }
    }

    private fun fetchContacts(): List<Contact> {
        val contacts = mutableMapOf<Long, Contact>()

        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.IS_PRIMARY
            ),
            null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )?.use { cursor ->
            val idIdx      = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIdx    = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIdx   = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val primaryIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY)

            while (cursor.moveToNext()) {
                val id        = cursor.getLong(idIdx)
                val name      = cursor.getString(nameIdx) ?: continue
                val phone     = cursor.getString(phoneIdx) ?: continue
                val isPrimary = cursor.getInt(primaryIdx) != 0

                if (!contacts.containsKey(id) || isPrimary) {
                    contacts[id] = Contact(id, name, phone)
                }
            }
        }

        return contacts.values.toList()
    }

    private fun openDialer(contact: Contact) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))
        startActivity(intent)
    }
}
