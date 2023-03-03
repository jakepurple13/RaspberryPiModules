package com.programmersbox.common

import androidx.compose.runtime.Composable

@Composable
public fun WiFiDiscoveryScreen(
    discover: (MutableList<DeviceIp>, isSearching: (Boolean) -> Unit) -> Unit,
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
    DiscoveryScreen(
        discover,
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