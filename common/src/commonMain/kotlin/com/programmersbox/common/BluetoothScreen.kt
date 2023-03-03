package com.programmersbox.common

import androidx.compose.runtime.Composable

@Composable
public expect fun BluetoothDiscovery(
    onBackPress: () -> Unit,
    onConnect: () -> Unit,
    wifiSetupText: String = "WiFi Setup",
    permissionText: String = "Please Enable Bluetooth/Nearby Devices and Location Permissions",
    permissionSecondaryText: String = "They are needed to connect to a PillCounter device",
    enableText: String = "Enable",
    pleaseWait: String = "Please Wait...",
    connect: String = "Connect",
    findDevice: String = "Find Device",
    connectDeviceToWiFi: String = "Connect Device to WiFi",
    refreshNetworks: String = "Refresh Networks",
    ssidText: String = "SSID",
    password: String = "Password",
    releaseToRefresh: String = "Release to Refresh",
    refreshing: String = "Refreshing",
    pullToRefresh: String = "Pull to Refresh"
)