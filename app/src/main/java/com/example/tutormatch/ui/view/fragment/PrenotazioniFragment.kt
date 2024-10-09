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
import com.example.tutormatch.util.FirebaseUtil

class PrenotazioniFragment : Fragment() {

    private var _binding: FragmentPrenotazioniBinding? = null
    private val binding get() = _binding!!
    private lateinit var prenotazioneViewModel: PrenotazioneViewModel
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var adapterPrenotazione: PrenotazioneAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prenotazioneViewModel = ViewModelProvider(this).get(PrenotazioneViewModel::class.java)
        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrenotazioniBinding.inflate(inflater, container, false)
        return binding.root
    }
    private fun showConfirmationDialog(message: String, onConfirmAction: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton("Sì") { _, _ ->
                // Se l'utente conferma, esegui l'azione di conferma
                onConfirmAction()
            }
            .setNegativeButton("No", null)
            .show()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = arguments?.getString("userId")
        val ruolo = arguments?.getBoolean("ruolo")
        // Osserva il messaggio di creazione della chat
        chatViewModel.chatCreationMessage.observe(viewLifecycleOwner, Observer { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        })

        adapterPrenotazione = PrenotazioneAdapter(
            emptyList(),
            ruolo!!,
            onDeleteClick = { prenotazione ->
                prenotazioneViewModel.eliminaPrenotazione(prenotazione)
            },
            onChatClick = { prenotazione ->
                // Prima recuperiamo i dettagli dell'annuncio per ottenere la materia e il tutor
                FirebaseUtil.getAnnuncio(prenotazione.annuncioRef!!) { annuncio ->
                    FirebaseUtil.getUserFromFirestore(prenotazione.studenteRef) { studente ->
                        if (annuncio != null) {
                            FirebaseUtil.getUserFromFirestore(annuncio.tutor?.id ?: "") { tutor ->
                                if (studente != null && tutor != null) {
                                    if (annuncio != null) {
                                        chatViewModel.creaChatConTutor(
                                            tutorEmail = tutor.email,
                                            tutorName = tutor.nome,
                                            tutorSurname = tutor.cognome,
                                            userName = studente.nome,
                                            userSurname = studente.cognome,
                                            materia = annuncio.materia,
                                            onSuccess = {
                                            },
                                            onFailure = { errorMessage ->
                                                Toast.makeText(context, "Errore: $errorMessage", Toast.LENGTH_SHORT).show()
                                            },
                                            onConfirm = { message, onConfirmAction ->
                                                // Mostra un dialogo di conferma all'utente
                                                showConfirmationDialog(message, onConfirmAction)
                                            }
                                        )
                                    }
                                } else {
                                    Toast.makeText(requireContext(), "Errore nel recupero dei dettagli", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }
            }
        )

        binding.recyclerViewPrenotazioni.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewPrenotazioni.adapter = adapterPrenotazione

        // Carica le prenotazioni dell'utente
        userId?.let {
            prenotazioneViewModel.caricaPrenotazioni(ruolo, it)
        }

        prenotazioneViewModel.listaPrenotazioni.observe(viewLifecycleOwner, Observer { prenotazioni ->
            adapterPrenotazione.updatePrenotazioni(prenotazioni)
        })

        prenotazioneViewModel.notificaPrenotazione.observe(viewLifecycleOwner, Observer { messaggio ->
            messaggio?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
