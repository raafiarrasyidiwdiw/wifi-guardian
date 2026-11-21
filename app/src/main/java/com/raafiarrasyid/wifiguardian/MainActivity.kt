package com.raafiarrasyid.wifiguardian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBackground = Color(0xFF090D14)
private val CardBackground = Color(0xFF151D29)
private val AccentBlue = Color(0xFF42A5F5)
private val AccentGreen = Color(0xFF35D39A)

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
                        "Siap diperiksa",
                        color = Color.White,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Hubungkan perangkat ke Wi-Fi untuk memulai pemeriksaan.",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )

                    Button(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Periksa Jaringan")
                    }
                }
            }

            Text(
                "TOOLS & OPTIMIZATION",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            FeatureCard("Pemindai Perangkat", "Periksa perangkat di jaringan lokal")
            FeatureCard("Keamanan Wi-Fi", "Periksa jenis keamanan jaringan")
            FeatureCard("Analisis Sinyal", "Pantau kekuatan dan frekuensi Wi-Fi")
            FeatureCard("Speed Test & Ping", "Ukur kecepatan dan latensi")
            FeatureCard("Rekomendasi DNS", "Temukan pilihan DNS yang sesuai")
            FeatureCard("Router Assistant", "Panduan pengaturan router")

            Spacer(modifier = Modifier.weight(1f))

            Text(
                "WiFi Guardian • Version 1.0",
                color = Color.Gray,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun FeatureCard(title: String, description: String) {
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
            Column(modifier = Modifier.weight(1f)) {
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
