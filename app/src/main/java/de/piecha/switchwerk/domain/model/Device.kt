package de.piecha.switchwerk.domain.model

data class Device(
    val id: String,
    val name: String,
    val actionLabel: String,
    val apiCall: ApiCall,
    val connections: List<DeviceConnection>,
    val sortOrder: Int
)
