package com.example.tutormatch.ui.view.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.tutormatch.databinding.FragmentProfiloBinding
import com.example.tutormatch.ui.view.activity.MainActivity
import com.example.tutormatch.ui.viewmodel.ProfiloViewModel

class ProfiloFragment : Fragment() {

    private var _binding: FragmentProfiloBinding? = null
    private val binding get() = _binding!!
    private lateinit var profiloViewModel: ProfiloViewModel


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

        val userId = arguments?.getString("userId")
        userId?.let {
            profiloViewModel.loadUserProfile(it)
        }

        val ruolo = arguments?.getBoolean("ruolo")!!


        // Listener per il bottone "Salva"
        binding.salva.setOnClickListener {
            userId?.let {
                profiloViewModel.saveUserProfile(it)
            }
        }

        // Osserva il LiveData showMessage per mostrare il Toast
        profiloViewModel.showMessage.observe(viewLifecycleOwner, Observer { message ->
            message?.let {
                // Mostra il Toast con il messaggio
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        })

        // Listener per il bottone "Elimina Account"
        binding.eliminaAccount.setOnClickListener {
            userId?.let {
                showDeleteAccountDialog(it, ruolo)
            }
        }

        // Osserva i messaggi dal ViewModel
        profiloViewModel.message.observe(viewLifecycleOwner) { text ->
            text?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                if (it == "Account e dati associati eliminati con successo.") {
                    navigateToMainActivity()
                }
            }
        }

    }

    // Mostra il dialog di conferma per l'eliminazione dell'account
    private fun showDeleteAccountDialog(userId: String, ruolo: Boolean) {
        AlertDialog.Builder(requireContext())
            .setTitle("Conferma eliminazione")
            .setMessage("Sei sicuro di voler eliminare il tuo account?\nQuesta operazione è irreversibile.")
            .setPositiveButton("Si") { dialog, _ ->
                profiloViewModel.eliminaDatiUtenteDaFirestore(ruolo)
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
        startActivity(intent)
        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
