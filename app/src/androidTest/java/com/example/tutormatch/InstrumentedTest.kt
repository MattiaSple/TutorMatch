package com.example.tutormatch.ui.view.activity

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tutormatch.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InstrumentedTest {
    @get:Rule
    var activityRule: ActivityScenarioRule<MainActivity>
            = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun simulation() {
        // Simula il click sul pulsante di registrazione
        onView(withId(R.id.registrazione))
            .perform((click()))
        Thread.sleep(1000) // Utilizzare Thread.sleep non è ideale, poiché rallenta i test e li rende fragili.

        // Configurazione dei dati per il test
        val email = "provatutor@provatutor.provatutor"
        val password = "provatutor@provatutor.provatutor"
        val spinnerSelected = "Chimica"
        val price = "38"
        val description = "descizione per il test simulazione"

        // Dati "shuffled" per il test
        val shuffledEmail = email.toList().shuffled().joinToString("")
        val shuffledPassword = password.toList().shuffled().joinToString("")

        // Inserimento di email e password errate (shuffled)
        onView(withId(R.id.email))
            .perform(typeText(shuffledEmail), closeSoftKeyboard())
        Thread.sleep(1000)
        onView(withId(R.id.password))
            .perform(typeText(shuffledPassword), closeSoftKeyboard())
        Thread.sleep(1000)

        // Simula il tentativo di login
        onView(withId(R.id.accedi))
            .perform((click()))
        Thread.sleep(1000)

        // Verifica e correzione dei dati di accesso
        if (email != shuffledEmail || password != shuffledPassword) {
            onView(withId(R.id.email))
                .perform(clearText(), typeText(email), closeSoftKeyboard())
            onView(withId(R.id.password))
                .perform(clearText(), typeText(password), closeSoftKeyboard())
            Thread.sleep(1000)
            onView(withId(R.id.accedi))
                .perform((click()))
            Thread.sleep(1000)
        }

        // Selezione di una materia dallo spinner
        onView(withId(R.id.spinnerMateria))
            .perform(click())
        Thread.sleep(1000)
        onView(withText(spinnerSelected))
            .perform(click())
        Thread.sleep(1000)

        // Inserimento del prezzo e della descrizione
        onView(withId(R.id.editTextNumber))
            .perform(typeText(price), closeSoftKeyboard())
        Thread.sleep(1000)
        onView(withId(R.id.editTextDescrizione))
            .perform(typeText(description), closeSoftKeyboard())
        Thread.sleep(1000)

        // Simulazione dell'interazione con le checkbox
        onView(withId(R.id.checkBoxOnline))
            .perform(click()) // Primo click
        Thread.sleep(1000)
        onView(withId(R.id.checkBoxOnline))
            .perform(click()) // Secondo click
        Thread.sleep(1000)
        onView(withId(R.id.checkBoxOnline))
            .perform(click()) // Terzo click
        Thread.sleep(1000)

        // Interazione con un'altra checkbox
        onView(withId(R.id.checkBoxPresenza))
            .perform(click())
        Thread.sleep(1000)

        // Salvataggio dei dati
        onView(withId(R.id.buttonSalva))
            .perform((click()))
        Thread.sleep(5000) // Attesa finale, non ideale per test automatizzati
    }
}
