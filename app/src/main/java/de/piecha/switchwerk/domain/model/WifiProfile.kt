package de.piecha.switchwerk.domain.model

data class WifiProfile(
    val id: String,
    val ssid: String,
    val name: String = ssid,
    val connectionMode: WifiConnectionMode = WifiConnectionMode.SWITCHWERK_MANAGED,
    val lastSuccessfulSecurityType: WifiSecurityType? = null,
    val isSecurityTypeVerifiedLocally: Boolean = true
)
