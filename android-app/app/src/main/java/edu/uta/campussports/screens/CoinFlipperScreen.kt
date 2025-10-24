package edu.uta.campussports.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun CoinFlipperScreen() {
    var team1Name by remember { mutableStateOf("") }
    var team2Name by remember { mutableStateOf("") }
    var showFlipInterface by remember { mutableStateOf(false) }
    var flipResult by remember { mutableStateOf<String?>(null) }
    var isFlipping by remember { mutableStateOf(false) }
    var coinRotation by remember { mutableStateOf(0f) }

    if (!showFlipInterface) {
        // Team name input screen
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text(
                    text = "Coin Flipper",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Enter team names to flip a coin",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                OutlinedTextField(
                    value = team1Name,
                    onValueChange = { team1Name = it },
                    label = { Text("Team 1 Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    placeholder = { Text("Enter team name") }
                )
            }

            item {
                OutlinedTextField(
                    value = team2Name,
                    onValueChange = { team2Name = it },
                    label = { Text("Team 2 Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    placeholder = { Text("Enter team name") }
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (team1Name.isNotBlank() && team2Name.isNotBlank()) {
                            showFlipInterface = true
                            flipResult = null
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = team1Name.isNotBlank() && team2Name.isNotBlank()
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    } else {
        // Coin flip interface
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Button(
                    onClick = {
                        showFlipInterface = false
                        team1Name = ""
                        team2Name = ""
                        flipResult = null
                    },
                    colors = ButtonDefaults.outlinedButtonColors(),
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Text("← Back")
                }
            }

            item {
                Text(
                    text = "Coin Flip",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Spacer(Modifier.height(20.dp))

                // Coin animation
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color(0xFFFFD700), CircleShape)
                        .rotate(coinRotation),
                    contentAlignment = Alignment.Center
                ) {
                    if (coinRotation < 180) {
                        Text(
                            text = "H",
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    } else {
                        Text(
                            text = "T",
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = {
                        isFlipping = true
                        flipResult = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = !isFlipping
                ) {
                    Text(
                        text = "Flip Coin",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (flipResult != null) {
                item {
                    Spacer(Modifier.height(20.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Coin Result",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            val (headsTeam, tailsTeam) = if (flipResult == "Heads") {
                                Pair(team1Name, team2Name)
                            } else {
                                Pair(team2Name, team1Name)
                            }

                            Text(
                                text = flipResult ?: "",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            Color(0xFF4CAF50).copy(alpha = 0.2f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Heads",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = headsTeam,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            Color(0xFF2196F3).copy(alpha = 0.2f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Tails",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = tailsTeam,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Handle coin flip animation
        LaunchedEffect(isFlipping) {
            if (isFlipping) {
                repeat(20) {
                    coinRotation += 18f
                    delay(30)
                }

                val result = if ((Math.random() * 100).toInt() % 2 == 0) "Heads" else "Tails"
                flipResult = result
                isFlipping = false
            }
        }
    }
}
