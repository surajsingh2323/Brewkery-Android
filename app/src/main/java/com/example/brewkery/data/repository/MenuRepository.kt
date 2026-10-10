package com.example.brewkery.data.repository

import com.example.brewkery.data.model.MenuItem
import com.example.brewkery.data.model.MenuResponse
import com.example.brewkery.data.remote.BrewkeryApi
import kotlin.coroutines.cancellation.CancellationException

/**
 * Single source of truth for menu data.
 * Wraps network errors in Result so ViewModels never deal with try/catch.
 * Keeps the last response in memory so the detail screen can look items up
 * without a second network call.
 */
class MenuRepository(private val api: BrewkeryApi) {

    var cachedMenu: MenuResponse? = null
        private set

    suspend fun getMenu(): Result<MenuResponse> = try {
        Result.success(api.getMenu().also { cachedMenu = it })
    } catch (e: CancellationException) {
        throw e // never swallow coroutine cancellation
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun findItem(id: Int): MenuItem? = cachedMenu?.items?.firstOrNull { it.id == id }
}