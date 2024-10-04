package com.example.tutormatch.util


import com.example.tutormatch.data.model.Annuncio
import com.example.tutormatch.data.model.Calendario
import com.example.tutormatch.data.model.Prenotazione
import com.example.tutormatch.data.model.Utente
import com.google.android.gms.tasks.Tasks
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
                            //add(Calendar.DAY_OF_MONTH, 1)
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
                                //add(Calendar.DAY_OF_MONTH, 1)
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
    fun creaPrenotazioniConBatch(
        listaFasceSelezionate: List<Calendario>,
        idStudente: String,
        annuncioId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val annuncioRef = db.collection("annunci").document(annuncioId)

        annuncioRef.get().addOnSuccessListener { documentSnapshot ->
            val annuncio = documentSnapshot.toObject(Annuncio::class.java)

            if (annuncio != null) {
                val tutorRef = annuncio.tutor // Recupera il riferimento al tutor dall'annuncio

                // Inizializza il batch
                val batch = db.batch()
                var completate = 0 // Contatore delle operazioni completate
                var operazioneFallita = false

                // Itera sulle fasce orarie selezionate
                listaFasceSelezionate.forEach { fasciaOraria ->
                    db.collection("calendario")
                        .whereEqualTo("tutorRef", tutorRef)
                        .whereEqualTo("data", fasciaOraria.data)
                        .whereEqualTo("oraInizio", fasciaOraria.oraInizio)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                val calendarioDocument = querySnapshot.documents.first()
                                val calendarioRef = calendarioDocument.reference

                                // Aggiungi l'aggiornamento dello stato al batch
                                batch.update(calendarioRef, "statoPren", true)

                                // Crea un nuovo documento per ogni prenotazione
                                val prenotazioneRef = db.collection("prenotazioni").document()
                                val prenotazione = mapOf(
                                    "annuncioRef" to annuncioRef,
                                    "fasciaCalendarioRef" to calendarioRef,
                                    "studenteRef" to idStudente,
                                    "tutorRef" to tutorRef!!.id
                                )

                                // Aggiungi la prenotazione al batch
                                batch.set(prenotazioneRef, prenotazione)
                            } else {
                                operazioneFallita = true
                                onFailure(Exception("Fascia oraria non trovata"))
                            }

                            completate++

                            // Se tutte le operazioni sono completate, committa il batch
                            if (completate == listaFasceSelezionate.size && !operazioneFallita) {
                                batch.commit().addOnSuccessListener {
                                    onSuccess()
                                }.addOnFailureListener { exception ->
                                    onFailure(exception)
                                }
                            }
                        }.addOnFailureListener { exception ->
                            operazioneFallita = true
                            onFailure(exception)
                        }
                }
            } else {
                onFailure(Exception("Annuncio non trovato"))
            }
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }




    fun getNomeCognomeUtenteAtomico(
        annuncioRef: DocumentReference,
        calendarioRef: DocumentReference,
        studenteId: String,
        isTutor: Boolean,  // Indica se chi sta accedendo è il tutor
        onSuccess: (Annuncio, Calendario, String, String) -> Unit,  // Restituisce nome e cognome
        onFailure: (Exception) -> Unit
    ) {
        // Ottieni entrambe le chiamate Firestore
        val annuncioTask = annuncioRef.get()
        val calendarioTask = calendarioRef.get()

        annuncioTask.addOnSuccessListener { annuncioSnapshot ->
            val annuncio = annuncioSnapshot.toObject(Annuncio::class.java)
            if (annuncio != null) {
                // Se è il tutor che visualizza, recupera lo studente, altrimenti recupera il tutor
                val utenteTask = if (isTutor) {
                    // Recupera lo studente dalla collezione utenti usando l'userId
                    FirebaseFirestore.getInstance().collection("utenti").document(studenteId).get()
                } else {
                    // Recupera il tutor tramite DocumentReference
                    annuncio.tutor!!.get()
                }

                // Usa Tasks.whenAll per eseguire tutto in modo atomico
                Tasks.whenAllSuccess<Any>(annuncioTask, calendarioTask, utenteTask)
                    .addOnSuccessListener { results ->
                        val annuncioSnapshot = results[0] as com.google.firebase.firestore.DocumentSnapshot
                        val calendarioSnapshot = results[1] as? com.google.firebase.firestore.DocumentSnapshot
                        val utenteSnapshot = results[2] as? com.google.firebase.firestore.DocumentSnapshot

                        if (calendarioSnapshot != null && utenteSnapshot != null) {
                            val annuncio = annuncioSnapshot.toObject(Annuncio::class.java)
                            val calendario = calendarioSnapshot.toObject(Calendario::class.java)
                            val utente = utenteSnapshot.toObject(Utente::class.java)

                            if (annuncio != null && calendario != null && utente != null) {
                                // Restituisci i dati in base al ruolo
                                onSuccess(annuncio, calendario, utente.nome, utente.cognome)
                            } else {
                                onFailure(Exception("Dati non trovati"))
                            }
                        } else {
                            onFailure(Exception("Snapshot non valido"))
                        }
                    }
                    .addOnFailureListener { exception ->
                        onFailure(exception)
                    }
            } else {
                onFailure(Exception("Annuncio non trovato"))
            }
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }


    fun getPrenotazioniPerRuolo(
        userId: String,
        isTutor: Boolean,
        onSuccess: (List<Prenotazione>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val query = if (isTutor) {
            db.collection("prenotazioni").whereEqualTo("tutorRef", userId)
        } else {
            db.collection("prenotazioni").whereEqualTo("studenteRef", userId)
        }

        query.get()
            .addOnSuccessListener { querySnapshot ->
                val prenotazioni = querySnapshot.toObjects(Prenotazione::class.java)

                // Lista per conservare le prenotazioni con informazioni sul calendario
                val prenotazioniConCalendario = mutableListOf<Pair<Prenotazione, Calendario>>()

                // Recupera i dettagli del Calendario per ciascuna prenotazione
                val tasks = prenotazioni.map { prenotazione ->
                    prenotazione.fasciaCalendarioRef?.get()?.addOnSuccessListener { calendarioSnapshot ->
                        val calendario = calendarioSnapshot.toObject(Calendario::class.java)
                        if (calendario != null) {
                            prenotazioniConCalendario.add(Pair(prenotazione, calendario))
                        }
                    }
                }

                // Esegui tutto in modo atomico e ordina dopo aver recuperato tutte le informazioni
                Tasks.whenAllSuccess<Any>(tasks).addOnSuccessListener {
                    // Ordina per data e oraInizio
                    val prenotazioniOrdinate = prenotazioniConCalendario.sortedWith(compareBy(
                        { it.second.data },  // Ordina per data
                        { it.second.oraInizio }  // Ordina per ora di inizio
                    ))

                    // Restituisci la lista delle prenotazioni ordinate (senza il calendario)
                    onSuccess(prenotazioniOrdinate.map { it.first })
                }.addOnFailureListener { exception ->
                    onFailure(exception)
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }



    fun eliminaPrenotazioneF(prenotazione: Prenotazione, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        // Inizializza un batch
        val batch = db.batch()

        // 1. Recupera il riferimento alla fascia oraria associata alla prenotazione
        val fasciaCalendarioRef = prenotazione.fasciaCalendarioRef

        // 2. Aggiorna lo stato della fascia oraria a 'false'
        batch.update(fasciaCalendarioRef!!, "statoPren", false)

        // 3. Trova il documento della prenotazione
        db.collection("prenotazioni")
            .whereEqualTo("tutorRef", prenotazione.tutorRef)
            .whereEqualTo("fasciaCalendarioRef", prenotazione.fasciaCalendarioRef)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // 4. Ottieni il riferimento del documento della prenotazione
                    val prenotazioneDoc = querySnapshot.documents.first()
                    val prenotazioneRef = prenotazioneDoc.reference

                    // 5. Aggiungi l'operazione di eliminazione al batch
                    batch.delete(prenotazioneRef)

                    // 6. Commetti il batch solo se entrambe le operazioni sono pronte
                    batch.commit()
                        .addOnSuccessListener {
                            // Se entrambe le operazioni sono eseguite con successo
                            onSuccess()
                        }
                        .addOnFailureListener { exception ->
                            // Se il batch fallisce
                            onFailure(exception)
                        }
                } else {
                    // Se la prenotazione non è stata trovata
                    onFailure(Exception("Prenotazione non trovata"))
                }
            }
            .addOnFailureListener { exception ->
                // Fallimento nel trovare la prenotazione
                onFailure(exception)
            }
    }


}