package com.example.tutormatch.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NominatimApi {
    @GET("search")
    fun getLocation(
        @Query("q") query: String,
        @Query("format") format: String = "json"
    ): Call<List<LocationResponse>>
}

data class LocationResponse(
    val lat: String,
    val lon: String,
    val display_name: String, // Descrizione completa dell'indirizzo
    val classType: String, // Nota: "class" è una parola chiave in Kotlin, quindi usiamo "classType"
    val type: String,
    val name: String
)