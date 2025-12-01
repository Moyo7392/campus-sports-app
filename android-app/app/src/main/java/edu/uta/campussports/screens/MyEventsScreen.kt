package edu.uta.campussports.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.uta.campussports.data.SportsEvent
import edu.uta.campussports.viewmodel.EventsViewModel
import edu.uta.campussports.auth.FirebaseAuthViewModel
import kotlinx.coroutines.tasks.await

@Composable
fun MyEventsScreen(
    eventsViewModel: EventsViewModel = viewModel(),
    authViewModel: FirebaseAuthViewModel = viewModel()
) {
    val events by eventsViewModel.events.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    // Filter events where current user is a participant (not creator)
    val joinedEvents = events.filter { event ->
        currentUser?.uid != null && event.currentParticipants.contains(currentUser?.uid) && event.createdBy != currentUser?.uid
    }

    // Filter events created by current user (only active events)
    val createdEvents = events.filter { event ->
        currentUser?.uid != null && event.createdBy == currentUser?.uid && event.isActive
    }

    if (createdEvents.isEmpty() && joinedEvents.isEmpty()) {
        // Empty state
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "No Events Yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Join events from the Events tab to see them here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
        // List of events
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Created Events Section
            if (createdEvents.isNotEmpty()) {
                item {
                    Text(
                        text = "Events You Created",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                }

                items(createdEvents) { event ->
                    MyCreatedEventCard(event = event, eventsViewModel = eventsViewModel)
                }

                item {
                    Spacer(Modifier.height(12.dp))
                }
            }

            // Joined Events Section
            if (joinedEvents.isNotEmpty()) {
                item {
                    Text(
                        text = "Events You Joined",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                }

                items(joinedEvents) { event ->
                    MyEventCard(event = event)
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun MyCreatedEventCard(event: SportsEvent, eventsViewModel: EventsViewModel) {
    var showManagementDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Event Title, Sport, and Management Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AssistChip(
                            onClick = { },
                            label = { Text(event.sport) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f),
                                labelColor = Color(0xFF4CAF50)
                            )
                        )

                        AssistChip(
                            onClick = { },
                            label = { Text(event.difficulty) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        if (event.isActive) {
                            AssistChip(
                                onClick = { },
                                label = { Text("Active", fontSize = androidx.compose.material3.MaterialTheme.typography.labelSmall.fontSize) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.3f),
                                    labelColor = Color(0xFF4CAF50)
                                )
                            )
                        } else {
                            AssistChip(
                                onClick = { },
                                label = { Text("Closed") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                                    labelColor = MaterialTheme.colorScheme.error
                                )
                            )
                        }
                    }
                }

                // Manage Button
                if (event.isActive) {
                    IconButton(onClick = { showManagementDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Manage event",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Event Details
            EventDetailRow(
                icon = Icons.Default.LocationOn,
                label = event.location
            )

            Spacer(Modifier.height(8.dp))

            EventDetailRow(
                icon = Icons.Default.DateRange,
                label = "${event.date} • ${event.time}"
            )

            Spacer(Modifier.height(12.dp))

            // Participants Info with editable max
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "${event.currentParticipants.size}/${event.maxParticipants} participants",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (event.spotsRemaining > 0) {
                    Text(
                        text = "${event.spotsRemaining} spots left",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Full",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Description
            if (event.description.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))

                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Management Dialog
    if (showManagementDialog) {
        EventManagementDialog(
            event = event,
            eventsViewModel = eventsViewModel,
            onDismiss = { showManagementDialog = false }
        )
    }
}

@Composable
fun MyEventCard(event: SportsEvent) {
    var showDetails by remember { mutableStateOf(false) }

    if (showDetails) {
        MyEventDetailsScreen(event = event, onBack = { showDetails = false })
    } else {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDetails = true },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
            // Event Title and Sport
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AssistChip(
                            onClick = { },
                            label = { Text(event.sport) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f),
                                labelColor = Color(0xFF4CAF50)
                            )
                        )

                        AssistChip(
                            onClick = { },
                            label = { Text(event.difficulty) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Event Details
            EventDetailRow(
                icon = Icons.Default.LocationOn,
                label = event.location
            )

            Spacer(Modifier.height(8.dp))

            EventDetailRow(
                icon = Icons.Default.DateRange,
                label = "${event.date} • ${event.time}"
            )

            Spacer(Modifier.height(12.dp))

            // Participants Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "${event.currentParticipants.size}/${event.maxParticipants} participants",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (event.spotsRemaining > 0) {
                    Text(
                        text = "${event.spotsRemaining} spots left",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Full",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Description
            if (event.description.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))

                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        }
    }
}

@Composable
fun EventDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EventManagementDialog(
    event: SportsEvent,
    eventsViewModel: EventsViewModel,
    onDismiss: () -> Unit
) {
    var selectedAction by remember { mutableStateOf<String?>(null) }

    when (selectedAction) {
        "remove_participant" -> {
            RemoveParticipantDialog(
                event = event,
                eventsViewModel = eventsViewModel,
                onDismiss = { selectedAction = null }
            )
        }
        "increase_spots" -> {
            IncreaseSpotDialog(
                event = event,
                eventsViewModel = eventsViewModel,
                onDismiss = { selectedAction = null }
            )
        }
        "close_event" -> {
            CloseEventManagementDialog(
                event = event,
                eventsViewModel = eventsViewModel,
                onDismiss = { selectedAction = null }
            )
        }
        "terminate_event" -> {
            TerminateEventDialog(
                event = event,
                eventsViewModel = eventsViewModel,
                onDismiss = { selectedAction = null }
            )
        }
        else -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Manage Event") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("What would you like to do?", modifier = Modifier.padding(bottom = 8.dp))

                        Button(
                            onClick = { selectedAction = "remove_participant" },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Remove Participant")
                        }

                        Button(
                            onClick = { selectedAction = "increase_spots" },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Increase Max Participants")
                        }

                        Button(
                            onClick = { selectedAction = "close_event" },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Close Event")
                        }

                        Button(
                            onClick = { selectedAction = "terminate_event" },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Terminate Event")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun RemoveParticipantDialog(
    event: SportsEvent,
    eventsViewModel: EventsViewModel,
    onDismiss: () -> Unit
) {
    var selectedUserId by remember { mutableStateOf<String?>(null) }
    var reason by remember { mutableStateOf("") }

    if (selectedUserId != null) {
        AlertDialog(
            onDismissRequest = { selectedUserId = null },
            title = { Text("Remove Participant") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Why are you removing this participant?")
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        placeholder = { Text("Enter reason...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Get the participant name from participants list
                        val participantName = event.participants.find { it.userId == selectedUserId }?.userName
                            ?: event.currentParticipants.find { it == selectedUserId }?.take(10)
                            ?: selectedUserId!!
                        eventsViewModel.kickParticipant(event.id, selectedUserId!!, participantName, reason)
                        selectedUserId = null
                        onDismiss()
                    },
                    enabled = reason.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedUserId = null }) {
                    Text("Cancel")
                }
            }
        )
    } else {
        // Load participant names
        var participantsList by remember { mutableStateOf<List<edu.uta.campussports.data.ParticipantInfo>>(emptyList()) }

        LaunchedEffect(event.id) {
            if (participantsList.isEmpty() && event.currentParticipants.isNotEmpty()) {
                val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val loadedParticipants = mutableListOf<edu.uta.campussports.data.ParticipantInfo>()

                for (userId in event.currentParticipants) {
                    if (userId != event.createdBy) {
                        try {
                            val userDoc = firestore.collection("users").document(userId).get().await()
                            if (userDoc.exists()) {
                                val userProfile = userDoc.toObject(edu.uta.campussports.data.UserProfile::class.java)
                                if (userProfile != null && userProfile.fullName.isNotEmpty()) {
                                    loadedParticipants.add(
                                        edu.uta.campussports.data.ParticipantInfo(
                                            userId = userId,
                                            userName = userProfile.fullName
                                        )
                                    )
                                } else {
                                    loadedParticipants.add(
                                        edu.uta.campussports.data.ParticipantInfo(
                                            userId = userId,
                                            userName = userId
                                        )
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            loadedParticipants.add(
                                edu.uta.campussports.data.ParticipantInfo(
                                    userId = userId,
                                    userName = userId
                                )
                            )
                        }
                    }
                }
                participantsList = loadedParticipants
            }
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select Participant to Remove") },
            text = {
                if (participantsList.isEmpty()) {
                    Text("Loading participants...", modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(participantsList) { participant ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                TextButton(
                                    onClick = { selectedUserId = participant.userId },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(Color(0xFF4CAF50), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = participant.userName.take(1).uppercase(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                        Text(
                                            text = participant.userName,
                                            modifier = Modifier.weight(1f),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun IncreaseSpotDialog(
    event: SportsEvent,
    eventsViewModel: EventsViewModel,
    onDismiss: () -> Unit
) {
    var newMax by remember { mutableStateOf(event.maxParticipants.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Increase Max Participants") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Current max: ${event.maxParticipants}")
                Text("Current participants: ${event.currentParticipants.size}")
                OutlinedTextField(
                    value = newMax,
                    onValueChange = { if (it.all { char -> char.isDigit() }) newMax = it },
                    label = { Text("New max participants") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    newMax.toIntOrNull()?.let { max ->
                        if (max > event.currentParticipants.size) {
                            eventsViewModel.updateEventMaxParticipants(event.id, max)
                            onDismiss()
                        }
                    }
                },
                enabled = newMax.toIntOrNull()?.let { it > event.currentParticipants.size } == true
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CloseEventManagementDialog(
    event: SportsEvent,
    eventsViewModel: EventsViewModel,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Close Event") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Are you sure you want to close this event?")
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    placeholder = { Text("Enter reason for closing...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    eventsViewModel.closeEvent(event.id, reason)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Close Event")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MyEventDetailsScreen(
    event: SportsEvent,
    onBack: () -> Unit
) {
    // Load participant names from Firestore - ALWAYS load to ensure all participants are shown
    var participants by remember { mutableStateOf<List<edu.uta.campussports.data.ParticipantInfo>>(emptyList()) }

    LaunchedEffect(event.id, event.currentParticipants.size) {
        // Always load participant names from currentParticipants to ensure we get all participants
        if (event.currentParticipants.isNotEmpty()) {
            println("📝 Loading ${event.currentParticipants.size} participants from event: ${event.id}")
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val loadedParticipants = mutableListOf<edu.uta.campussports.data.ParticipantInfo>()

            for (userId in event.currentParticipants) {
                try {
                    println("🔍 Fetching profile for userId: $userId")
                    val userDoc = firestore.collection("users").document(userId).get().await()
                    if (userDoc.exists()) {
                        val userProfile = userDoc.toObject(edu.uta.campussports.data.UserProfile::class.java)
                        if (userProfile != null && userProfile.fullName.isNotEmpty()) {
                            println("✅ Loaded name: ${userProfile.fullName} for userId: $userId")
                            loadedParticipants.add(
                                edu.uta.campussports.data.ParticipantInfo(
                                    userId = userId,
                                    userName = userProfile.fullName
                                )
                            )
                        } else {
                            println("⚠️ No fullName found for userId: $userId, using ID as fallback")
                            loadedParticipants.add(
                                edu.uta.campussports.data.ParticipantInfo(
                                    userId = userId,
                                    userName = userId
                                )
                            )
                        }
                    } else {
                        println("❌ User document not found for userId: $userId")
                        loadedParticipants.add(
                            edu.uta.campussports.data.ParticipantInfo(
                                userId = userId,
                                userName = userId
                            )
                        )
                    }
                } catch (e: Exception) {
                    println("❌ Error loading participant name for $userId: ${e.message}")
                    loadedParticipants.add(
                        edu.uta.campussports.data.ParticipantInfo(
                            userId = userId,
                            userName = userId
                        )
                    )
                }
            }
            println("✅ Loaded all ${loadedParticipants.size} participants successfully")
            participants = loadedParticipants
        } else {
            println("⚠️ No participants in event")
            participants = emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Event Details",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Event Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AssistChip(
                                onClick = { },
                                label = { Text(event.sport) }
                            )
                            AssistChip(
                                onClick = { },
                                label = { Text(event.difficulty) }
                            )
                        }

                        Text(
                            text = event.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("📍 Location", style = MaterialTheme.typography.labelSmall)
                                Text(event.location, style = MaterialTheme.typography.bodySmall)
                            }
                            Column {
                                Text("📅 Date", style = MaterialTheme.typography.labelSmall)
                                Text(event.date, style = MaterialTheme.typography.bodySmall)
                            }
                            Column {
                                Text("🕐 Time", style = MaterialTheme.typography.labelSmall)
                                Text(event.time, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // Participants Section
            item {
                Text(
                    text = "Participants (${event.participantCount}/${event.maxParticipants})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (participants.isEmpty()) {
                item {
                    Text(
                        text = "No participants yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(participants) { participant ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (participant.userId == event.createdBy) Color(0xFFFFF9C4) else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (participant.userId == event.createdBy) Color(0xFFFF9800) else Color(0xFF4CAF50),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = participant.userName.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = participant.userName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (participant.userId == event.createdBy) {
                                    Text(
                                        text = "Event Organizer",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFFF9800),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun TerminateEventDialog(
    event: SportsEvent,
    eventsViewModel: EventsViewModel,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Terminate Event") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Are you sure you want to terminate this event? This action cannot be undone.")
                Text(
                    text = "All participants will be notified that the event has been terminated.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    placeholder = { Text("Enter reason for terminating...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4,
                    label = { Text("Reason (optional)") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    eventsViewModel.terminateEvent(event.id, reason)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Terminate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
