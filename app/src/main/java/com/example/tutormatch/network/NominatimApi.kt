package com.example.tutormatch.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NominatimApi {
    @GET("search")
    fun getLocation(
        @Query("q") query: String,  //Query per la ricerca dell'indirizzo.
        @Query("format") format: String = "json",  //Specifica che il formato della risposta sarà JSON.
        @Query("addressdetails") addressDetails: Int = 1, // Impostato a 1 per includere i dettagli dell'indirizzo.
        @Query("limit") limit: Int = 5, // Limita il numero massimo di risultati.
        @Query("countrycodes") countryCodes: String = "it", // Limita i risultati all'Italia
        @Query("accept-language") language: String = "it" // Imposta la lingua italiana
    ): Call<List<LocationResponse>>
}