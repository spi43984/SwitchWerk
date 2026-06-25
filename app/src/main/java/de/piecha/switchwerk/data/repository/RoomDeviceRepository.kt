package de.piecha.switchwerk.data.repository

import de.piecha.switchwerk.data.local.dao.DeviceConnectionDao
import de.piecha.switchwerk.data.local.dao.DeviceDao
import de.piecha.switchwerk.data.local.entity.DeviceConnectionEntity
import de.piecha.switchwerk.data.local.entity.DeviceEntity
import de.piecha.switchwerk.domain.model.ApiCall
import de.piecha.switchwerk.domain.model.ApiContentType
import de.piecha.switchwerk.domain.model.ApiMethod
import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.domain.model.DeviceConnection
import de.piecha.switchwerk.domain.model.DeviceProtocol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class RoomDeviceRepository(
    private val deviceDao: DeviceDao,
    private val deviceConnectionDao: DeviceConnectionDao
) : DeviceRepository {

    override fun observeDevices(): Flow<List<Device>> {
        return combine(
            deviceDao.observeAll(),
            deviceConnectionDao.observeAll()
        ) { devices, connections ->
            devices.map { device ->
                device.toDomain(
                    connections = connections
                        .filter { it.deviceId == device.id }
                        .sortedBy { it.priority }
                )
            }
        }
    }

    override suspend fun getDevices(): List<Device> {
        return deviceDao.getAll().map { device ->
            device.toDomain(
                connections = deviceConnectionDao.getForDevice(device.id)
            )
        }
    }

    override suspend fun saveDevice(device: Device) {
        deviceDao.upsert(device.toEntity())
        deviceConnectionDao.deleteForDevice(device.id)
        deviceConnectionDao.upsertAll(
            device.connections.mapIndexed { index, connection ->
                connection.toEntity(
                    deviceId = device.id,
                    priority = index
                )
            }
        )
    }

    override suspend fun updateDeviceOrder(deviceIds: List<String>) {
        val existingDevices = deviceDao.getAll()
        val existingDeviceIds = existingDevices.map { it.id }.toSet()
        val orderedDeviceIds = deviceIds.filter { it in existingDeviceIds } +
            existingDevices.map { it.id }.filterNot { it in deviceIds }

        orderedDeviceIds
            .forEachIndexed { index, deviceId ->
                deviceDao.updateSortOrder(
                    id = deviceId,
                    sortOrder = index
                )
            }
    }

    override suspend fun deleteDevice(deviceId: String) {
        deviceConnectionDao.deleteForDevice(deviceId)
        deviceDao.deleteById(deviceId)
    }

    private fun DeviceEntity.toDomain(connections: List<DeviceConnectionEntity>): Device {
        return Device(
            id = id,
            name = name,
            actionLabel = actionLabel,
            protocol = DeviceProtocol.valueOf(apiProtocol),
            apiCall = ApiCall(
                method = ApiMethod.valueOf(apiMethod),
                path = apiPath,
                requestBody = apiRequestBody,
                contentType = ApiContentType.valueOf(apiContentType)
            ),
            connections = connections.map { it.toDomain() },
            sortOrder = sortOrder
        )
    }

    private fun Device.toEntity(): DeviceEntity {
        return DeviceEntity(
            id = id,
            name = name,
            actionLabel = actionLabel,
            apiProtocol = protocol.name,
            apiMethod = apiCall.method.name,
            apiPath = apiCall.path,
            apiRequestBody = apiCall.requestBody,
            apiContentType = apiCall.contentType.name,
            sortOrder = sortOrder
        )
    }

    private fun DeviceConnectionEntity.toDomain(): DeviceConnection {
        return DeviceConnection(
            wifiProfileId = wifiProfileId,
            host = host
        )
    }

    private fun DeviceConnection.toEntity(
        deviceId: String,
        priority: Int
    ): DeviceConnectionEntity {
        return DeviceConnectionEntity(
            id = "$deviceId:$wifiProfileId",
            deviceId = deviceId,
            wifiProfileId = wifiProfileId,
            host = host,
            priority = priority
        )
    }
}
