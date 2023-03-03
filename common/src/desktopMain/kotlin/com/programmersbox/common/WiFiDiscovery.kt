package com.programmersbox.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.InetAddress
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener

@Composable
public fun WiFiDiscoveryScreen(
    onConnect: (url: String) -> Unit,
    openBLEDiscovery: () -> Unit,
    onBackPress: () -> Unit,
    discoveryText: String = "Discovery",
    releaseToRefresh: String = "Release to Refresh",
    refreshing: String = "Refreshing",
    pullToRefresh: String = "Pull to Refresh",
    needToConnectDeviceToWiFiText: String = "Need to Connect Device to WiFi?",
    enterIpAddressText: String = "Enter IP Address",
    discoverText: String = "Discover",
    manualIPText: String = "Manual IP"
) {
    val scope = rememberCoroutineScope()
    DiscoveryScreen(
        discover = { list, isSearching ->
            isSearching(true)
            scope.launch(Dispatchers.IO) {
                val jmdns = JmDNS.create(InetAddress.getByName("10.0.0.2"), "HOST")
                jmdns.addServiceListener(
                    "_http._tcp.local.",
                    object : ServiceListener {
                        override fun serviceAdded(event: ServiceEvent) {
                        }

                        override fun serviceRemoved(event: ServiceEvent) {
                        }

                        override fun serviceResolved(event: ServiceEvent) {
                            list.addAll(
                                event.info.inet4Addresses.mapNotNull {
                                    it.hostAddress?.let { it1 -> DeviceIp(it1, it.hostName) }
                                }
                            )
                        }
                    }
                )
                delay(30000)
                jmdns.unregisterAllServices()
                isSearching(false)
            }
        },
        onConnect,
        openBLEDiscovery,
        onBackPress,
        discoveryText,
        releaseToRefresh,
        refreshing,
        pullToRefresh,
        needToConnectDeviceToWiFiText,
        enterIpAddressText,
        discoverText,
        manualIPText
    )
}