import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.tutormatch.auth.AuthViewModel
import com.example.tutormatch.data.model.Utente
import com.example.tutormatch.util.FirebaseUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.RuntimeEnvironment

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var authViewModel: AuthViewModel
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Step 1: Mock FirebaseApp initialization
        mockkStatic(FirebaseApp::class)
        val mockFirebaseApp = mockk<FirebaseApp>(relaxed = true)
        every { FirebaseApp.initializeApp(any()) } returns mockFirebaseApp
        every { FirebaseApp.getInstance() } returns mockFirebaseApp

        // Step 2: Mock FirebaseAuth
        mockkStatic(FirebaseAuth::class)
        firebaseAuth = mockk(relaxed = true)
        every { FirebaseAuth.getInstance() } returns firebaseAuth

        // Step 3: Mock FirebaseUser
        val mockFirebaseUser = mockk<FirebaseUser>(relaxed = true) {
            every { uid } returns "testUserId"
        }
        every { firebaseAuth.currentUser } returns mockFirebaseUser

        // Step 4: Mock FirebaseFirestore
        mockkStatic(FirebaseFirestore::class)
        firestore = mockk(relaxed = true)
        every { FirebaseFirestore.getInstance() } returns firestore

        // Step 5: Initialize the ViewModel
        authViewModel = AuthViewModel(mockk(relaxed = true))

        // Step 6: Mock FirebaseUtil methods
        mockkObject(FirebaseUtil)
        every { FirebaseUtil.getUserFromFirestore(any(), any()) } answers {
            val callback = secondArg<(Utente?) -> Unit>()
            callback(mockk(relaxed = true)) // Return a mocked user
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `onLoginClick - should show error when fields are empty`() {
        val observer: Observer<String?> = mockk(relaxed = true)
        authViewModel.showMessage.observeForever(observer)

        // Call onLoginClick without setting email and password
        authViewModel.onLoginClick()

        // Verify that an error message was shown
        verify { observer.onChanged("Tutti i campi devono essere compilati") }
    }

    @Test
    fun `onLoginClick - successful login`() {
        authViewModel.email.value = "test@example.com"
        authViewModel.password.value = "password123"

        val mockTask = mockk<Task<AuthResult>>(relaxed = true)
        every { mockTask.isSuccessful } returns true

        // Mock FirebaseAuth signInWithEmailAndPassword
        every { firebaseAuth.signInWithEmailAndPassword(any(), any()) } returns mockTask

        // Simulate task completion
        every { mockTask.addOnCompleteListener(any()) } answers {
            val listener = arg<OnCompleteListener<AuthResult>>(0)
            listener.onComplete(mockTask)
            mockTask
        }

        val observer: Observer<String?> = mockk(relaxed = true)
        authViewModel.showMessage.observeForever(observer)

        authViewModel.onLoginClick()

        // Verify that a success message was shown
        verify { observer.onChanged("Login riuscito!") }
    }

    @Test
    fun `onRegisterClick - should show error when fields are empty`() {
        val observer: Observer<String?> = mockk(relaxed = true)
        authViewModel.showMessage.observeForever(observer)

        authViewModel.onRegisterClick()

        // Verify that an error message was shown
        verify { observer.onChanged("Tutti i campi devono essere compilati") }
    }

    @Test
    fun `onRegisterClick - should show error when address is invalid`() = runTest {
        // Ottieni un'istanza di Application con Robolectric
        val application = RuntimeEnvironment.getApplication()

        // Crea uno spy di AuthViewModel usando l'istanza di Application
        val authViewModel = spyk(AuthViewModel(application)) {
            // Simula il comportamento della funzione verificaIndirizzo
            coEvery { verificaIndirizzo(any(), any()) } returns false
        }

        // Imposta i valori dei campi di input
        authViewModel.email.value = "test@example.com"
        authViewModel.password.value = "password123"
        authViewModel.nome.value = "TestNome"
        authViewModel.cognome.value = "TestCognome"
        authViewModel.residenza.value = "TestResidenza"
        authViewModel.via.value = "TestVia"
        authViewModel.cap.value = "12345"

        // Crea un observer simulato per showMessage
        val observer: Observer<String?> = mockk(relaxed = true)
        authViewModel.showMessage.observeForever(observer)

        // Chiama onRegisterClick
        authViewModel.onRegisterClick()

        // Assicura che tutte le coroutine siano state processate
        advanceUntilIdle()

        // Verifica che il messaggio di errore sia stato mostrato
        verify(exactly = 1) { observer.onChanged("Verifica che residenza e CAP siano corretti.") }

        // Pulisci l'observer per evitare memory leaks
        authViewModel.showMessage.removeObserver(observer)
    }


    @Test
    fun `onRegisterClick - registration forces success and shows success message`() = runTest {
        val application = mockk<Application>(relaxed = true)

        // Crea uno spy di AuthViewModel
        val authViewModel = spyk(AuthViewModel(application)) {
            // Mock della funzione verificaIndirizzo per forzare il valore true
            coEvery { verificaIndirizzo(any(), any()) } returns true

            // Mock della funzione registraUtente per simulare successo
            coEvery { registraUtente(any(), any()) } answers {
                // Forza l'impostazione del messaggio di successo
                _showMessage.value = "Registrazione riuscita!"
            }
        }

        // Imposta i valori dei campi di input per la registrazione
        authViewModel.email.value = "test@example.com"
        authViewModel.password.value = "password123"
        authViewModel.nome.value = "TestNome"
        authViewModel.cognome.value = "TestCognome"
        authViewModel.residenza.value = "TestResidenza"
        authViewModel.via.value = "TestVia"
        authViewModel.cap.value = "12345"

        // Crea un observer simulato per osservare i cambiamenti in _showMessage
        val observer: Observer<String?> = mockk(relaxed = true)
        authViewModel.showMessage.observeForever(observer)

        // Esegui la funzione onRegisterClick
        authViewModel.onRegisterClick()

        // Assicura che tutte le coroutine siano completate
        advanceUntilIdle()

        // Verifica che il messaggio di successo sia stato inviato
        verify(exactly = 1) { observer.onChanged("Registrazione riuscita!") }

        // Pulisci l'observer per evitare memory leaks
        authViewModel.showMessage.removeObserver(observer)
    }

    @Test
    fun `onForgotPasswordClick - should show error when email is empty`() {
        val observer: Observer<String?> = mockk(relaxed = true)
        authViewModel.showMessage.observeForever(observer)

        authViewModel.onForgotPasswordClick()

        verify { observer.onChanged("Per favore, inserisci un'email.") }
    }

    @Test
    fun `onForgotPasswordClick - successful reset email`() {
        authViewModel.email.value = "test@example.com"

        val mockTask = mockk<Task<Void>>(relaxed = true)
        every { mockTask.isSuccessful } returns true

        // Mock FirebaseAuth password reset
        every { firebaseAuth.sendPasswordResetEmail(any()) } returns mockTask

        // Simulate task completion
        every { mockTask.addOnCompleteListener(any()) } answers {
            val listener = arg<OnCompleteListener<Void>>(0)
            listener.onComplete(mockTask)
            mockTask
        }

        val observer: Observer<String?> = mockk(relaxed = true)
        authViewModel.showMessage.observeForever(observer)

        authViewModel.onForgotPasswordClick()

        // Verify the success message
        verify { observer.onChanged("Email di recupero password inviata!") }
    }
}
