package com.example.tutormatch.ui.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.tutormatch.R
import com.example.tutormatch.databinding.FragmentRicercaTutorBinding
import com.example.tutormatch.ui.viewmodel.RicercaTutorViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class RicercaTutorFragment : Fragment() {

    private lateinit var _binding: FragmentRicercaTutorBinding
    private val binding get() = _binding
    private lateinit var viewModel: RicercaTutorViewModel
    private lateinit var mapView: MapView
    private val materie = listOf("Matematica", "Fisica", "Chimica", "Inglese", "Storia", "Geografia")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[RicercaTutorViewModel::class.java]

        _binding = FragmentRicercaTutorBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Configura MapView
        mapView = binding.mapView
        Configuration.getInstance().load(context,
            context?.let { androidx.preference.PreferenceManager.getDefaultSharedPreferences(it) })
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(10.0)
        mapView.controller.setCenter(GeoPoint(41.9028, 12.4964))  // Coordinate centrali di default (Roma, Italia)

        // Configura lo Spinner delle Materie
        val subjectSpinner = binding.subjectSpinner
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, materie)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        subjectSpinner.adapter = adapter

        // Osserva i LiveData dal ViewModel
        viewModel.annunci.observe(viewLifecycleOwner, Observer { annunci ->
            mapView.overlays.clear()
            for (annuncio in annunci) {
                val position = GeoPoint(annuncio.posizione.latitude, annuncio.posizione.longitude)
                val marker = Marker(mapView)
                marker.position = position
                marker.title = annuncio.descrizione
                mapView.overlays.add(marker)
            }
            mapView.invalidate()  // Aggiorna la mappa
        })

        // Carica i dati iniziali
        viewModel.loadAnnunci()

        // Gestisci il click del pulsante Applica Filtri
        binding.applyButton.setOnClickListener {
            val selectedMateria = subjectSpinner.selectedItem.toString()
            val budget = binding.budgetEditText.text.toString().toDoubleOrNull() ?: Double.MAX_VALUE
            val selectedModalita = when (binding.modeRadioGroup.checkedRadioButtonId) {
                R.id.onlineRadioButton -> "Online"
                R.id.inPersonRadioButton -> "In presenza"
                else -> ""
            }
            viewModel.applyFilters(selectedMateria, budget, selectedModalita)
        }

        // Gestisci il click del pulsante Reset
        binding.resetButton.setOnClickListener {
            binding.subjectSpinner.setSelection(0)
            binding.budgetEditText.text.clear()
            binding.modeRadioGroup.clearCheck()
            viewModel.loadAnnunci()
        }

        return root
    }
}
