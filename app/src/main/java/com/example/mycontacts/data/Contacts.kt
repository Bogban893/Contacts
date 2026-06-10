package com.example.mycontacts.data

import android.content.ContentResolver
import android.provider.ContactsContract

data class Contact(
    val id: Int,
    val name: String,
    val number: String
)

class ContactsRepository(private val contentResolver: ContentResolver) {
    fun fetchContact(selection: String? = null): List<Contact> {
        val res: MutableList<Contact> = mutableListOf()
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor = contentResolver.query(
            uri,
            projection,
            selection, null, null
        )

        cursor?.use {
            val id = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val name = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val number = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                res.add(
                    Contact(
                        it.getInt(id), it.getString(name), it.getString(number)
                    )
                )
            }
        }


        return res.toList()
    }
}