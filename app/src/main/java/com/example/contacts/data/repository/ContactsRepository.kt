package com.example.contacts.data.repository

import android.content.ContentResolver
import android.provider.ContactsContract
import com.example.contacts.data.model.Contact

class ContactsRepository(private val contentResolver: ContentResolver) {

    fun fetchContacts(): List<Contact> {
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
            val idIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val primaryIdx =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIdx)
                val name = cursor.getString(nameIdx) ?: continue
                val phone = cursor.getString(phoneIdx) ?: continue
                val isPrimary = cursor.getInt(primaryIdx) != 0

                if (!contacts.containsKey(id) || isPrimary) {
                    contacts[id] = Contact(id, name, phone)
                }
            }
        }

        return contacts.values.toList()
    }
}