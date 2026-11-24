
package com.raafiarrasyid.wifiguardian

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import java.net.Inet4Address
import java.net.InetAddress
import java.util.concurrent.Executors
import java.util.concurrent.Callable

data class DeviceInfo(
    val ipAddress: String
)

object NetworkScanner {

    private val mainHandler = Handler(Looper.getMainLooper())

    fun scan(
        context: Context,
        callback: (List<DeviceInfo>, String?) -> Unit
    ) {
        Thread {
            try {
                val manager = context.getSystemService(
                    Context.CONNECTIVITY_SERVICE
                ) as ConnectivityManager

                val network = manager.activeNetwork
                    ?: throw Exception("Tidak ada jaringan aktif.")

                val capabilities =
                    manager.getNetworkCapabilities(network)
                        ?: throw Exception("Informasi jaringan tidak tersedia.")

                if (!capabilities.hasTransport(
                        NetworkCapabilities.TRANSPORT_WIFI
                    )
                ) {
                    throw Exception(
                        "Hubungkan HP ke Wi-Fi terlebih dahulu."
                    )
                }

                val properties = manager.getLinkProperties(network)
                    ?: throw Exception("Informasi Wi-Fi tidak tersedia.")

                val address = properties.linkAddresses.firstOrNull {
                    it.address is Inet4Address
                } ?: throw Exception("Alamat IPv4 tidak ditemukan.")

                val localIp = address.address.hostAddress
                    ?: throw Exception("IP perangkat tidak terbaca.")

                val ipBytes = address.address.address

                val ipNumber = ipBytes.fold(0L) { result, byte ->
                    (result shl 8) or (byte.toLong() and 0xFF)
                }

                val prefix = address.prefixLength

                val mask = (0xFFFFFFFFL shl (32 - prefix)) and
                    0xFFFFFFFFL

                val networkAddress = ipNumber and mask
                val broadcastAddress =
                    networkAddress or (mask xor 0xFFFFFFFFL)

                var start = networkAddress + 1
                var end = broadcastAddress - 1

                // Batasi pemindaian maksimal 254 alamat.
                if (end - start + 1 > 254) {
                    start = (ipNumber and 0xFFFFFF00L) + 1
                    end = (ipNumber and 0xFFFFFF00L) + 254
                }

                val executor = Executors.newFixedThreadPool(32)

                try {
                    val tasks = (start..end)
                        .filter { it != ipNumber }
                        .map { number ->
                            Callable<DeviceInfo?> {
                                val bytes = byteArrayOf(
                                    (number shr 24).toByte(),
                                    (number shr 16).toByte(),
                                    (number shr 8).toByte(),
                                    number.toByte()
                                )

                                val host = InetAddress.getByAddress(bytes)

                                if (host.isReachable(350)) {
                                    DeviceInfo(host.hostAddress ?: "")
                                } else {
                                    null
                                }
                            }
                        }

                    val results = executor.invokeAll(tasks)

                    val devices = results.mapNotNull {
                        it.get()
                    }.sortedBy { it.ipAddress }

                    mainHandler.post {
                        callback(devices, null)
                    }

                } finally {
                    executor.shutdownNow()
                }

            } catch (e: Exception) {
                mainHandler.post {
                    callback(
                        emptyList(),
                        e.message ?: "Pemindaian gagal."
                    )
                }
            }
        }.start()
    }
}
