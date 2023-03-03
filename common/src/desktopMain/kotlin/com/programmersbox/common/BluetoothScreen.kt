package com.programmersbox.common

import androidx.compose.runtime.Composable

@Composable
public actual fun BluetoothDiscovery(
    onBackPress: () -> Unit,
    onConnect: () -> Unit,
    wifiSetupText: String,
    permissionText: String,
    permissionSecondaryText: String,
    enableText: String,
    pleaseWait: String,
    connect: String,
    findDevice: String,
    connectDeviceToWiFi: String,
    refreshNetworks: String,
    ssidText: String,
    password: String,
    releaseToRefresh: String,
    refreshing: String,
    pullToRefresh: String
) {
    error("Desktop is not supported")
}