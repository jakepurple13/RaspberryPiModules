package com.programmersbox.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DiscoveryScreen(
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
    val vm = remember {
        DiscoveryViewModel(discover, onConnect)
    }
    var ip by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.startDiscovery() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(discoveryText) },
                navigationIcon = { IconButton(onClick = onBackPress) { Icon(Icons.Default.ArrowBack, null) } }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier.background(MaterialTheme.colorScheme.background)
            ) {
                if (hasBLEDiscovery) {
                    OutlinedButton(
                        onClick = openBLEDiscovery,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(needToConnectDeviceToWiFiText) }
                }
                OutlinedTextField(
                    value = ip,
                    onValueChange = { ip = it },
                    label = { Text(enterIpAddressText) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .padding(bottom = 4.dp),
                    trailingIcon = {
                        IconButton(
                            onClick = { vm.connect(ip) }
                        ) { Icon(Icons.Default.Add, null) }
                    }
                )
                LinearProgressIndicator(
                    progress = animateFloatAsState(
                        targetValue = if (vm.isSearching) 0f else 1f,
                        animationSpec = if (vm.isSearching) tween(30000) else tween(0)
                    ).value,
                    modifier = Modifier.fillMaxWidth()
                )
                BottomAppBar(
                    actions = {
                        OutlinedButton(
                            onClick = { vm.startDiscovery() },
                            enabled = !vm.isSearching,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        ) { Text(discoverText) }
                    },
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            text = { Text(manualIPText) },
                            icon = { Icon(Icons.Default.Add, null) },
                            onClick = { if (ip.isNotEmpty()) vm.connect(ip) }
                        )
                    }
                )
            }
        }
    ) { padding ->
        PullToRefresh(
            state = rememberPullToRefreshState(vm.isSearching),
            onRefresh = { vm.discover(vm.discoveredList) { vm.isSearching = it } },
            indicatorPadding = padding,
            indicator = { state, refreshTrigger, refreshingOffset ->
                PullToRefreshIndicator(
                    state,
                    refreshTrigger,
                    refreshingOffset,
                    releaseToRefresh = { releaseToRefresh },
                    refreshing = { refreshing },
                    pullToRefresh = { pullToRefresh }
                )
            }
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = padding,
                modifier = Modifier.fillMaxSize()
            ) {
                items(vm.filteredDiscoveredList) {
                    OutlinedCard(
                        onClick = { vm.connect(it.ip) }
                    ) {
                        ListItem(
                            headlineText = { Text(it.name) },
                            supportingText = { Text(it.ip) }
                        )
                    }
                }
            }
        }
    }
}

internal class DiscoveryViewModel(
    val discover: (MutableList<DeviceIp>, isSearching: (Boolean) -> Unit) -> Unit,
    private val onConnect: (url: String) -> Unit
) {
    var isSearching by mutableStateOf(false)
    val discoveredList = mutableStateListOf<DeviceIp>()
    val filteredDiscoveredList by derivedStateOf { discoveredList.distinct() }

    fun startDiscovery() {
        if (!isSearching) {
            discoveredList.clear()
            discover(discoveredList) { isSearching = it }
        }
    }

    fun connect(url: String) {
        onConnect(url)
    }

}

public data class DeviceIp(val ip: String, val name: String)