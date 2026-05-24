package com.example.contacts.ui.contacts

import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.contacts.data.model.Contact
import com.example.contacts.R

class ContactsAdapter(
    private val onContactClick: (Contact) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    sealed class ListItem {
        data class Header(val letter: String) : ListItem()
        data class ContactItem(val contact: Contact) : ListItem()
    }

    private var allContacts: List<Contact> = emptyList()
    private var items: List<ListItem> = emptyList()

    fun submitContacts(contacts: List<Contact>) {
        allContacts = contacts
        applyFilter(null)
    }

    private fun applyFilter(query: String?) {
        val filtered = if (query.isNullOrBlank()) {
            allContacts
        } else {
            val q = query.trim().lowercase()
            allContacts.filter {
                it.name.lowercase().contains(q) || it.phone.contains(q)
            }
        }
        items = buildListWithHeaders(filtered)
        notifyDataSetChanged()
    }

    private fun buildListWithHeaders(contacts: List<Contact>): List<ListItem> {
        return contacts
            .groupBy { it.name.firstOrNull()?.uppercaseChar()?.toString() ?: "#" }
            .toSortedMap()
            .flatMap { (letter, group) ->
                listOf(ListItem.Header(letter)) + group.map { ListItem.ContactItem(it) }
            }
    }

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?) = FilterResults()
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            applyFilter(constraint?.toString())
        }
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is ListItem.Header -> VIEW_TYPE_HEADER
        is ListItem.ContactItem -> VIEW_TYPE_CONTACT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = inflater.inflate(R.layout.item_header, parent, false)
                HeaderViewHolder(view)
            }

            else -> {
                val view = inflater.inflate(R.layout.item_contact, parent, false)
                ContactViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListItem.Header -> (holder as HeaderViewHolder).bind(item.letter)
            is ListItem.ContactItem -> (holder as ContactViewHolder).bind(item.contact)
        }
    }

    override fun getItemCount() = items.size

    inner class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView = view.findViewById(R.id.tvName)
        private val tvPhone: TextView = view.findViewById(R.id.tvPhone)
        private val tvAvatar: TextView = view.findViewById(R.id.tvAvatar)

        fun bind(contact: Contact) {
            tvName.text = contact.name
            tvPhone.text = contact.phone

            val letter = contact.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            tvAvatar.text = letter
            tvAvatar.background = makeAvatarDrawable(letter)

            itemView.setOnClickListener { onContactClick(contact) }
        }

        // fix later
        private fun makeAvatarDrawable(letter: String): ShapeDrawable {
            val colors = listOf(
                Color.parseColor("#E57373"), Color.parseColor("#64B5F6"),
                Color.parseColor("#81C784"), Color.parseColor("#FFB74D"),
                Color.parseColor("#BA68C8"), Color.parseColor("#4DB6AC"),
                Color.parseColor("#F06292"), Color.parseColor("#4FC3F7")
            )
            val color = colors[(letter[0].code) % colors.size]
            return ShapeDrawable(OvalShape()).apply { paint.color = color }
        }
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvHeader: TextView = view.findViewById(R.id.tvSectionHeader)
        fun bind(letter: String) {
            tvHeader.text = letter
        }
    }

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_CONTACT = 1
    }
}
