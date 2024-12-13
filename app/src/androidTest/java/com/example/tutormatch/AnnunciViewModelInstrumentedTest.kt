package com.example.tutormatch

import android.app.Application
import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.tutormatch.data.model.Annuncio
import com.example.tutormatch.ui.viewmodel.AnnunciViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AnnunciViewModelInstrumentedTest {

    private lateinit var annunciViewModel: AnnunciViewModel
    private val firestore = FirebaseFirestore.getInstance()
    private val messageObserver = TestObserver()

    @Before
    fun setUp() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application

        // Inizializza il ViewModel
        annunciViewModel = AnnunciViewModel(appContext)

        // Inizializza il riferimento del tutor
        annunciViewModel._tutorRef = firestore.collection("utenti").document("user123")

        // Osserva i messaggi LiveData
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            annunciViewModel.message.observeForever(messageObserver)
        }
    }

    @After
    fun tearDown() {
        runBlocking {
            // Rimuove l'Observer
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                annunciViewModel.message.removeObserver(messageObserver)
            }

            // Pulisce la collezione di test
            val testCollection = firestore.collection("test_annunci")
            val docs = testCollection.get().await()
            docs.documents.forEach { doc ->
                testCollection.document(doc.id).delete().await()
            }
        }
    }

    @Test
    fun salvaAnnuncioSalvaCorrettiDatiInFirebase() {
        runBlocking {
            val userId = "user123"
            val materia = "Matematica"
            val prezzo = "20"
            val descrizione = "Lezioni di algebra"
            val online = true
            val presenza = false
            val modalita = "Modalità: Online"
            val tutorReference = firestore.collection("utenti").document(userId)

            // Crea un utente nel database
            firestore.collection("utenti").document(userId).set(
                mapOf(
                    "userId" to userId,
                    "nome" to "Mario",
                    "cognome" to "Rossi",
                    "residenza" to "Civitanova Marche",
                    "via" to "Strada statale maceratese",
                    "cap" to "62012",
                    "email" to "test@example.com",
                    "ruolo" to true // Indica che è un tutor
                )
            ).await()

            // Salva l'annuncio tramite il ViewModel
            annunciViewModel.salvaAnnuncio(materia, prezzo, descrizione, online, presenza)

            // Attesa per la sincronizzazione di Firebase
            kotlinx.coroutines.delay(3000)

            // Verifica se l'annuncio è stato salvato
            val docs = firestore.collection("annunci").get().await()
            val trovato = docs.documents.find { doc ->
                doc.getString("materia") == materia &&
                        doc.getString("prezzo") == prezzo &&
                        doc.getString("descrizione") == descrizione &&
                        doc.getBoolean("mod_on") == online &&
                        doc.getBoolean("mod_pres") == presenza &&
                        doc.getString("modalita") == modalita &&
                        doc.getDocumentReference("tutor") == tutorReference
            }

            Assert.assertNotNull("Annuncio specifico non trovato nella collezione", trovato)

            // Elimina l'annuncio trovato
            trovato?.let { doc ->
                val idAnnuncio = doc.id
                val annuncio = Annuncio(
                    id = idAnnuncio,
                    tutor = tutorReference,
                    materia = materia,
                    prezzo = prezzo,
                    descrizione = descrizione,
                    mod_on = online,
                    mod_pres = presenza
                )
                annunciViewModel.eliminaAnnuncio(annuncio)
            }

            // Attesa per la cancellazione
            kotlinx.coroutines.delay(2000)

            // Elimina l'utente
            firestore.collection("utenti").document(userId).delete().await()
        }
    }

    // Implementazione Observer di test
    private class TestObserver : Observer<String> {
        val observedMessages = mutableListOf<String>()

        override fun onChanged(value: String) {
            println("TestObserver ha ricevuto: $value")
            observedMessages.add(value)
        }
    }
}
