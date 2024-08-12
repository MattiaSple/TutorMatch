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
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class RicercaTutorFragment : Fragment() {

    private lateinit var _binding: FragmentRicercaTutorBinding
    private lateinit var mapView: MapView
    private lateinit var viewModel: RicercaTutorViewModel
    private val materie = listOf("Matematica", "Fisica", "Chimica", "Inglese", "Storia", "Geografia") // Lista di materie

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(RicercaTutorViewModel::class.java)

        _binding = FragmentRicercaTutorBinding.inflate(inflater, container, false)
        val root: View = _binding.root

        // Configure MapView
        mapView = _binding.mapView
        Configuration.getInstance().load(context,
            context?.let { androidx.preference.PreferenceManager.getDefaultSharedPreferences(it) })
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(10.0)
        mapView.controller.setCenter(GeoPoint(0.0, 0.0))

        // Set up the subject spinner
        val subjectSpinner = _binding.subjectSpinner
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, materie)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        subjectSpinner.adapter = adapter

        // Observe the LiveData from the ViewModel
        viewModel.annunci.observe(viewLifecycleOwner, Observer { annunci ->
            mapView.overlays.clear()
            for (annuncio in annunci) {
                val position = GeoPoint(annuncio.posizione.latitude, annuncio.posizione.longitude)
                val marker = Marker(mapView)
                marker.position = position
                marker.title = annuncio.descrizione
                mapView.overlays.add(marker)
            }
            mapView.invalidate()
        })

        // Load initial data
        viewModel.loadAnnunci()

        // Set up the apply button click listener
        _binding.applyButton.setOnClickListener {
            val selectedMateria = subjectSpinner.selectedItem.toString()
            val budget = _binding.budgetEditText.text.toString().toDoubleOrNull() ?: Double.MAX_VALUE
            val selectedModalita = when (_binding.modeRadioGroup.checkedRadioButtonId) {
                R.id.onlineRadioButton -> "Online"
                R.id.inPersonRadioButton -> "In presenza"
                else -> ""
            }
            viewModel.applyFilters(selectedMateria, budget, selectedModalita)
        }

        // Set up the reset button click listener
        _binding.resetButton.setOnClickListener {
            _binding.subjectSpinner.setSelection(0)
            _binding.budgetEditText.text.clear()
            _binding.modeRadioGroup.clearCheck()
            viewModel.loadAnnunci()
        }

        return root
    }
}
