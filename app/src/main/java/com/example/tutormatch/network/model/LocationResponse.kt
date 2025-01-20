package com.example.tutormatch.network

// Classe che rappresenta la risposta dell'API di Nominatim
data class LocationResponse(
    val lat: String, // Latitudine della posizione
    val lon: String, // Longitudine della posizione
    val display_name: String, // Nome completo dell'indirizzo
    val address: Address? // Dettagli dell'indirizzo, rappresentati dalla classe Address
)

// Classe che rappresenta i dettagli dell'indirizzo
data class Address(
    val road: String?, // Nome della via
    val city: String?, // Nome della città
    val town: String?, // Nome della cittadina
    val village: String?, // Nome del villaggio
    val postcode: String?, // Codice postale
    val country: String? // Paese
)
