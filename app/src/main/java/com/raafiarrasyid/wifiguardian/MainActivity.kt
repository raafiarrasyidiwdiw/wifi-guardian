
package com.raafiarrasyid.wifiguardian

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBackground = Color(0xFF090D14)
private val CardBackground = Color(0xFF151D29)
private val AccentBlue = Color(0xFF42A5F5)
private val AccentGreen = Color(0xFF35D39A)
private val AccentRed = Color(0xFFFF5252)
private val AccentOrange = Color(0xFFFFB74D)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = DarkBackground,
                    surface = CardBackground,
                    primary = AccentBlue
                )
            ) {
                Dashboard()
            }
        }
    }
}

@Composable
fun Dashboard() {
    val context = LocalContext.current

    var showScanner by remember { mutableStateOf(false) }

    var status by remember {
        mutableStateOf("Siap diperiksa")
    }

    var description by remember {
        mutableStateOf(
            "Hubungkan perangkat ke Wi-Fi untuk memulai pemeriksaan."
        )
    }

    var statusColor by remember {
        mutableStateOf(AccentGreen)
    }

    var checked by remember { mutableStateOf(false) }

    fun checkNetwork() {
        try {
            val manager = context.getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager

            val network = manager.activeNetwork
            val capabilities =
                manager.getNetworkCapabilities(network)

            when {
                capabilities == null -> {
                    status = "Tidak Terhubung"
                    description = "Tidak ditemukan koneksi aktif."
                    statusColor = AccentRed
                }

                capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                ) -> {
                    status = "Wi-Fi Terhubung"
                    description = "Jaringan Wi-Fi berhasil terdeteksi."
                    statusColor = AccentGreen
                }

                capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_CELLULAR
                ) -> {
                    status = "Data Seluler Aktif"
                    description = "Perangkat menggunakan jaringan seluler."
                    statusColor = AccentOrange
                }

                else -> {
                    status = "Jaringan Terdeteksi"
                    description = "Jenis jaringan belum dikenali."
                    statusColor = AccentOrange
                }
            }

            checked = true

        } catch (e: Exception) {
            status = "Pemeriksaan Gagal"
            description = e.message ?: "Terjadi kesalahan."
            statusColor = AccentRed
            checked = true
        }
    }

    if (showScanner) {
        DeviceScannerScreen(
            onBack = { showScanner = false }
        )
    } else {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DarkBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Column {
                    Text(
                        "WIFI GUARDIAN",
                        color = AccentBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Network Dashboard",
                        color = Color.White,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Lindungi dan optimalkan jaringanmu",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "STATUS JARINGAN",
                            color = AccentGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            status,
                            color = statusColor,
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            description,
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )

                        Button(
                            onClick = { checkNetwork() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (checked) "Periksa Ulang"
                                else "Periksa Jaringan"
                            )
                        }
                    }
                }

                Text(
                    "TOOLS & OPTIMIZATION",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                FeatureCard(
                    "Pemindai Perangkat",
                    "Periksa perangkat di jaringan lokal",
                    onClick = { showScanner = true }
                )

                FeatureCard(
                    "Keamanan Wi-Fi",
                    "Periksa jenis keamanan jaringan"
                )

                FeatureCard(
                    "Analisis Sinyal",
                    "Pantau kekuatan dan frekuensi Wi-Fi"
                )

                FeatureCard(
                    "Speed Test & Ping",
                    "Ukur kecepatan dan latensi"
                )

                FeatureCard(
                    "Rekomendasi DNS",
                    "Temukan pilihan DNS yang sesuai"
                )

                FeatureCard(
                    "Router Assistant",
                    "Panduan pengaturan router"
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "WiFi Guardian • Version 1.0",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun DeviceScannerScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var scanning by remember { mutableStateOf(false) }
    var scanned by remember { mutableStateOf(false) }

    var devices by remember {
        mutableStateOf<List<DeviceInfo>>(emptyList())
    }

    var error by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "‹  Kembali",
                color = AccentBlue,
                fontSize = 16.sp,
                modifier = Modifier.clickable { onBack() }
            )

            Text(
                "WIFI GUARDIAN",
                color = AccentBlue,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Pemindai Perangkat",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Temukan perangkat yang merespons di jaringan Wi-Fi lokal.",
                color = Color.LightGray,
                fontSize = 13.sp
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = CardBackground
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "STATUS PEMINDAIAN",
                        color = AccentGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )

                    Text(
                        when {
                            scanning -> "Sedang memindai..."
                            error != null -> "Pemindaian gagal"
                            scanned && devices.isNotEmpty() ->
                                "Pemindaian selesai"
                            scanned -> "Tidak ada perangkat merespons"
                            else -> "Siap memindai"
                        },
                        color = when {
                            scanning -> AccentOrange
                            error != null -> AccentRed
                            else -> AccentGreen
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (scanning) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = AccentBlue
                        )
                    }

                    Text(
                        when {
                            scanning ->
                                "Mohon tunggu. Sedang memeriksa alamat perangkat."
                            error != null -> error!!
                            scanned ->
                                "Ditemukan ${devices.size} perangkat yang merespons."
                            else ->
                                "Pastikan HP terhubung ke Wi-Fi sebelum memulai."
                        },
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )

                    Button(
                        onClick = {
                            if (!scanning) {
                                scanning = true
                                scanned = false
                                error = null
                                devices = emptyList()

                                NetworkScanner.scan(
                                    context.applicationContext
                                ) { result, message ->
                                    devices = result
                                    error = message
                                    scanned = true
                                    scanning = false
                                }
                            }
                        },
                        enabled = !scanning,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (scanning) "Memindai..."
                            else "Mulai Pemindaian"
                        )
                    }
                }
            }

            Text(
                "PERANGKAT TERDETEKSI",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            if (devices.isEmpty() && scanned && error == null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground
                    )
                ) {
                    Text(
                        "Belum ada perangkat yang merespons pemindaian. " +
                        "Perangkat yang memblokir ping mungkin tidak terdeteksi.",
                        color = Color.LightGray,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 13.sp
                    )
                }
            }

            devices.forEachIndexed { index, device ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = CardBackground
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "●",
                            color = AccentGreen,
                            fontSize = 20.sp
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                "Perangkat ${index + 1}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "IP: ${device.ipAddress}",
                                color = Color.LightGray,
                                fontSize = 13.sp
                            )

                            Text(
                                "Merespons jaringan",
                                color = AccentGreen,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Text(
                "WiFi Guardian • Network Scanner",
                color = Color.Gray,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    description: String,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    description,
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }

            Text(
                "›",
                color = AccentBlue,
                fontSize = 27.sp
            )
        }
    }
}
