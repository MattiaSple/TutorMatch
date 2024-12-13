package com.example.tutormatch

import com.example.tutormatch.data.model.Annuncio
import com.google.firebase.firestore.GeoPoint
import org.junit.Assert.*
import org.junit.Test

class AnnuncioTest {

    @Test
    fun `test creazione oggetto Annuncio con parametri`() {
        val geoPoint = GeoPoint(45.0, 12.0)
        val annuncio = Annuncio(
            id = "1",
            descrizione = "Ripetizioni di matematica",
            materia = "Matematica",
            mod_on = true,
            mod_pres = false,
            posizione = geoPoint,
            prezzo = "20",
            tutor = null // Puoi testare con un mock se necessario
        )

        assertEquals("1", annuncio.id)
        assertEquals("Ripetizioni di matematica", annuncio.descrizione)
        assertEquals("Matematica", annuncio.materia)
        assertTrue(annuncio.mod_on)
        assertFalse(annuncio.mod_pres)
        assertEquals(geoPoint, annuncio.posizione)
        assertEquals("20", annuncio.prezzo)
        assertNull(annuncio.tutor)
    }

    @Test
    fun `test funzione getModalita`() {
        val annuncioOnline = Annuncio(mod_on = true, mod_pres = false)
        assertEquals("Modalità: Online", annuncioOnline.getModalita())

        val annuncioPresenza = Annuncio(mod_on = false, mod_pres = true)
        assertEquals("Modalità: Presenza", annuncioPresenza.getModalita())

        val annuncioEntrambi = Annuncio(mod_on = true, mod_pres = true)
        assertEquals("Modalità: Online e Presenza", annuncioEntrambi.getModalita())

        val annuncioNessuna = Annuncio(mod_on = false, mod_pres = false)
        assertEquals("Modalità: Non specificata", annuncioNessuna.getModalita())
    }
}
