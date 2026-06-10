package com.example.mycontacts

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.mycontacts.data.Contact
import com.example.mycontacts.data.ContactsRepository
import com.example.mycontacts.ContactsScreen
import com.example.mycontacts.ui.theme.MyContactsTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyContactsTheme {
                var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
                var permissionGranted by remember { mutableStateOf(false) }

                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    if (permissions[Manifest.permission.READ_CONTACTS] == true) {
                        permissionGranted = true
                        contacts = ContactsRepository(contentResolver).fetchContacts()
                    }
                }

                LaunchedEffect(Unit) {
                    launcher.launch(
                        arrayOf(
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.CALL_PHONE
                        )
                    )
                }

                ContactsScreen(
                    contacts = contacts,
                    permissionGranted = permissionGranted,
                    onCallContact = { contact ->
                        val intent = Intent(Intent.ACTION_CALL).apply {
                            data = Uri.parse("tel:${contact.number}")
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

