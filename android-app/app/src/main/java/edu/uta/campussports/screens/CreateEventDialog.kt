package edu.uta.campussports.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import edu.uta.campussports.data.AcademicInfo
import edu.uta.campussports.data.Sports
import edu.uta.campussports.data.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventDialog(
    onDismiss: () -> Unit,
    onCreateEvent: (String, String, String, String, String, Int, String, String) -> Unit,
    userProfile: UserProfile?
) {
    var title by remember { mutableStateOf("") }
    var selectedSport by remember { mutableStateOf(userProfile?.favoritesSports?.firstOrNull() ?: "Basketball") }
    var selectedLocation by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var maxParticipants by remember { mutableStateOf(6) }
    var difficulty by remember { mutableStateOf(userProfile?.skillLevel ?: "Beginner") }
    var description by remember { mutableStateOf("") }

    var sportExpanded by remember { mutableStateOf(false) }
    var locationExpanded by remember { mutableStateOf(false) }
    var difficultyExpanded by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf("") }

    // Set default location when sport changes
    LaunchedEffect(selectedSport) {
        selectedLocation = Sports.UTA_LOCATIONS[selectedSport]?.firstOrNull() ?: ""
    }

    // Data validation function
    fun validateForm(): Boolean {
        validationError = when {
            title.isBlank() -> "Event title is required"
            selectedSport.isBlank() -> "Sport is required"
            selectedLocation.isBlank() -> "Location is required"
            date.isBlank() -> "Date is required"
            time.isBlank() -> "Time is required"
            maxParticipants < 2 -> "Minimum 2 participants required"
            maxParticipants > 20 -> "Maximum 20 participants allowed"
            else -> ""
        }
        return validationError.isEmpty()
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    text = "Create Sports Event",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                // Event Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title") },
                    placeholder = { Text("Friendly Basketball Game") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Sport Dropdown
                ExposedDropdownMenuBox(
                    expanded = sportExpanded,
                    onExpandedChange = { sportExpanded = !sportExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedSport,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sport") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sportExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = sportExpanded,
                        onDismissRequest = { sportExpanded = false }
                    ) {
                        Sports.ALL_SPORTS.forEach { sport ->
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
                
                // Location Dropdown
                ExposedDropdownMenuBox(
                    expanded = locationExpanded,
                    onExpandedChange = { locationExpanded = !locationExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedLocation,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Location") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = locationExpanded,
                        onDismissRequest = { locationExpanded = false }
                    ) {
                        (Sports.UTA_LOCATIONS[selectedSport] ?: emptyList()).forEach { location ->
                            DropdownMenuItem(
                                text = { Text(location) },
                                onClick = {
                                    selectedLocation = location
                                    locationExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Date (with Calendar Picker) and Time
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Date Picker
                    Card(
                        modifier = Modifier
                            .weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    ) {
                        DatePickerField(
                            selectedDate = date,
                            onDateSelected = { date = it },
                            label = "Date",
                            placeholder = "Select a date"
                        )
                    }

                    // Time Input
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time") },
                        placeholder = { Text("6:00 PM") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                
                // Max Participants
                Column {
                    Text("Max Participants", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = { if (maxParticipants > 2) maxParticipants-- }) {
                            Icon(Icons.Default.Close, contentDescription = "Decrease")
                        }
                        Text(
                            text = maxParticipants.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        IconButton(onClick = { if (maxParticipants < 20) maxParticipants++ }) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }
                
                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("Looking for teammates for a fun game!") },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    maxLines = 3
                )
                
                // Validation Error Display
                if (validationError.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color(0xFFC62828)
                            )
                            Text(
                                text = validationError,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFC62828)
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (validateForm()) {
                                onCreateEvent(
                                    title, selectedSport, selectedLocation, date, time,
                                    maxParticipants, difficulty, description
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Create Event")
                    }
                }
            }
        }
    }
}