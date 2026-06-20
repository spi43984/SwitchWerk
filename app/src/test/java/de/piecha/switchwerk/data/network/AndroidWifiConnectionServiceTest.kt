package de.piecha.switchwerk.data.network

import android.net.LinkAddress
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import java.net.InetAddress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class AndroidWifiConnectionServiceTest {

    @Test
    fun readinessWaitsForWifiCapabilitiesAndIpRegardlessOfCallbackOrder() {
        val network = mock(Network::class.java)
        val linkProperties = linkPropertiesWithAddress("192.168.33.2")
        val capabilities = mock(NetworkCapabilities::class.java)
        `when`(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)).thenReturn(true)
        val events = mutableListOf<WifiConnectionProgress>()
        val readiness = AndroidWifiConnectionService.NetworkReadiness()

        readiness.markFound(network, events::add, isActive = true)
        val beforeCapabilities = readiness.updateLinkProperties(
            network,
            linkProperties,
            events::add,
            isActive = true
        )
        val readyNetwork = readiness.updateCapabilities(
            network,
            capabilities,
            events::add,
            isActive = true
        )

        assertNull(beforeCapabilities)
        assertSame(network, readyNetwork)
        assertEquals(
            listOf(
                WifiConnectionProgress.NetworkFound,
                WifiConnectionProgress.Connected,
                WifiConnectionProgress.IpAddressAvailable
            ),
            events
        )
    }

    @Test
    fun readinessIgnoresLateCallbacksAfterCompletion() {
        val network = mock(Network::class.java)
        val capabilities = mock(NetworkCapabilities::class.java)
        `when`(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)).thenReturn(true)
        val events = mutableListOf<WifiConnectionProgress>()
        val readiness = AndroidWifiConnectionService.NetworkReadiness()

        readiness.updateLinkProperties(
            network,
            linkPropertiesWithAddress("192.168.33.2"),
            events::add,
            isActive = true
        )
        readiness.updateCapabilities(network, capabilities, events::add, isActive = true)
        val lateResult = readiness.updateCapabilities(
            network,
            capabilities,
            events::add,
            isActive = true
        )

        assertNull(lateResult)
        assertEquals(3, events.size)
    }

    private fun linkPropertiesWithAddress(address: String): LinkProperties {
        val linkAddress = mock(LinkAddress::class.java)
        `when`(linkAddress.address).thenReturn(InetAddress.getByName(address))
        return mock(LinkProperties::class.java).also { linkProperties ->
            `when`(linkProperties.linkAddresses).thenReturn(listOf(linkAddress))
        }
    }

}
