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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.example.mycontacts.data.Contact
import com.example.mycontacts.data.ContactsRepository
import com.example.mycontacts.ui.theme.MyContactsTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyContactsTheme {
                var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
                var permissionGranted by remember { mutableStateOf(false) }
                var grouped by remember { mutableStateOf(mapOf<Char, List<Contact>>()) }

                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val contactGranted = permissions[Manifest.permission.READ_CONTACTS]
                    val callGranted = permissions[Manifest.permission.CALL_PHONE]
                    if (contactGranted ?: false) {
                        permissionGranted = true
                        contacts =
                            ContactsRepository(contentResolver).fetchContact().sortedBy { it.name }
                        grouped = contacts.groupBy { it.name.first().uppercaseChar() }
                    }

                    if (callGranted ?: false) {
//                        придумать что-то
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

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (!permissionGranted) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(text = "Нет разрешений")
                        }
                    } else {
                        Column {
//                            Row(modifier = Modifier.padding(innerPadding.calculateTopPadding()/2)) { }
                            Text(
                                text = "Contacts",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(
                                    top = innerPadding.calculateTopPadding(),
//                                    bottom = innerPadding.calculateBottomPadding(),
                                )
                            )
                            LazyColumn(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                grouped.forEach { (letter, contacts) ->
                                    item {
                                        Text(
                                            text = letter.toString(),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }

                                    items(contacts) { label ->

                                        Button(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(50.dp)
                                                .padding(1.dp),
                                            shape = RectangleShape,
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_CALL).apply {
                                                    data = Uri.parse("tel:${label.number}")
                                                }
                                                startActivity(intent)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.surface,
                                                contentColor = MaterialTheme.colorScheme.onSurface
                                            )
                                        ) {
                                            Text(label.name)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

