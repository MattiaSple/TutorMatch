package com.example.tutormatch.ui.viewmodel

import com.example.tutormatch.data.model.Annuncio
import com.google.firebase.firestore.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.junit.runner.RunWith
import androidx.lifecycle.Observer
import com.example.tutormatch.data.model.Calendario
import com.example.tutormatch.viewmodel.ScadenzeViewModel
import org.junit.Assert.assertTrue
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@Config(manifest=Config.NONE)
@RunWith(RobolectricTestRunner::class)
class TestClass {

    private lateinit var application: android.app.Application
    private lateinit var viewModelAnnunci: AnnunciViewModel
    private lateinit var viewModelCalendario: CalendarioViewModel
    private lateinit var viewModelScadenze: ScadenzeViewModel

    @Before
    fun setUp() {
        application = RuntimeEnvironment.application
        viewModelAnnunci = AnnunciViewModel(application)
        viewModelCalendario = CalendarioViewModel(application)
        viewModelScadenze = ScadenzeViewModel()
    }

    @Test
    fun testListaAnnuncio() {
        val geoPoint = GeoPoint(45.0, 12.0)
        val annuncio = Annuncio(
            id = "1",
            descrizione = "Ripetizioni di matematica",
            materia = "Matematica",
            mod_on = true,
            mod_pres = false,
            posizione = geoPoint,
            prezzo = "20",
            tutor = null
        )

        // Aggiungere l'annuncio alla lista esistente
        val annunciList = mutableListOf<Annuncio>()
        annunciList.add(annuncio)
        viewModelAnnunci._listaAnnunci.postValue(annunciList)

        // Observer per verificare il risultato
        val observer = Observer<List<Annuncio>> { lista ->
            assertEquals(1, lista.size)
            assertEquals("Ripetizioni di matematica", lista[0].descrizione)
        }
        viewModelAnnunci.listaAnnunci.observeForever(observer)

        // Cleanup
        viewModelAnnunci.listaAnnunci.removeObserver(observer)
    }
    @Test
    fun testOrdinaFasceOrarie() {
        // Crea una lista non ordinata di oggetti Calendario
        val fasceOrarie = listOf(
            Calendario(oraInizio = "14:00", oraFine = "15:00"),
            Calendario(oraInizio = "09:00", oraFine = "10:00"),
            Calendario(oraInizio = "11:00", oraFine = "12:00")
        )

        // Ordina la lista utilizzando la funzione
        val sortedFasceOrarie = viewModelCalendario.ordinaFasceOrarie(fasceOrarie)

        // Controlla che l'ordine sia corretto confrontando solo i campi rilevanti
        val sortedOraInizio = sortedFasceOrarie.map { it.oraInizio }
        val expectedOraInizio = listOf("09:00", "11:00", "14:00")

        // Verifica che gli orari siano ordinati come previsto
        assertEquals(expectedOraInizio, sortedOraInizio)
    }
    @Test
    fun `test generateOrari con lista vuota`() {
        // Lista di orari esistenti vuota
        val existingOrari = emptyList<String>()

        // Esegui la funzione
        val result = viewModelCalendario.generateOrari(existingOrari)

        // Crea l'elenco atteso
        val expected = (0..23).map { String.format("%02d:00", it) }

        // Verifica che il risultato corrisponda
        assertEquals(expected, result)
    }

    @Test
    fun `test generateOrari con orari già esistenti`() {
        // Alcuni orari già esistenti
        val existingOrari = listOf("00:00", "12:00", "23:00")

        // Esegui la funzione
        val result = viewModelCalendario.generateOrari(existingOrari)

        // Crea l'elenco atteso, escludendo gli orari esistenti
        val expected = (0..23).map { String.format("%02d:00", it) }.filterNot { existingOrari.contains(it) }

        // Verifica che il risultato corrisponda
        assertEquals(expected, result)
    }

    @Test
    fun `test generateOrari con tutti gli orari esistenti`() {
        // Tutti gli orari già esistenti
        val existingOrari = (0..23).map { String.format("%02d:00", it) }

        // Esegui la funzione
        val result = viewModelCalendario.generateOrari(existingOrari)

        // Il risultato dovrebbe essere una lista vuota
        assertEquals(emptyList<String>(), result)
    }
    @Test
    fun scadenzeTest() {
        // Esegui la funzione da testare
        viewModelScadenze.eseguiOperazioneAlleProssimaOra()

        // Simula il passare del tempo (usando Robolectric)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        // Verifica se l'operazione è stata eseguita
        assertTrue(
            "La funzione eseguiOperazioniPeriodiche dovrebbe essere stata chiamata",
            viewModelScadenze.operazioneEseguita
        )
    }
}
