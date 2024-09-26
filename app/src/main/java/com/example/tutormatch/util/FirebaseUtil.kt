package com.example.tutormatch.util


import com.example.tutormatch.data.model.Annuncio
import com.example.tutormatch.data.model.Calendario
import com.example.tutormatch.data.model.Utente
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Oggetto per gestire le operazioni Firestore
object FirebaseUtil {
    private val db = FirebaseFirestore.getInstance() // Istanza di Firestore

    // Aggiunge un nuovo utente a Firestore
    fun addUserToFirestore(utente: Utente) {
        db.collection("utenti").document(utente.userId).set(utente)
            .addOnSuccessListener {
                // Utente aggiunto con successo
            }
            .addOnFailureListener { e ->
                // Errore nell'aggiunta dell'utente
            }
    }

    // Recupera i dati di un utente da Firestore
    fun getUserFromFirestore(userId: String, callback: (Utente?) -> Unit) {
        db.collection("utenti").document(userId).get()
            .addOnSuccessListener { document ->
                val utente = document.toObject(Utente::class.java)
                callback(utente)
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun osservaModificheAnnunciSuFirestore(
        onAnnunciUpdated: () -> Unit,  // Callback da eseguire quando ci sono modifiche
        onError: (Exception) -> Unit   // Callback per gestire gli errori
    ): ListenerRegistration {
        return db.collection("annunci").addSnapshotListener { querySnapshot, e ->
            if (e != null) {
                onError(e)  // Chiama onError in caso di errore
                return@addSnapshotListener
            }

            // Se ci sono cambiamenti nei documenti (aggiunte o eliminazioni)
            if (querySnapshot != null && querySnapshot.documentChanges.isNotEmpty()) {
                onAnnunciUpdated()  // Chiama la callback per aggiornare gli annunci
            }
        }
    }

    fun eliminaFasceOrarieScadutePerTutor(tutorRef: DocumentReference, callback: (Boolean, String?) -> Unit) {
        val calendarioCollection = db.collection("calendario")

        val dataCompleta = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        // Ottieni l'istanza di Calendar per la data corrente
        val dataCorrente = Calendar.getInstance().apply {
            // Resetta l'orario a mezzanotte
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time


        // Query per ottenere le fasce del tutor specifico
        calendarioCollection.whereEqualTo("tutorRef", tutorRef).get().addOnSuccessListener { querySnapshot ->
            querySnapshot?.let { documents ->
                for (document in documents) {
                    val calendario = document.toObject(Calendario::class.java)
                    calendario.let {
                        // 1. Primo controllo: confronto solo le date
                        val fasciaData = calendario.data
                        if (fasciaData.before(dataCorrente)) {
                            // Se la data della fascia è passata, elimina il documento
                            calendarioCollection.document(document.id).delete()
                                .addOnFailureListener { e ->
                                    callback(false, e.message)
                                }
                        }
                        if (fasciaData.equals(dataCorrente)) {
                            // 2. Se la data è quella corrente, confronta gli orari
                            val dataFasciaInizio = Calendar.getInstance().apply {
                                time = calendario.data // Usa la data dal database
                                set(Calendar.HOUR_OF_DAY, calendario.oraInizio.split(":")[0].toInt())
                                set(Calendar.MINUTE, calendario.oraInizio.split(":")[1].toInt())
                            }.time


                            // Se l'ora di inizio è già passata, elimina la fascia
                            if (dataFasciaInizio.before(dataCompleta)) {
                                calendarioCollection.document(document.id).delete()
                                    .addOnFailureListener { e ->
                                        callback(false, e.message)
                                    }
                            }
                        }
                    }
                }
                callback(true, null) // Eliminazione completata con successo
            }
        }.addOnFailureListener { exception ->
            callback(false, exception.message) // Errore nel recupero delle fasce
        }
    }



    // Funzione per ottenere il tutorRef dall'annuncio
    fun getTutorDaAnnuncioF(
        annuncioId: String,
        onSuccess: (DocumentReference) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val annuncioDocumentReference = db.collection("annunci").document(annuncioId)

        annuncioDocumentReference.get().addOnSuccessListener { documentSnapshot ->
            val annuncio = documentSnapshot.toObject(Annuncio::class.java)
            if (annuncio != null) {
                // Se l'annuncio esiste, esegui il callback di successo
                onSuccess(annuncio.tutor!!)
            } else {
                // Se l'annuncio non è valido, segnala l'errore
                onFailure(Exception("Annuncio non valido o tutor non trovato"))
            }
        }.addOnFailureListener { exception ->
            // Gestisci l'errore e esegui il callback di fallimento
            onFailure(exception)
        }
    }
}