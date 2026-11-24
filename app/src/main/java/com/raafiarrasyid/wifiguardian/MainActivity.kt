
package com.raafiarrasyid.wifiguardian

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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

    val status = remember {
        mutableStateOf("Siap diperiksa")
    }

    val description = remember {
        mutableStateOf(
            "Hubungkan perangkat ke Wi-Fi untuk memulai pemeriksaan."
        )
    }

    val statusColor = remember {
        mutableStateOf(AccentGreen)
    }

    val checked = remember {
        mutableStateOf(false)
    }

    fun checkNetwork() {

        try {
            val manager = context.getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager

            val network = manager.activeNetwork

            val capabilities =
                manager.getNetworkCapabilities(network)

            if (capabilities == null) {

                status.value = "Tidak Terhubung"
                description.value =
                    "Tidak ditemukan koneksi jaringan aktif."

                statusColor.value = AccentRed

            } else {

                val wifi = capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_WIFI
                )

                val mobile = capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_CELLULAR
                )

                val ethernet = capabilities.hasTransport(
                    NetworkCapabilities.TRANSPORT_ETHERNET
                )

                val hasInternet = capabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                )

                val validated = capabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_VALIDATED
                )

                when {

                    wifi && validated -> {
                        status.value = "Wi-Fi Terhubung"
                        description.value =
                            "Wi-Fi aktif dan akses internet berhasil terdeteksi."

                        statusColor.value = AccentGreen
                    }

                    wifi && hasInternet -> {
                        status.value = "Wi-Fi Terhubung"
                        description.value =
                            "Wi-Fi aktif, tetapi akses internet belum terverifikasi."

                        statusColor.value = AccentOrange
                    }

                    wifi -> {
                        status.value = "Wi-Fi Tanpa Internet"
                        description.value =
                            "Perangkat terhubung ke Wi-Fi, tetapi akses internet tidak tersedia."

                        statusColor.value = AccentRed
                    }

                    mobile && validated -> {
                        status.value = "Data Seluler Aktif"
                        description.value =
                            "Perangkat terhubung melalui jaringan seluler dan internet tersedia."

                        statusColor.value = AccentGreen
                    }

                    mobile -> {
                        status.value = "Data Seluler Terhubung"
                        description.value =
                            "Jaringan seluler terdeteksi, tetapi internet belum terverifikasi."

                        statusColor.value = AccentOrange
                    }

                    ethernet && validated -> {
                        status.value = "Ethernet Terhubung"
                        description.value =
                            "Koneksi kabel aktif dan internet tersedia."

                        statusColor.value = AccentGreen
                    }

                    else -> {
                        status.value = "Jaringan Terdeteksi"
                        description.value =
                            "Ada koneksi jaringan, tetapi jenis atau akses internet belum dapat dipastikan."

                        statusColor.value = AccentOrange
                    }
                }
            }

            checked.value = true

        } catch (e: Exception) {

            status.value = "Pemeriksaan Gagal"
            description.value =
                "Terjadi kesalahan saat memeriksa jaringan: ${e.message}"

            statusColor.value = AccentRed
            checked.value = true
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
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
                        status.value,
                        color = statusColor.value,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        description.value,
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )

                    Button(
                        onClick = {
                            checkNetwork()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (checked.value)
                                "Periksa Ulang"
                            else
                                "Periksa Jaringan"
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
                "Periksa perangkat di jaringan lokal"
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

            Spacer(modifier = Modifier.weight(1f))

            Text(
                "WiFi Guardian • Version 1.0",
                color = Color.Gray,
                fontSize = 11.sp,
                modifier = Modifier.align(
                    Alignment.CenterHorizontally
                )
            )
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    description: String
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
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
