package com.example.contacts

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.contacts.data.model.Contact
import com.example.contacts.data.repository.ContactsRepository
import com.example.contacts.ui.contacts.ContactsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class ContactsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: ContactsRepository
    private lateinit var viewModel: ContactsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        viewModel = ContactsViewModel(repository, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadContacts posts fetched list to contacts LiveData`() = runTest {
        val fakeContacts = listOf(
            Contact(1, "House money", "8 800 555 35 35"),
            Contact(2, "Bob", "911")
        )
        whenever(repository.fetchContacts()).thenReturn(fakeContacts)

        viewModel.loadContacts()
        advanceUntilIdle()

        assertEquals(fakeContacts, viewModel.contacts.value)
    }

    @Test
    fun `loadContacts with empty result posts empty list`() = runTest {
        whenever(repository.fetchContacts()).thenReturn(emptyList())

        viewModel.loadContacts()
        advanceUntilIdle()

        assertEquals(emptyList<Contact>(), viewModel.contacts.value)
    }

    @Test
    fun `loadContacts calls repository exactly once`() = runTest {
        whenever(repository.fetchContacts()).thenReturn(emptyList())

        viewModel.loadContacts()
        advanceUntilIdle()

        verify(repository, times(1)).fetchContacts()
    }

    @Test
    fun `contacts LiveData is null before loadContacts is called`() {
        assertNull(viewModel.contacts.value)
    }

    @Test
    fun `calling loadContacts twice updates LiveData with latest result`() = runTest {
        val first = listOf(Contact(1, "Alice", "+1"))
        val second = listOf(Contact(1, "Alice", "+1"), Contact(2, "Bob", "+2"))

        whenever(repository.fetchContacts())
            .thenReturn(first)
            .thenReturn(second)

        viewModel.loadContacts()
        advanceUntilIdle()
        assertEquals(first, viewModel.contacts.value)

        viewModel.loadContacts()
        advanceUntilIdle()
        assertEquals(second, viewModel.contacts.value)
    }

    @Test
    fun `Factory creates a ContactsViewModel instance`() {
        val factory = ContactsViewModel.Factory(repository)
        val vm = factory.create(ContactsViewModel::class.java)

        assertNotNull(vm)
    }
}