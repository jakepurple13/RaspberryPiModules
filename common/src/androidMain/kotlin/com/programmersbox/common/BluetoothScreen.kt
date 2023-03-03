package com.programmersbox.common

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@RequiresPermission(
    allOf = [
        android.Manifest.permission.BLUETOOTH_SCAN,
        android.Manifest.permission.BLUETOOTH_CONNECT,
        android.Manifest.permission.BLUETOOTH,
        android.Manifest.permission.BLUETOOTH_ADMIN,
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ]
)
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
    PermissionRequest(
        listOf(
            *if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                listOf(
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                )
            } else {
                listOf(
                    android.Manifest.permission.BLUETOOTH,
                    android.Manifest.permission.BLUETOOTH_ADMIN,
                )
            }.toTypedArray(),
            *if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                listOf(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            } else {
                listOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            }.toTypedArray(),
        ),
        onBackPress = onBackPress,
        wifiSetupText = wifiSetupText,
        permissionText = permissionText,
        permissionSecondaryText = permissionSecondaryText,
        enableText = enableText
    ) {
        val vm = viewModel { BluetoothViewModel(onConnect) }
        BluetoothDiscoveryScreen(
            state = vm.state,
            isConnecting = vm.connecting,
            device = vm.advertisement,
            deviceList = vm.advertisementList,
            onDeviceClick = { it?.let(vm::click) },
            deviceIdentifier = { it?.address.orEmpty() },
            deviceName = { it?.name ?: it?.peripheralName ?: "Device" },
            isDeviceSelected = { found, selected -> found?.address == selected?.address },
            networkItem = vm.networkItem,
            onNetworkItemClick = vm::networkClick,
            wifiNetworks = vm.wifiNetworks,
            connectToWifi = vm::connectToWifi,
            getNetworks = vm::getNetworks,
            ssid = { it?.e ?: "No SSID" },
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
}

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalPermissionsApi
@Composable
internal fun PermissionRequest(
    permissionsList: List<String>,
    onBackPress: () -> Unit,
    wifiSetupText: String,
    permissionText: String,
    permissionSecondaryText: String,
    enableText: String,
    content: @Composable () -> Unit
) {
    val permissions = rememberMultiplePermissionsState(permissionsList)
    val context = LocalContext.current
    SideEffect { permissions.launchMultiplePermissionRequest() }
    if (permissions.allPermissionsGranted) {
        content()
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(wifiSetupText) },
                    navigationIcon = { IconButton(onClick = onBackPress) { Icon(Icons.Default.ArrowBack, null) } }
                )
            }
        ) { padding ->
            if (permissions.shouldShowRationale) {
                NeedsPermissions(
                    padding,
                    permissionText,
                    permissionSecondaryText,
                    enableText
                ) { permissions.launchMultiplePermissionRequest() }
            } else {
                NeedsPermissions(
                    padding,
                    permissionText,
                    permissionSecondaryText,
                    enableText
                ) {
                    context.startActivity(
                        Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                    )
                }
            }
        }
    }
}

@Composable
internal fun NeedsPermissions(
    paddingValues: PaddingValues,
    permissionText: String,
    permissionSecondaryText: String,
    enableText: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 30.dp)
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = permissionText,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )

        Text(
            text = permissionSecondaryText,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Button(
            onClick = onClick,
            modifier = Modifier.padding(bottom = 4.dp)
        ) { Text(text = enableText) }
    }
}