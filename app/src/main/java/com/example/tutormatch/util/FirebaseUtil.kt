package com.example.tutormatch.util


import com.example.tutormatch.data.model.Annuncio
import com.example.tutormatch.data.model.Calendario
import com.example.tutormatch.data.model.Prenotazione
import com.example.tutormatch.data.model.Utente
import com.example.tutormatch.network.RetrofitInstance
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.GeoPoint
import java.text.SimpleDateFormat
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
    suspend fun getUserFromFirestore(userId: String): Utente? {
        return try {
            val documentSnapshot = db.collection("utenti")
                .document(userId)
                .get()
                .await()  // Sospende finché Firebase non restituisce il risultato
            documentSnapshot.toObject(Utente::class.java)
        } catch (e: Exception) {
            null // Gestione dell'errore
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

    suspend fun getAnnuncio(annuncioRef: DocumentReference): Annuncio? {
        return try {
            val documentSnapshot = annuncioRef.get().await()  // Usa await per sospendere fino al completamento della chiamata
            documentSnapshot.toObject(Annuncio::class.java)   // Restituisce l'oggetto Annuncio o null
        } catch (e: Exception) {
            null  // Gestisci il fallimento restituendo null o gestisci l'eccezione come preferisci
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
                    db.collection("utenti").document(studenteId).get()
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



    suspend fun eliminaPrenotazioneF(prenotazione: Prenotazione): Boolean {
        return try {

            // Inizializza un batch
            val batch = db.batch()

            // 1. Recupera il riferimento alla fascia oraria associata alla prenotazione
            val fasciaCalendarioRef = prenotazione.fasciaCalendarioRef

            // 2. Aggiorna lo stato della fascia oraria a 'false'
            batch.update(fasciaCalendarioRef!!, "statoPren", false)

            // 3. Trova il documento della prenotazione
            val querySnapshot = db.collection("prenotazioni")
                .whereEqualTo("tutorRef", prenotazione.tutorRef)
                .whereEqualTo("fasciaCalendarioRef", prenotazione.fasciaCalendarioRef)
                .get()
                .await()  // Usa await per sospendere fino al completamento dell'operazione

            if (!querySnapshot.isEmpty) {
                // 4. Ottieni il riferimento del documento della prenotazione
                val prenotazioneDoc = querySnapshot.documents.first()
                val prenotazioneRef = prenotazioneDoc.reference

                // 5. Aggiungi l'operazione di eliminazione al batch
                batch.delete(prenotazioneRef)

                // 6. Commetti il batch solo se entrambe le operazioni sono pronte
                batch.commit().await()  // Usa await per attendere il completamento del batch

                // Se tutto ha avuto successo, restituisci true
                return true
            } else {
                // Se la prenotazione non è stata trovata
                throw Exception("Prenotazione non trovata")
            }
        } catch (e: Exception) {
            // Se c'è stato un errore, gestiscilo e restituisci false
            e.printStackTrace()
            false
        }
    }


    suspend fun eliminaFasceOrarieScadute(dataCorrente: String, oraCorrente: String) {

        val batch = db.batch()  // Batch per operazioni atomiche

        val documentiFas = db.collection("calendario").get().await()  // Aspetta il risultato della query
        documentiFas.forEach { documentoFascia ->
            val fascia = documentoFascia.toObject(Calendario::class.java)
            val dataFascia = SimpleDateFormat("yyyy-MM-dd", Locale.ITALY).format(fascia.data)

            // Controlla se la fascia è scaduta
            if (dataFascia < dataCorrente || (dataFascia == dataCorrente && fascia.oraInizio < oraCorrente)) {
                batch.delete(documentoFascia.reference)  // Aggiungi al batch
            }
        }

        // Commetti il batch
        batch.commit().await()  // Attendi il commit del batch
    }

    suspend fun eliminaPrenotazioniScadute() {

        val batch = db.batch()  // Batch per operazioni atomiche

        val documentiPren = db.collection("prenotazioni").get().await()  // Aspetta il risultato della query
        documentiPren.forEach { documentoPrenotazione ->
            val prenotazione = documentoPrenotazione.toObject(Prenotazione::class.java)

            // Controlla se la fascia oraria associata alla prenotazione esiste ancora
            val fasciaCalendarioRef = prenotazione.fasciaCalendarioRef
            if (fasciaCalendarioRef != null) {
                val fasciaDoc = fasciaCalendarioRef.get().await()  // Attendi il recupero della fascia
                if (!fasciaDoc.exists()) {
                    // Se la fascia non esiste più, procedi con:

                    // 1. Elimina la prenotazione
                    batch.delete(documentoPrenotazione.reference)

                    // 2. Aggiorna il feedback dello studente
                    val studenteRef = db.collection("utenti").document(prenotazione.studenteRef)  // Riferimento allo studente
                    val studenteDoc = studenteRef.get().await()  // Recupera il documento dello studente


                    val studente = studenteDoc.toObject(Utente::class.java)
                    if (studente != null && !studente.tutorDaValutare.contains(prenotazione.tutorRef)) {
                        // Aggiungi l'ID del tutor al feedback se non è già presente
                        studente.tutorDaValutare.add(prenotazione.tutorRef)

                        // Aggiungi l'aggiornamento dello studente al batch
                        batch.set(studenteRef, studente)

                    }
                }
            }
        }
        // Commetti il batch per applicare sia le eliminazioni che gli aggiornamenti
        batch.commit().await()  // Attendi il commit del batch
    }


    fun getDocumentRefById(userId: String): DocumentReference {
        return db.collection("utenti").document(userId)
    }


    // Funzione per caricare i tutor associati da una lista di tutorRef
    suspend fun loadTutors(tutorRefs: List<String>): List<Utente> {
        val tutorList = mutableListOf<Utente>()

        try {
            for (tutorRef in tutorRefs) {
                val documentSnapshot = db.collection("utenti")
                    .document(tutorRef)
                    .get()
                    .await()  // Sospende finché non ottiene il risultato

                val tutor = documentSnapshot.toObject(Utente::class.java)
                tutor?.let { tutorList.add(it) }
            }
        } catch (e: Exception) {
            // Puoi gestire l'errore qui (ad esempio, ritornando una lista vuota)
        }

        return tutorList
    }


    // Funzione atomica per valutare il tutor e rimuoverlo dalla lista tutorDaValutare dello studente
    suspend fun rateTutorAndRemoveFromListBatch(studentRef: String, tutorRef: String, rating: Int): Boolean {
        return try {

            val batch = db.batch()

            val tutorDocRef = db.collection("utenti").document(tutorRef)
            val studentDocRef = db.collection("utenti").document(studentRef)

            // Recupera i documenti del tutor e dello studente
            val tutorSnapshot = tutorDocRef.get()
                .await() // Operazione asincrona per ottenere il documento del tutor
            val studentSnapshot = studentDocRef.get()
                .await() // Operazione asincrona per ottenere il documento dello studente

            // Aggiorna il feedback del tutor se esiste
            val tutor = tutorSnapshot.toObject(Utente::class.java)
            tutor?.let {
                val updatedRatings = it.feedback.toMutableList().apply {
                    add(rating)
                }
                // Prepara l'aggiornamento del feedback nel batch
                batch.update(tutorDocRef, "feedback", updatedRatings)
            }

            // Aggiorna la lista tutorDaValutare dello studente se esiste
            val student = studentSnapshot.toObject(Utente::class.java)
            student?.let {
                val updatedTutorRefs = it.tutorDaValutare.toMutableList()
                if (updatedTutorRefs.contains(tutorRef)) {
                    updatedTutorRefs.remove(tutorRef)
                    // Prepara l'aggiornamento nel batch
                    batch.update(studentDocRef, "tutorDaValutare", updatedTutorRefs)
                }
            }
            // Esegui il batch
            batch.commit().await() // Operazione atomica e asincrona
            true
        } catch (e: Exception) {
            false
        }
    }

    // Funzione per verificare se esiste un annuncio con i criteri specificati
    suspend fun verificaAnnuncioEsistente(
        materia: String,
        prezzo: String,
        descrizione: String,
        online: Boolean,
        presenza: Boolean,
        tutorRef: DocumentReference
    ): Boolean {
        return try {
            val querySnapshot = db
                .collection("annunci")
                .whereEqualTo("materia", materia)
                .whereEqualTo("prezzo", prezzo)
                .whereEqualTo("descrizione", descrizione)
                .whereEqualTo("mod_on", online)
                .whereEqualTo("mod_pres", presenza)
                .whereEqualTo("tutor", tutorRef)
                .get().await()

            querySnapshot.documents.isNotEmpty()
        } catch (e: Exception) {
            false // Se c'è un errore, restituisce false
        }
    }

    suspend fun salvaAnnuncioConGeoPoint(
        geoPoint: GeoPoint,
        materia: String,
        prezzo: String,
        descrizione: String,
        online: Boolean,
        presenza: Boolean,
        tutorRef: DocumentReference
    ): Boolean {
        return try {

            // Inizia la transazione
            db.runTransaction { transaction ->

                // Crea l'oggetto Annuncio senza l'id
                val nuovoAnnuncio = Annuncio(
                    descrizione = descrizione.trim().replace("\\s+".toRegex(), " "),
                    materia = materia,
                    mod_on = online,
                    mod_pres = presenza,
                    posizione = geoPoint,
                    prezzo = prezzo,
                    tutor = tutorRef
                )

                // Aggiungi l'annuncio alla collezione e ottieni il riferimento del documento
                val documentReference = db.collection("annunci").document()

                // Usa la transazione per impostare i dati dell'annuncio nel documento
                transaction.set(documentReference, nuovoAnnuncio)

                // Ottieni l'id generato dal documento e aggiorna il campo id nell'annuncio
                val documentId = documentReference.id
                transaction.update(documentReference, "id", documentId)

                // Ritorna null per indicare successo
                null
            }.await()

            true // Successo
        } catch (e: Exception) {
            false // Fallimento
        }
    }



    // Funzione per ottenere il GeoPoint (usa una chiamata REST esterna)
    fun getGeoPoint(address: String): GeoPoint? {
        return try {
            val response = RetrofitInstance.api.getLocation(address).execute()
            if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                val location = response.body()!![0]
                GeoPoint(location.lat.toDouble(), location.lon.toDouble())
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Funzione per caricare i dati del profilo utente
    suspend fun getUserProfile(userId: String): Utente? {
        return try {
            val documentSnapshot = db.collection("utenti").document(userId).get().await()
            documentSnapshot.toObject(Utente::class.java)
        } catch (e: Exception) {
            null // Se c'è un errore restituiamo null
        }
    }


}