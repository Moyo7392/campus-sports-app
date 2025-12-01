package edu.uta.campussports.screens

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ToolsScreen() {
    var selectedTool by remember { mutableStateOf<String?>(null) }

    when (selectedTool) {
        "coin_flipper" -> CoinFlipperScreen()
        "buzzer" -> BuzzerScreen(onBack = { selectedTool = null })
        "score_keeper" -> ScoreKeeperScreen()
        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Text(
                        text = "Tools & Utilities",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Quick access to useful sports tools",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    ToolButton(
                        title = "Coin Flipper",
                        description = "Flip a coin to make quick decisions",
                        icon = Icons.Default.Build,
                        backgroundColor = Color(0xFF6A5ACD),
                        onClick = { selectedTool = "coin_flipper" }
                    )
                }

                item {
                    ToolButton(
                        title = "Buzzer",
                        description = "Sound buzzer for games and events",
                        icon = Icons.Default.Info,
                        backgroundColor = Color(0xFF20B2AA),
                        onClick = { selectedTool = "buzzer" }
                    )
                }

                item {
                    ToolButton(
                        title = "Score Keeper",
                        description = "Keep track of scores during matches",
                        icon = Icons.Default.Star,
                        backgroundColor = Color(0xFFFF6347),
                        onClick = { selectedTool = "score_keeper" }
                    )
                }

                item {
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun ToolButton(
    title: String,
    description: String,
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = Color.White
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun BuzzerScreen(onBack: () -> Unit) {
    var isPlaying by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back button at top
            Box(
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 20.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Buzzer",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tap the button to sound the buzzer",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(80.dp))

            // Large buzzer button
            Button(
                onClick = {
                    isPlaying = true
                    playBuzzerSound()
                },
                modifier = Modifier
                    .size(200.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF20B2AA)
                ),
                enabled = !isPlaying
            ) {
                Text(
                    text = if (isPlaying) "Playing..." else "BUZZ",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Info text
            Text(
                text = "Buzzer will play for 1 second",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Reset playing state after sound finishes
            LaunchedEffect(isPlaying) {
                if (isPlaying) {
                    kotlinx.coroutines.delay(1000)
                    isPlaying = false
                }
            }
        }
    }
}

// Function to play buzzer sound using AudioTrack
private fun playBuzzerSound() {
    Thread {
        try {
            val sampleRate = 44100
            val duration = 1 // seconds
            val frequency = 1000 // Hz (1000 Hz is a common buzzer frequency)
            val amplitude = 32767 // Maximum amplitude for 16-bit audio

            val numSamples = sampleRate * duration
            val audioData = ShortArray(numSamples)

            // Generate sine wave for buzzer sound
            for (i in 0 until numSamples) {
                val sample = (amplitude * Math.sin(2.0 * Math.PI * frequency * i / sampleRate)).toInt().toShort()
                audioData[i] = sample
            }

            // Create and configure AudioTrack
            val audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        android.media.AudioFormat.Builder()
                            .setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(audioData.size * 2)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                AudioTrack(
                    android.media.AudioManager.STREAM_MUSIC,
                    sampleRate,
                    android.media.AudioFormat.CHANNEL_OUT_MONO,
                    android.media.AudioFormat.ENCODING_PCM_16BIT,
                    audioData.size * 2,
                    android.media.AudioTrack.MODE_STREAM
                )
            }

            audioTrack.play()
            audioTrack.write(audioData, 0, audioData.size)
            Thread.sleep(duration * 1000L + 100) // Wait for playback to finish
            audioTrack.stop()
            audioTrack.release()
        } catch (e: Exception) {
            println("Error playing buzzer: ${e.message}")
            e.printStackTrace()
        }
    }.start()
}
