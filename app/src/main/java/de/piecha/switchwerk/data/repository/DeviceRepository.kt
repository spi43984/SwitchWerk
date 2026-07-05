package de.piecha.switchwerk.data.repository

import de.piecha.switchwerk.domain.model.Device
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun observeDevices(): Flow<List<Device>>
    suspend fun getDevices(): List<Device>
    suspend fun saveDevice(device: Device)
    suspend fun updateDeviceOrder(deviceIds: List<String>)
    suspend fun updateDeviceSortOrders(sortOrders: Map<String, Int>)
    suspend fun deleteDevice(deviceId: String)
}
