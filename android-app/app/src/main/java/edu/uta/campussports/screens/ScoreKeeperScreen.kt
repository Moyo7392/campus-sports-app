package edu.uta.campussports.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Sport-specific configurations
data class SportConfig(
    val name: String,
    val points: IntArray = intArrayOf(1, 2, 3),
    val pointLabels: List<String> = listOf("+1", "+2", "+3"),
    val description: String = ""
)

val SPORTS_CONFIG = mapOf(
    "Basketball" to SportConfig(
        name = "Basketball",
        points = intArrayOf(1, 2, 3),
        pointLabels = listOf("+1 Free Throw", "+2 Points", "+3 Pointer"),
        description = "Track free throws, 2-pointers, and 3-pointers"
    ),
    "Soccer" to SportConfig(
        name = "Soccer",
        points = intArrayOf(1),
        pointLabels = listOf("+1 Goal"),
        description = "Track goals"
    ),
    "Volleyball" to SportConfig(
        name = "Volleyball",
        points = intArrayOf(1),
        pointLabels = listOf("+1 Point"),
        description = "Track points (first to 25)"
    ),
    "Tennis" to SportConfig(
        name = "Tennis",
        points = intArrayOf(1),
        pointLabels = listOf("+1 Game"),
        description = "Track games won"
    ),
    "Badminton" to SportConfig(
        name = "Badminton",
        points = intArrayOf(1),
        pointLabels = listOf("+1 Point"),
        description = "Track points (first to 21)"
    ),
    "Ping Pong" to SportConfig(
        name = "Ping Pong",
        points = intArrayOf(1),
        pointLabels = listOf("+1 Point"),
        description = "Track points (first to 11)"
    ),
    "Running" to SportConfig(
        name = "Running",
        points = intArrayOf(1),
        pointLabels = listOf("+1 Lap"),
        description = "Track laps completed"
    ),
    "Swimming" to SportConfig(
        name = "Swimming",
        points = intArrayOf(1),
        pointLabels = listOf("+1 Lap"),
        description = "Track laps completed"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreKeeperScreen() {
    var team1Name by remember { mutableStateOf("") }
    var team2Name by remember { mutableStateOf("") }
    var selectedSport by remember { mutableStateOf("Basketball") }
    var sportExpanded by remember { mutableStateOf(false) }
    var showScoreboard by remember { mutableStateOf(false) }
    var team1Score by remember { mutableStateOf(0) }
    var team2Score by remember { mutableStateOf(0) }

    val sports = listOf("Basketball", "Soccer", "Volleyball", "Tennis", "Badminton", "Ping Pong", "Running", "Swimming")
    val sportConfig = SPORTS_CONFIG[selectedSport] ?: SPORTS_CONFIG["Basketball"]!!

    if (!showScoreboard) {
        // Setup screen
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text(
                    text = "Score Keeper",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Enter team names and sport to start tracking scores",
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
                ExposedDropdownMenuBox(
                    expanded = sportExpanded,
                    onExpandedChange = { sportExpanded = !sportExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedSport,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Sport") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sportExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = sportExpanded,
                        onDismissRequest = { sportExpanded = false }
                    ) {
                        sports.forEach { sport ->
                            DropdownMenuItem(
                                text = { Text(sport) },
                                onClick = {
                                    selectedSport = sport
                                    sportExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "How to track",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = sportConfig.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (team1Name.isNotBlank() && team2Name.isNotBlank()) {
                            showScoreboard = true
                            team1Score = 0
                            team2Score = 0
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
                        text = "Start Scoring",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    } else {
        // Scoreboard interface
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedSport,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = {
                            showScoreboard = false
                            team1Name = ""
                            team2Name = ""
                            team1Score = 0
                            team2Score = 0
                        },
                        modifier = Modifier.height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("← Back")
                    }
                }
            }

            // Team 1 Score Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(
                        2.dp,
                        Color(0xFF4CAF50)
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
                            text = team1Name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = team1Score.toString(),
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 80.sp,
                            color = Color(0xFF4CAF50)
                        )

                        // Sport-specific buttons
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Undo button
                            if (team1Score > 0) {
                                Button(
                                    onClick = { team1Score-- },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF757575)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Undo")
                                    Spacer(Modifier.width(8.dp))
                                    Text("Undo", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            // Sport-specific point buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                sportConfig.pointLabels.forEachIndexed { index, label ->
                                    Button(
                                        onClick = { team1Score += sportConfig.points[index] },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF4CAF50)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Team 2 Score Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)
                    ),
                    border = BorderStroke(
                        2.dp,
                        Color(0xFF2196F3)
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
                            text = team2Name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = team2Score.toString(),
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 80.sp,
                            color = Color(0xFF2196F3)
                        )

                        // Sport-specific buttons
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Undo button
                            if (team2Score > 0) {
                                Button(
                                    onClick = { team2Score-- },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF757575)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Undo")
                                    Spacer(Modifier.width(8.dp))
                                    Text("Undo", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            // Sport-specific point buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                sportConfig.pointLabels.forEachIndexed { index, label ->
                                    Button(
                                        onClick = { team2Score += sportConfig.points[index] },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF2196F3)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Reset button
            item {
                Button(
                    onClick = {
                        team1Score = 0
                        team2Score = 0
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reset Scores")
                }
            }
        }
    }
}
