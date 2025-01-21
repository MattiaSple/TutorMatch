package com.example.tutormatch.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Oggetto singleton per gestire l'istanza di Retrofit
object RetrofitInstance {

    // Logging per monitorare le richieste e le risposte HTTP
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Livello dettagliato: mostra corpo delle richieste e risposte
    }

    // Configurazione del client HTTP con interceptor e timeout
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging) // Aggiunge il logger per il debug delle richieste
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                // Aggiunta di un header personalizzato User-Agent per identificare l'applicazione
                .header("User-Agent", "TutorMatchApp/1.0 (contact@example.com)")
                .build()
            chain.proceed(request) // Prosegue con la richiesta modificata
        }
        .connectTimeout(30, TimeUnit.SECONDS) // Timeout per la connessione
        .readTimeout(30, TimeUnit.SECONDS)    // Timeout per la lettura dei dati
        .build()

    // Inizializzazione lazy dell'API Retrofit
    val api: NominatimApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/") // URL base per l'API
            .client(client) // Assegna il client HTTP configurato
            .addConverterFactory(GsonConverterFactory.create()) // Converte JSON in oggetti Kotlin
            .build()
            .create(NominatimApi::class.java) // Crea l'implementazione dell'interfaccia API
    }
}
