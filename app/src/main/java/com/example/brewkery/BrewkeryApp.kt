package com.example.brewkery

import android.app.Application
import com.example.brewkery.data.remote.BrewkeryApi
import com.example.brewkery.data.repository.CartRepository
import com.example.brewkery.data.repository.MenuRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Tiny manual dependency container (no Hilt -> fewer moving parts for a 4-6h task).
 * Easy to explain in the interview: "I'd swap this for Hilt in a bigger app".
 */
class AppContainer {
    private val api: BrewkeryApi by lazy {
        Retrofit.Builder()
            .baseUrl(BrewkeryApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BrewkeryApi::class.java)
    }
    val menuRepository: MenuRepository by lazy { MenuRepository(api) }
    val cartRepository: CartRepository by lazy { CartRepository() }
}

class BrewkeryApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}