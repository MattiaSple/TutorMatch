package com.example.tutormatch.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ProfiloViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var firestore: FirebaseFirestore
    private lateinit var profiloViewModel: ProfiloViewModel

    @Before
    fun setUp() {
        // Usa UnconfinedTestDispatcher per eseguire le coroutine immediatamente
        val testDispatcher = UnconfinedTestDispatcher()

        // Imposta il dispatcher di test come dispatcher principale
        Dispatchers.setMain(testDispatcher)

        // Step 1: Mock FirebaseApp initialization
        mockkStatic(FirebaseApp::class)
        val mockFirebaseApp = mockk<FirebaseApp>(relaxed = true)
        every { FirebaseApp.initializeApp(any()) } returns mockFirebaseApp
        every { FirebaseApp.getInstance() } returns mockFirebaseApp

        // Mock FirebaseFirestore e altre dipendenze
        mockkStatic(FirebaseFirestore::class)
        firestore = mockk(relaxed = true)
        every { FirebaseFirestore.getInstance() } returns firestore

        // Istanzia il ViewModel
        profiloViewModel = ProfiloViewModel(mockk(relaxed = true))
    }

    @After
    fun tearDown() {
        // Reset del dispatcher principale dopo il test
        Dispatchers.resetMain()
        unmockkAll()
    }
    @Test
    fun `saveUserProfile should post error message when fields are empty`() = runTest {
        // Simula l'aggiornamento del profilo senza completare i campi (tutti i campi sono vuoti)

        // Chiama la funzione per salvare il profilo
        profiloViewModel.saveUserProfile("testUserId")

        // Esegui tutte le coroutine sospese
        advanceUntilIdle()

        // Verifica che sia stato mostrato un messaggio di errore
        Assert.assertEquals("Tutti i campi devono essere compilati.", profiloViewModel.message.value)
    }
}
