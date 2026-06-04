package de.piecha.switchwerk.data.repository

import de.piecha.switchwerk.domain.model.Device

interface DeviceRepository {
    suspend fun getDevices(): List<Device>
}
