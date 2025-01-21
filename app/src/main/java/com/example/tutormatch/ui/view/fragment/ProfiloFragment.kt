package com.example.tutormatch.ui.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.tutormatch.databinding.FragmentProfiloBinding
import com.example.tutormatch.ui.view.activity.MainActivity
import com.example.tutormatch.ui.viewmodel.ProfiloViewModel

class ProfiloFragment : Fragment() {

    private var _binding: FragmentProfiloBinding? = null
    private val binding get() = _binding!!
    private lateinit var profiloViewModel: ProfiloViewModel

    private lateinit var userId: String
    private var ruolo: Boolean = false

    // Metodo onCreateView: Inflazione del layout e setup ViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfiloBinding.inflate(inflater, container, false)

        profiloViewModel = ViewModelProvider(this)[ProfiloViewModel::class.java]
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = profiloViewModel

        return binding.root
    }

    // Metodo onViewCreated: Imposta gli osservatori e gli eventi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recupera userId e ruolo
        userId = requireArguments().getString("userId")!!
        ruolo = requireArguments().getBoolean("ruolo")

        // Carica il profilo utente
        profiloViewModel.loadUserProfile(userId)

        // Listener per il bottone "Salva"
        binding.salva.setOnClickListener {
            profiloViewModel.saveUserProfile(userId)
        }

        // Listener per il bottone "Elimina Account"
        binding.eliminaAccount.setOnClickListener {
            showDeleteAccountDialog()
        }

        // Gestione del click sul pulsante di logout
        binding.btnLogout.setOnClickListener {
            profiloViewModel.logout()
        }

        // Osserva il LiveData showMessage per mostrare il Toast
        profiloViewModel.showMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        // Osserva l'evento di navigazione
        profiloViewModel.navigateToMain.observe(viewLifecycleOwner) { navigate ->
            if (navigate == true) {
                navigateToMainActivity()
            }
        }

        // Osserva i messaggi dal ViewModel
        profiloViewModel.message.observe(viewLifecycleOwner) { text ->
            text?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Mostra il dialog di conferma per l'eliminazione dell'account
    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Conferma eliminazione")
            .setMessage("Sei sicuro di voler eliminare il tuo account?\nQuesta operazione è irreversibile.")
            .setPositiveButton("Si") { dialog, _ ->
                profiloViewModel.eliminaDatiUtenteDaFirestore(userId, ruolo)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    // Naviga alla MainActivity dopo l'eliminazione dell'account
    private fun navigateToMainActivity() {
        val intent = Intent(activity, MainActivity::class.java)

        // Aggiungi i flag per resettare lo stack delle attività
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        startActivity(intent)
        activity?.finish() // Assicura di chiudere l'attività corrente
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
