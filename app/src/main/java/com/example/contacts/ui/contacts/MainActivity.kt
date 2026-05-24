package com.example.contacts.ui.contacts

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.contacts.data.model.Contact
import com.example.contacts.R
import com.example.contacts.data.repository.ContactsRepository

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ContactsAdapter
    private lateinit var viewModel: ContactsViewModel

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.CALL_PHONE] != true) {
            Toast.makeText(this, getString(R.string.no_contacts_permission), Toast.LENGTH_SHORT)
                .show()
        } else {
            viewModel.loadContacts()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViewModel()
        setupSearch()
        setupRecyclerView()
        checkPermissions()
    }

    private fun setupViewModel() {
        val repository = ContactsRepository(contentResolver)
        val factory = ContactsViewModel.Factory(repository)
        viewModel = ViewModelProvider(this, factory)[ContactsViewModel::class.java]

        viewModel.contacts.observe(this) { contacts ->
            adapter.submitContacts(contacts)
        }
    }

    private fun setupSearch() {
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
    }

    private fun setupRecyclerView() {
        adapter = ContactsAdapter { contact -> openDialer(contact) }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }

    private fun checkPermissions() {
        val needed = arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE)
            .filter {
                ContextCompat.checkSelfPermission(
                    this,
                    it
                ) != PackageManager.PERMISSION_GRANTED
            }

        if (needed.isEmpty()) viewModel.loadContacts() else permissionLauncher.launch(needed.toTypedArray())
    }

    private fun openDialer(contact: Contact) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${contact.phone}"))
            startActivity(intent)
        } else {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))
            startActivity(intent)
        }
    }
}

