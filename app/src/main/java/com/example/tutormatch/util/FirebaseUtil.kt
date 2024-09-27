package com.example.tutormatch.util


import com.example.tutormatch.data.model.Annuncio
import com.example.tutormatch.data.model.Calendario
import com.example.tutormatch.data.model.Prenotazione
import com.example.tutormatch.data.model.Utente
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.Calendar
import java.util.TimeZone


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


        val calendar = Calendar.getInstance(TimeZone.getDefault()).apply {
            set(Calendar.SECOND, 0)      // Azzera i secondi
            set(Calendar.MILLISECOND, 0) // Azzera i millisecondi
        }
        val dataCompleta = calendar.time

        // Ottieni l'istanza di Calendar per la data corrente in UTC
        val dataCorrente = Calendar.getInstance().apply {
            // Resetta l'orario a mezzanotte in UTC
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
                        val fasciaData = Calendar.getInstance().apply {
                            time = calendario.data
                            add(Calendar.DAY_OF_MONTH, 1)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.time

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
                                add(Calendar.DAY_OF_MONTH, 1)
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

    // Funzione per gestire la prenotazione e l'aggiornamento atomico delle fasce orarie
    // Funzione per gestire la prenotazione e l'aggiornamento atomico delle fasce orarie
    fun creaPrenotazioniConBatch(
        listaFasceSelezionate: List<Calendario>, // Lista delle fasce orarie selezionate
        idStudente: String, // ID dello studente
        annuncioId: String, // ID dell'annuncio
        onSuccess: () -> Unit, // Callback per successo
        onFailure: (Exception) -> Unit // Callback per errore
    ) {
        // Ottieni il riferimento all'annuncio
        val annuncioRef = db.collection("annunci").document(annuncioId)

        annuncioRef.get().addOnSuccessListener { documentSnapshot ->
            val annuncio = documentSnapshot.toObject(Annuncio::class.java)

            if (annuncio != null) {
                val tutorRef = annuncio.tutor // Recupera il riferimento al tutor dall'annuncio

                // Inizializza il batch
                val batch = db.batch()

                // Contatore per tenere traccia delle operazioni completate
                var operazioniCompletate = 0
                val totaleOperazioni = listaFasceSelezionate.size

                // Itera sulle fasce orarie selezionate e cerca il documento in Firestore
                listaFasceSelezionate.forEach { fasciaOraria ->
                    db.collection("calendario")
                        .whereEqualTo("tutorRef", tutorRef) // Cerca in base al tutor
                        .whereEqualTo("data", fasciaOraria.data) // Cerca in base alla data
                        .whereEqualTo("oraInizio", fasciaOraria.oraInizio) // Cerca in base all'ora di inizio
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                val calendarioDocument = querySnapshot.documents.first()
                                val calendarioRef = calendarioDocument.reference

                                // Aggiungi l'aggiornamento dello stato al batch
                                batch.update(calendarioRef, "statoPren", true)

                                // Crea un nuovo documento per ogni prenotazione
                                val prenotazioneRef = db.collection("prenotazioni").document()
                                val prenotazione = Prenotazione(
                                    annuncioRef = annuncioRef,
                                    fasciaCalendarioRef = calendarioRef,
                                    studenteRef = idStudente
                                )

                                // Aggiungi la prenotazione al batch
                                batch.set(prenotazioneRef, prenotazione)
                            }

                            // Incrementa il contatore delle operazioni completate
                            operazioniCompletate++

                            // Quando tutte le operazioni sono completate, committa il batch
                            if (operazioniCompletate == totaleOperazioni) {
                                // Committa il batch
                                batch.commit().addOnSuccessListener {
                                    onSuccess() // Tutte le operazioni sono state eseguite con successo
                                }.addOnFailureListener { exception ->
                                    onFailure(exception) // Gestisci l'errore nel commit
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            onFailure(exception) // Gestisci l'errore nel recupero della fascia oraria
                        }
                }

            } else {
                onFailure(Exception("Annuncio non trovato"))
            }
        }.addOnFailureListener { exception ->
            onFailure(exception) // Gestisci l'errore nel recupero dell'annuncio
        }
    }
}