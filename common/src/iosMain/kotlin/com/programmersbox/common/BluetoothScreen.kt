package com.programmersbox.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

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
    val vm = remember { BluetoothViewModel(onConnect) }
    DisposableEffect(Unit) { onDispose { vm.disconnect() } }
    BluetoothDiscoveryScreen(
        state = vm.state,
        isConnecting = vm.connecting,
        device = vm.advertisement,
        deviceList = vm.advertisementList,
        onDeviceClick = { it?.let(vm::click) },
        deviceIdentifier = { it?.identifier?.UUIDString.orEmpty() },
        deviceName = { it?.name ?: it?.peripheralName ?: "Device" },
        isDeviceSelected = { found, selected -> found?.identifier == selected?.identifier },
        networkItem = vm.networkItem,
        onNetworkItemClick = vm::networkClick,
        wifiNetworks = vm.wifiNetworks,
        connectToWifi = vm::connectToWifi,
        getNetworks = vm::getNetworks,
        ssid = { it?.e.orEmpty() },
        signalStrength = { it?.s ?: 0 },
        connectOverBle = vm::connect,
        onBackPress = onBackPress,
        pleaseWait = pleaseWait,
        connect = connect,
        connectDeviceToWiFi = connectDeviceToWiFi,
        refreshNetworks = refreshNetworks,
        ssidText = ssidText,
        password = password,
        releaseToRefresh = releaseToRefresh,
        refreshing = refreshing,
        pullToRefresh = pullToRefresh,
        findDevice = findDevice,
    )
}