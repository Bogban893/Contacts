package com.example.mycontacts.data

import android.content.ContentResolver
import android.provider.ContactsContract

data class Contact(
    val id: Int,
    val name: String,
    val number: String
)

class ContactsRepository(private val contentResolver: ContentResolver) {
    fun fetchContacts(): List<Contact> {
        val res: MutableList<Contact> = mutableListOf()
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.IS_PRIMARY
        )
        val selection =
            "${ContactsContract.CommonDataKinds.Phone.TYPE} = ${ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE}"

        val sortOrder = "${ContactsContract.CommonDataKinds.Phone.IS_PRIMARY} DESC"
        val cursor = contentResolver.query(uri, projection, selection, null, sortOrder)
        val seenIds = mutableSetOf<Int>()

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameCol = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberCol = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val contactId = it.getInt(idCol)
                if (contactId in seenIds) continue

                seenIds.add(contactId)
                res.add(
                    Contact(
                        id = contactId,
                        name = it.getString(nameCol),
                        number = it.getString(numberCol)
                    )
                )
            }
        }

        return res.sortedBy { it.name }
    }
}
