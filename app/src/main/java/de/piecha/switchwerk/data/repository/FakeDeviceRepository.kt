package de.piecha.switchwerk.data.repository

import de.piecha.switchwerk.domain.model.ApiCall
import de.piecha.switchwerk.domain.model.ApiMethod
import de.piecha.switchwerk.domain.model.Device
import de.piecha.switchwerk.domain.model.DeviceConnection

class FakeDeviceRepository : DeviceRepository {

    override suspend fun getDevices(): List<Device> =
        listOf(
            Device(
                id = "garage-door",
                name = "Garagentor",
                actionLabel = "Öffnen",
                apiCall = ApiCall(
                    method = ApiMethod.GET,
                    path = "/rpc/Switch.Set?id=0&on=true&toggle_after=1"
                ),
                connections = listOf(
                    DeviceConnection(
                        wifiProfileId = "garage-ap",
                        host = "192.168.33.1"
                    )
                ),
                sortOrder = 1
            ),
            Device(
                id = "workshop-light",
                name = "Werkstatt Licht",
                actionLabel = "Schalten",
                apiCall = ApiCall(
                    method = ApiMethod.GET,
                    path = "/rpc/Switch.Toggle?id=0"
                ),
                connections = listOf(
                    DeviceConnection(
                        wifiProfileId = "home-wifi",
                        host = "shelly-workshop.local"
                    )
                ),
                sortOrder = 2
            )
        )
}
