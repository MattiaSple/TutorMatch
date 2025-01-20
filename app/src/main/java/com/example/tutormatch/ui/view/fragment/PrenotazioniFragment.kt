package com.example.tutormatch.ui.view.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tutormatch.databinding.FragmentPrenotazioniBinding
import com.example.tutormatch.ui.adapter.PrenotazioneAdapter
import com.example.tutormatch.ui.viewmodel.ChatViewModel
import com.example.tutormatch.ui.viewmodel.PrenotazioneViewModel

class PrenotazioniFragment : Fragment() {

    private var _binding: FragmentPrenotazioniBinding? = null // Binding per il layout del fragment
    private val binding get() = _binding!! // Assicura che il binding non sia nullo
    private lateinit var prenotazioneViewModel: PrenotazioneViewModel // ViewModel per le prenotazioni
    private lateinit var chatViewModel: ChatViewModel // ViewModel per le chat
    private lateinit var adapterPrenotazione: PrenotazioneAdapter // Adapter per le prenotazioni

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inizializza i ViewModel
        prenotazioneViewModel = ViewModelProvider(this)[PrenotazioneViewModel::class.java]
        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrenotazioniBinding.inflate(inflater, container, false)
        return binding.root // Ritorna la root del layout
    }

    // Mostra un dialogo di conferma
    private fun showConfirmationDialog(message: String, onConfirmAction: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton("Sì") { _, _ ->
                onConfirmAction() // Esegui l'azione se confermata
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = requireArguments().getString("userId")!! // ID dell'utente
        val ruolo = requireArguments().getBoolean("ruolo") // Ruolo dell'utente (tutor o studente)

        // Osserva i messaggi di creazione della chat
        chatViewModel.chatCreationMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        // Osserva i dati necessari per creare la chat
        prenotazioneViewModel.datiChat.observe(viewLifecycleOwner) { (annuncio, studente, tutor) ->
            if (annuncio != null && studente != null && tutor != null) {
                // Crea la chat utilizzando i dati forniti
                chatViewModel.creaChatConTutor(
                    tutorEmail = tutor.email,
                    studenteEmail = studente.email,
                    tutorName = tutor.nome,
                    tutorSurname = tutor.cognome,
                    userName = studente.nome,
                    userSurname = studente.cognome,
                    materia = annuncio.materia,
                    onSuccess = {
                        Toast.makeText(requireContext(), "Chat creata con successo!", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { errorMessage ->
                        Toast.makeText(requireContext(), "Errore: $errorMessage", Toast.LENGTH_SHORT).show()
                    },
                    onConfirm = { message, onConfirmAction ->
                        showConfirmationDialog(message, onConfirmAction)
                    }
                )
            } else {
                Toast.makeText(requireContext(), "Errore nel recupero dei dettagli", Toast.LENGTH_LONG).show()
            }
        }

        // Configura l'adapter per il RecyclerView
        adapterPrenotazione = PrenotazioneAdapter(
            emptyList(),
            ruolo,
            onDeleteClick = { prenotazione ->
                prenotazioneViewModel.eliminaPrenotazione(prenotazione) // Elimina la prenotazione
            },
            onChatClick = { prenotazione ->
                prenotazioneViewModel.recuperaDatiChat(prenotazione) // Recupera i dati per la chat
            }
        )

        // Configura il RecyclerView
        binding.recyclerViewPrenotazioni.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewPrenotazioni.adapter = adapterPrenotazione

        // Carica le prenotazioni dell'utente
        prenotazioneViewModel.caricaPrenotazioni(ruolo, userId)

        // Osserva la lista delle prenotazioni e aggiorna l'adapter
        prenotazioneViewModel.listaPrenotazioni.observe(viewLifecycleOwner, Observer { prenotazioni ->
            adapterPrenotazione.updatePrenotazioni(prenotazioni)
        })

        // Osserva le notifiche di prenotazione
        prenotazioneViewModel.notificaPrenotazione.observe(viewLifecycleOwner, Observer { messaggio ->
            messaggio?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Rimuove il binding per evitare memory leaks
    }
}
