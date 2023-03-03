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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.benasher44.uuid.uuidFrom
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.juul.kable.*
import com.juul.kable.logs.Logging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class BluetoothViewModel(
    private val onConnect: () -> Unit
) : ViewModel() {

    private val scanner by lazy {
        Scanner {
            filters = listOf(Filter.Service(uuidFrom(BluetoothConstants.SERVICE_WIRELESS_SERVICE)))
            logging {
                level = Logging.Level.Events
                data = Logging.DataProcessor { bytes ->
                    bytes.joinToString { byte -> byte.toString() } // Show data as integer representation of bytes.
                }
            }
        }
    }

    val advertisementList = mutableStateListOf<Advertisement>()
    val wifiNetworks = mutableStateListOf<NetworkList>()
    var networkItem: NetworkList? by mutableStateOf(null)
    var state by mutableStateOf(BluetoothState.Searching)

    var advertisement: Advertisement? by mutableStateOf(null)
    var connecting by mutableStateOf(false)

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private var peripheral: Peripheral? = null

    init {
        scanner.advertisements
            .onEach { a ->
                if (advertisementList.none { it.address == a.address }) advertisementList.add(a)
            }
            .launchIn(viewModelScope)
    }

    private fun disconnect() {
        viewModelScope.cancel()
    }

    fun connect() {
        viewModelScope.launch {
            connecting = true
            try {
                peripheral?.connect()
            } catch (e: Exception) {
                e.printStackTrace()
                return@launch
            } finally {
                connecting = false
            }
            state = BluetoothState.Wifi

            var networkString by mutableStateOf("")
            var refresh = false

            peripheral?.observe(
                characteristicOf(
                    BluetoothConstants.SERVICE_WIRELESS_SERVICE,
                    BluetoothConstants.CHARACTERISTIC_WIRELESS_COMMANDER_RESPONSE
                )
            )
                ?.onEach {
                    println("Kable content")
                    println(it)
                    if (refresh) networkString = ""
                    networkString += it.decodeToString()
                    refresh = 10 in it
                }
                ?.launchIn(viewModelScope)

            snapshotFlow { networkString }
                .onEach { println(it) }
                .filter { it.isNotEmpty() }
                .filter { "\n" in it }
                .onEach {
                    println("Command!")
                    val value = it.removeSuffix("\n")
                    val command = json.decodeFromString<SingleCommand>(value)
                    println(command)
                    when (command.c) {
                        0 -> {
                            wifiNetworks.clear()
                            wifiNetworks.addAll(json.decodeFromString<WifiList>(value).p)
                        }

                        1 -> {

                        }

                        2 -> {

                        }

                        4 -> {

                        }
                    }
                }
                .launchIn(viewModelScope)

            peripheral?.state
                ?.onEach { println(it) }
                ?.launchIn(viewModelScope)

            sendScan()
            getNetworks()
        }
    }

    private fun sendScan() {
        viewModelScope.launch {
            peripheral?.write(
                characteristicOf(
                    BluetoothConstants.SERVICE_WIRELESS_SERVICE,
                    BluetoothConstants.CHARACTERISTIC_WIRELESS_COMMANDER
                ),
                BluetoothConstants.SCAN.toByteArray(),
                WriteType.WithResponse
            )
        }
    }

    fun getNetworks() {
        viewModelScope.launch {
            peripheral?.write(
                characteristicOf(
                    BluetoothConstants.SERVICE_WIRELESS_SERVICE,
                    BluetoothConstants.CHARACTERISTIC_WIRELESS_COMMANDER
                ),
                BluetoothConstants.GET_NETWORKS.toByteArray(),
                WriteType.WithResponse
            )
        }
    }

    fun click(advertisement: Advertisement) {
        this.advertisement = advertisement
        peripheral = viewModelScope.peripheral(advertisement) { transport = Transport.Le }
    }

    fun networkClick(networkList: NetworkList?) {
        networkItem = networkList
    }

    fun connectToWifi(password: String) {
        peripheral?.let { p ->
            networkItem?.e?.let { ssid ->
                viewModelScope.launch {
                    (Json.encodeToString(ConnectRequest(c = 1, p = WiFiInfo(e = ssid, p = password))) + "\n")
                        .also { println(it) }
                        .chunked(20) { it.toString().toByteArray() }
                        .onEach {
                            println(it.contentToString())
                            println(it.decodeToString())
                        }
                        .forEach {
                            p.write(
                                characteristicOf(
                                    BluetoothConstants.SERVICE_WIRELESS_SERVICE,
                                    BluetoothConstants.CHARACTERISTIC_WIRELESS_COMMANDER
                                ),
                                it,
                                WriteType.WithResponse
                            )
                        }
                    state = BluetoothState.Checking
                    delay(5000)
                    p.disconnect()
                    onConnect()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}
