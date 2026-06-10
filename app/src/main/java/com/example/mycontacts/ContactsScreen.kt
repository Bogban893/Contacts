package com.example.mycontacts

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.example.mycontacts.data.Contact

@Composable
fun ContactsScreen(
    contacts: List<Contact>,
    permissionGranted: Boolean,
    onCallContact: (Contact) -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        if (!permissionGranted) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "No permission granted")
            }
        } else {
            val grouped = contacts.groupBy { it.name.first().uppercaseChar() }
            Column {
                Text(
                    text = "Contacts",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
                )
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                ) {
                    grouped.forEach { (letter, groupedContacts) ->
                        item {
                            Text(
                                text = letter.toString(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        items(groupedContacts) { contact ->
                            ContactItem(contact = contact, onClick = { onCallContact(contact) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactItem(contact: Contact, onClick: () -> Unit) {
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(50.dp)
            .padding(1.dp),
        shape = RectangleShape,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = contact.name, style = MaterialTheme.typography.bodyLarge)
            Text(text = contact.number, style = MaterialTheme.typography.bodySmall)
        }
    }
}