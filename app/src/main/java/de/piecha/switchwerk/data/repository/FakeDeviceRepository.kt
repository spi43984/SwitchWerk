package de.piecha.switchwerk.data.repository

import de.piecha.switchwerk.domain.model.Device

class FakeDeviceRepository : DeviceRepository {
    override suspend fun getDevices(): List<Device> = emptyList()
}
