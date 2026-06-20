package de.piecha.switchwerk.data.network

sealed interface WifiConnectionProgress {
    data object RequestStarted : WifiConnectionProgress

    data object NetworkFound : WifiConnectionProgress

    data object Connected : WifiConnectionProgress

    data object IpAddressAvailable : WifiConnectionProgress
}
