
package com.raafiarrasyid.wifiguardian

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import java.io.File
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Callable
import java.util.concurrent.Executors

data class DeviceInfo(
    val ipAddress: String,
    val macAddress: String? = null,
    val deviceName: String? = null,
    val isRouter: Boolean = false
)

object NetworkScanner {

    private val mainHandler = Handler(Looper.getMainLooper())

    private val commonPorts = listOf(
        80, 443, 53, 22, 445,
        554, 8080, 8000, 5555
    )

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

                val localAddress = address.address
                val localIp = localAddress.hostAddress
                    ?: throw Exception("IP perangkat tidak terbaca.")

                val ipBytes = localAddress.address

                val ipNumber = ipBytes.fold(0L) { result, byte ->
                    (result shl 8) or (byte.toLong() and 0xFF)
                }

                val prefix = address.prefixLength

                if (prefix !in 1..30) {
                    throw Exception("Subnet Wi-Fi tidak didukung.")
                }

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

                val gatewayIp = properties.routes
                    .firstOrNull { it.isDefaultRoute }
                    ?.gateway?.hostAddress

                val executor = Executors.newFixedThreadPool(32)

                val devices = try {

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
                                val hostIp = host.hostAddress ?: return@Callable null

                                var reachable = false

                                // Periksa beberapa port TCP umum.
                                for (port in commonPorts) {
                                    try {
                                        Socket().use { socket ->
                                            socket.connect(
                                                InetSocketAddress(host, port),
                                                150
                                            )
                                        }

                                        reachable = true
                                        break

                                    } catch (_: Exception) {
                                        // Lanjutkan pemeriksaan port berikutnya.
                                    }
                                }

                                // Pemeriksaan tambahan menggunakan ICMP.
                                if (!reachable) {
                                    reachable = try {
                                        host.isReachable(300)
                                    } catch (_: Exception) {
                                        false
                                    }
                                }

                                if (reachable) {
                                    DeviceInfo(
                                        ipAddress = hostIp,
                                        isRouter = hostIp == gatewayIp
                                    )
                                } else {
                                    null
                                }
                            }
                        }

                    executor.invokeAll(tasks).mapNotNull {
                        try {
                            it.get()
                        } catch (_: Exception) {
                            null
                        }
                    }

                } finally {
                    executor.shutdownNow()
                }

                // Baca cache ARP jika Android mengizinkan.
                val arpEntries = readArpTable()

                val updatedDevices = devices.map { device ->
                    device.copy(
                        macAddress = arpEntries[device.ipAddress]
                    )
                }.toMutableList()

                // Pastikan gateway ditampilkan.
                if (
                    gatewayIp != null &&
                    updatedDevices.none { it.ipAddress == gatewayIp }
                ) {
                    updatedDevices.add(
                        DeviceInfo(
                            ipAddress = gatewayIp,
                            macAddress = arpEntries[gatewayIp],
                            deviceName = "Router Huawei",
                            isRouter = true
                        )
                    )
                }

                // Tandai gateway jika sudah terdeteksi.
                val finalDevices = updatedDevices.map { device ->
                    if (device.ipAddress == gatewayIp) {
                        device.copy(
                            isRouter = true,
                            deviceName = device.deviceName ?: "Router Huawei"
                        )
                    } else {
                        device
                    }
                }.sortedBy { it.ipAddress }

                mainHandler.post {
                    callback(finalDevices, null)
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

    private fun readArpTable(): Map<String, String> {

        val entries = mutableMapOf<String, String>()

        try {
            File("/proc/net/arp").bufferedReader().useLines { lines ->

                lines.drop(1).forEach { line ->

                    val parts = line.trim().split(Regex("\\s+"))

                    if (parts.size >= 4) {

                        val ip = parts[0]
                        val mac = parts[3]

                        if (
                            mac.matches(
                                Regex(
                                    "(?i)([0-9a-f]{2}:){5}[0-9a-f]{2}"
                                )
                            ) &&
                            mac != "00:00:00:00:00:00"
                        ) {
                            entries[ip] = mac.uppercase()
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Cache ARP mungkin tidak dapat diakses Android.
        }

        return entries
    }
}
