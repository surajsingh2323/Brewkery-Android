package com.example.brewkery.data.remote

import com.example.brewkery.data.model.MenuItem
import com.example.brewkery.data.model.MenuResponse
import retrofit2.http.GET
import retrofit2.http.Path

/** Retrofit interface. `suspend` functions run off the main thread automatically. */
interface BrewkeryApi {

    @GET("data.json")
    suspend fun getMenu(): MenuResponse

    /** Not required by the UI (list payload already has every field) but kept for completeness. */
    @GET("api/items/{id}.json")
    suspend fun getItem(@Path("id") id: Int): MenuItem

    companion object {
        const val BASE_URL = "https://raw.githubusercontent.com/VivekShah138/Brewkery/main/"
    }
}