package de.piecha.switchwerk.domain.model

data class WifiProfile(
    val id: String,
    val ssid: String,
    val name: String = ssid,
    val lastSuccessfulSecurityType: WifiSecurityType? = null
)
