package com.dimitrijeilic.rygg.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectivityObserver @Inject constructor(
    @ApplicationContext context: Context
) {
    private val connectivityManager = context.getSystemService(ConnectivityManager::class.java)

    fun observeNetwork(): Flow<NetworkState> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                trySend(capabilities.toState())
            }

            override fun onLost(network: Network) {
                trySend(NetworkState.OFFLINE)
            }
        }
        trySend(currentState())
        connectivityManager.registerDefaultNetworkCallback(callback)
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.conflate().distinctUntilChanged()

    private fun currentState(): NetworkState =
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.toState()
            ?: NetworkState.OFFLINE

    private fun NetworkCapabilities.toState() = NetworkState(
        isOnline = hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET),
        isMetered = !hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
    )
}

data class NetworkState(
    val isOnline: Boolean,
    val isMetered: Boolean
) {
    companion object {
        val OFFLINE = NetworkState(isOnline = false, isMetered = false)
    }
}
