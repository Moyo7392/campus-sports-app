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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.uta.campussports.data.SportsEvent
import edu.uta.campussports.viewmodel.ActionState
import edu.uta.campussports.viewmodel.EventsViewModel
import edu.uta.campussports.auth.FirebaseAuthViewModel
import edu.uta.campussports.data.UserProfile
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealEventsScreen(
    eventsViewModel: EventsViewModel = viewModel(),
    authViewModel: FirebaseAuthViewModel = viewModel()
) {
    val events by eventsViewModel.events.collectAsStateWithLifecycle()
    val isLoading by eventsViewModel.isLoading.collectAsStateWithLifecycle()
    val actionState by eventsViewModel.actionState.collectAsStateWithLifecycle()
    val userProfile by authViewModel.userProfile.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf("All") }
    var selectedEventDetail by remember { mutableStateOf<SportsEvent?>(null) }
    
    // Show action messages
    LaunchedEffect(actionState) {
        if (actionState is ActionState.Success || actionState is ActionState.Error) {
            // Auto-clear after 3 seconds
            kotlinx.coroutines.delay(3000)
            eventsViewModel.clearActionState()
        }
    }
    
    if (selectedEventDetail != null) {
        // Show detailed event view
        EventDetailScreen(
            event = selectedEventDetail!!,
            currentUser = currentUser,
            eventsViewModel = eventsViewModel,
            onBack = { selectedEventDetail = null }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with filter buttons
            Text(
                text = "Sports Events",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filter buttons - Horizontally scrollable
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val filters = listOf("All", "Basketball", "Soccer", "Volleyball", "Tennis", "Swimming")
                        items(filters) { filter ->
                            FilterChip(
                                onClick = { selectedFilter = filter },
                                label = { Text(filter) },
                                selected = selectedFilter == filter
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Action state messages
                item {
                    when (actionState) {
                        is ActionState.Error -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = (actionState as ActionState.Error).message,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        is ActionState.Success -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = (actionState as ActionState.Success).message,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        else -> {}
                    }
                }

                // Loading indicator
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF0064A4))
                        }
                    }
                }

                // Events I Joined Section
                val joinedEvents = events.filter { event ->
                    currentUser?.uid != null && event.currentParticipants.contains(currentUser?.uid) && event.isActive
                }

                if (joinedEvents.isNotEmpty()) {
                    item {
                        Text(
                            text = "Events I Joined",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(joinedEvents) { event ->
                        RealEventCard(
                            event = event,
                            currentUserId = currentUser?.uid,
                            events = events,
                            onJoinEvent = { eventsViewModel.joinEvent(event.id, currentUser?.uid ?: "") },
                            onLeaveEvent = { eventsViewModel.leaveEvent(event.id, currentUser?.uid ?: "") },
                            onViewDetails = { selectedEventDetail = event }
                        )
                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Browse More Events",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                // Events list - only show active events
                val filteredEvents = if (selectedFilter == "All") events.filter { it.isActive }
                else events.filter { it.sport == selectedFilter && it.isActive }

                if (filteredEvents.isEmpty() && !isLoading) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (selectedFilter == "All") "No events yet" else "No $selectedFilter events",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "Be the first to create one!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                } else {
                    items(filteredEvents) { event ->
                        RealEventCard(
                            event = event,
                            currentUserId = currentUser?.uid,
                            events = events,
                            onJoinEvent = { eventsViewModel.joinEvent(event.id, currentUser?.uid ?: "") },
                            onLeaveEvent = { eventsViewModel.leaveEvent(event.id, currentUser?.uid ?: "") },
                            onViewDetails = { selectedEventDetail = event }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RealEventCard(
    event: SportsEvent,
    currentUserId: String?,
    events: List<SportsEvent>,
    onJoinEvent: () -> Unit,
    onLeaveEvent: () -> Unit,
    onViewDetails: () -> Unit
) {
    // Get the current event state from the list (for real-time updates)
    val currentEvent = events.find { it.id == event.id } ?: event
    val isUserInEvent = currentEvent.currentParticipants.contains(currentUserId)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentEvent.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${currentEvent.sport} • ${currentEvent.difficulty}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF0064A4)
                    )
                }

                // Participant count with real data
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentEvent.isFull) Color(0xFFFFEBEE) else Color(0xFFE8F5E8)
                    )
                ) {
                    Text(
                        text = "${currentEvent.participantCount}/${currentEvent.maxParticipants}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.SemiBold,
                        color = if (currentEvent.isFull) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "📍 ${currentEvent.location}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "📅 ${currentEvent.date}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "🕐 ${currentEvent.time}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (currentEvent.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = currentEvent.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isUserInEvent) {
                    Button(
                        onClick = onLeaveEvent,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Leave")
                    }

                    Button(
                        onClick = onViewDetails,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0064A4))
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Details")
                    }
                } else {
                    Button(
                        onClick = onJoinEvent,
                        enabled = !currentEvent.isFull && currentUserId != null,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (currentEvent.isFull) "Full" else "Join")
                    }
                }
            }
        }
    }
}

@Composable
fun EventDetailScreen(
    event: SportsEvent,
    currentUser: com.google.firebase.auth.FirebaseUser?,
    eventsViewModel: EventsViewModel,
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
                                } else if (participant.userId == currentUser?.uid) {
                                    Text(
                                        text = "You",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
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