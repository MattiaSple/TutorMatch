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

    private lateinit var _binding: FragmentProfiloBinding
    private val binding get() = _binding
    private lateinit var profiloViewModel: ProfiloViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        profiloViewModel = ViewModelProvider(this).get(ProfiloViewModel::class.java)

        _binding = FragmentProfiloBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = profiloViewModel

        // Recupera l'ID dell'utente dal bundle
        val userId = arguments?.getString("userId")
        userId?.let {
            profiloViewModel.loadUserProfile(it)
        }

        // Imposta il listener per il pulsante Salva
        binding.salva.setOnClickListener {
            userId?.let {

                profiloViewModel.saveUserProfile(it)
            }
        }

        //Elimina account
        binding.eliminaAccount.setOnClickListener{
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
                if(it == "Account e dati associati eliminati con successo.")
                {
                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                }
            }
        })

        return binding.root
    }
}


