package me.proton.android.pass.network.api

import kotlinx.coroutines.flow.Flow

enum class NetworkStatus {
    Online,
    Offline
}

interface NetworkMonitor {
    val connectivity: Flow<NetworkStatus>
}
