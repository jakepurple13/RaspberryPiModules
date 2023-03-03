package com.programmersbox

import kotlinx.coroutines.delay
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo

class NetworkHandling {

    private var jmdns: JmDNS? = null

    private fun setup() {
        try {
            if (jmdns == null) {
                val ipAddresses = getIpAddresses()

                jmdns = JmDNS.create(
                    InetAddress.getByName(ipAddresses.find { it.addressType == AddressType.SiteLocal }!!.address)
                )

                // Register a service
                val serviceInfo = ServiceInfo.create(
                    "_http._tcp.local.",
                    "pillcounter",
                    8080,
                    "path=index.html"
                )

                jmdns?.registerService(serviceInfo)
            }
        } catch (e: Exception) {
            println("Something went wrong here!")
            e.printStackTrace()
        }
    }

    suspend fun internetListener() {
        delay(10000)
        while (true) {
            try {
                val ipAddresses = getIpAddresses()
                if (
                    !InetAddress.getByName(ipAddresses.find { it.addressType == AddressType.SiteLocal }?.address)
                        .isReachable(10000)
                ) {
                    throw Exception("Can't reach network!")
                } else {
                    setup()
                }
                delay(10000)
            } catch (e: Exception) {
                e.printStackTrace()
                closeAll()
            }
        }
    }

    fun closeAll() {
        jmdns?.unregisterAllServices()
        jmdns = null
    }
}

suspend fun configureWifi(networkHandling: NetworkHandling) {
    networkHandling.internetListener()
}

fun getIpAddresses(): List<IpAddressInfo> {
    val ip = mutableListOf<IpAddressInfo>()
    try {
        val enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces()
        while (enumNetworkInterfaces.hasMoreElements()) {
            val networkInterface = enumNetworkInterfaces.nextElement()
            val enumInetAddress = networkInterface.inetAddresses
            while (enumInetAddress.hasMoreElements()) {
                val inetAddress = enumInetAddress.nextElement()
                if (inetAddress.isLoopbackAddress) {
                    ip += IpAddressInfo(inetAddress.hostAddress, AddressType.Loopback)
                } else if (inetAddress.isSiteLocalAddress) {
                    ip += IpAddressInfo(inetAddress.hostAddress, AddressType.SiteLocal)
                } else if (inetAddress.isLinkLocalAddress) {
                    ip += IpAddressInfo(inetAddress.hostAddress, AddressType.LinkLocal)
                } else if (inetAddress.isMulticastAddress) {
                    ip += IpAddressInfo(inetAddress.hostAddress, AddressType.Multicast)
                }
                ip += IpAddressInfo(
                    inetAddress.hostAddress,
                    when {
                        inetAddress.isLoopbackAddress -> AddressType.Loopback
                        inetAddress.isSiteLocalAddress -> AddressType.SiteLocal
                        inetAddress.isLinkLocalAddress -> AddressType.LinkLocal
                        inetAddress.isMulticastAddress -> AddressType.Multicast
                        else -> AddressType.None
                    }
                )
            }
        }
    } catch (e: SocketException) {
        e.printStackTrace()
    }
    return ip
}

enum class AddressType {
    Loopback, SiteLocal, LinkLocal, Multicast, None
}

data class IpAddressInfo(
    val address: String,
    val addressType: AddressType
)