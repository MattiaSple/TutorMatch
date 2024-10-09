package com.example.tutormatch.ui.view.fragment

import android.content.Intent
import android.os.Bundle
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

    // Variabili per memorizzare i valori iniziali di residenza, via e cap
    private var initialResidenza: String? = null
    private var initialVia: String? = null
    private var initialCap: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        profiloViewModel = ViewModelProvider(this).get(ProfiloViewModel::class.java)

        _binding = FragmentProfiloBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = profiloViewModel

        // Recupera l'ID dell'utente dal bundle
        val userId = arguments?.getString("userId")
        userId?.let {
            profiloViewModel.loadUserProfile(it)
        }

        return binding.root
    }

    private fun showResidenceChangedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Modifica residenza")
            .setMessage("Hai modificato la tua residenza, ti ricordiamo di modificare i tuoi annunci!")
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = arguments?.getString("userId")
        userId?.let {
            profiloViewModel.loadUserProfile(it)
        }

        // Osserva i cambiamenti nelle variabili residenza, via e cap per salvare i valori iniziali
        profiloViewModel.residenza.observe(viewLifecycleOwner, Observer { newValue ->
            if (initialResidenza == null) {
                initialResidenza = newValue
            }
        })

        profiloViewModel.via.observe(viewLifecycleOwner, Observer { newValue ->
            if (initialVia == null) {
                initialVia = newValue
            }
        })

        profiloViewModel.cap.observe(viewLifecycleOwner, Observer { newValue ->
            if (initialCap == null) {
                initialCap = newValue
            }
        })

        // Imposta il listener per il pulsante Salva
        binding.salva.setOnClickListener {
            userId?.let {
                if (isAddressChanged() && profiloViewModel.isTutor.value == true) {
                    showResidenceChangedDialog()
                    updateInitialAddressValues() // Aggiorna i valori iniziali con i nuovi
                }
                profiloViewModel.saveUserProfile(it)
            }
        }

        // Imposta il listener per il pulsante Elimina Account
        binding.eliminaAccount.setOnClickListener {
            userId?.let {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Conferma eliminazione")
                builder.setMessage("Sei sicuro di voler eliminare il tuo account?\nQuesta operazione è irreversibile.")
                builder.setPositiveButton("Si") { dialog, _ ->
                    profiloViewModel.eliminaDatiUtenteDaFirestore(userId)
                    dialog.dismiss()
                }
                builder.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                builder.create().show()
            }
        }

        profiloViewModel.message.observe(viewLifecycleOwner, Observer { text ->
            text?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                if (it == "Account e dati associati eliminati con successo.") {
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                }
            }
        })
    }

    // Funzione per verificare se l'indirizzo è stato modificato
    private fun isAddressChanged(): Boolean {
        val currentResidenza = profiloViewModel.residenza.value
        val currentVia = profiloViewModel.via.value
        val currentCap = profiloViewModel.cap.value

        return currentResidenza != initialResidenza ||
                currentVia != initialVia ||
                currentCap != initialCap
    }

    // Aggiorna i valori iniziali con i nuovi valori dopo il salvataggio
    private fun updateInitialAddressValues() {
        initialResidenza = profiloViewModel.residenza.value
        initialVia = profiloViewModel.via.value
        initialCap = profiloViewModel.cap.value
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
