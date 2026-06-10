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
            ContactsContract.CommonDataKinds.Phone.TYPE
        )
        val selection =
            "${ContactsContract.CommonDataKinds.Phone.TYPE} = ${ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE}"

        val cursor = contentResolver.query(uri, projection, selection, null, null)
        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameCol =
                it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberCol = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                res.add(
                    Contact(
                        id = it.getInt(idCol),
                        name = it.getString(nameCol),
                        number = it.getString(numberCol)
                    )
                )
            }
        }


        val mobileIds = res.map { it.id }.toSet()
        val fallbackCursor = contentResolver.query(uri, projection, null, null, null)
        fallbackCursor?.use {
            val idCol = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameCol =
                it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberCol = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val seen = mutableSetOf<Int>()
            while (it.moveToNext()) {
                val id = it.getInt(idCol)
                if (id !in mobileIds && id !in seen) {
                    seen.add(id)
                    res.add(
                        Contact(
                            id = id,
                            name = it.getString(nameCol),
                            number = it.getString(numberCol)
                        )
                    )
                }
            }
        }

        return res.sortedBy { it.name }
    }
}
