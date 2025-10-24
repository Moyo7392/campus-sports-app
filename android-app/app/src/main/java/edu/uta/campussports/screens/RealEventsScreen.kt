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
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    
    // Show action messages
    LaunchedEffect(actionState) {
        if (actionState is ActionState.Success || actionState is ActionState.Error) {
            // Auto-clear after 3 seconds
            kotlinx.coroutines.delay(3000)
            eventsViewModel.clearActionState()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with filter buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sports Events",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Button(
                onClick = { showCreateDialog = true },
                modifier = Modifier.height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Event")
            }
        }
        
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
            
            // Events list
            val filteredEvents = if (selectedFilter == "All") events 
                                else events.filter { it.sport == selectedFilter }
            
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
                        onJoinEvent = { eventsViewModel.joinEvent(event.id, currentUser?.uid ?: "") },
                        onLeaveEvent = { eventsViewModel.leaveEvent(event.id, currentUser?.uid ?: "") },
                        isUserInEvent = eventsViewModel.isUserInEvent(event.id, currentUser?.uid ?: "")
                    )
                }
            }
        }
    }
    
    // Create Event Dialog
    if (showCreateDialog) {
        CreateEventDialog(
            onDismiss = { showCreateDialog = false },
            onCreateEvent = { title, sport, location, date, time, maxParticipants, difficulty, description ->
                eventsViewModel.createEvent(
                    title = title,
                    sport = sport,
                    location = location,
                    date = date,
                    time = time,
                    maxParticipants = maxParticipants,
                    difficulty = difficulty,
                    description = description,
                    createdBy = currentUser?.uid ?: ""
                )
                showCreateDialog = false
            },
            userProfile = userProfile
        )
    }
}

@Composable
fun RealEventCard(
    event: SportsEvent,
    currentUserId: String?,
    onJoinEvent: () -> Unit,
    onLeaveEvent: () -> Unit,
    isUserInEvent: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                        text = event.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${event.sport} • ${event.difficulty}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF0064A4)
                    )
                }
                
                // Participant count with real data
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (event.isFull) Color(0xFFFFEBEE) else Color(0xFFE8F5E8)
                    )
                ) {
                    Text(
                        text = "${event.participantCount}/${event.maxParticipants}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.SemiBold,
                        color = if (event.isFull) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Details
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "📍 ${event.location}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "📅 ${event.date}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "🕐 ${event.time}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            if (event.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = event.description,
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
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Leave")
                    }
                } else {
                    Button(
                        onClick = onJoinEvent,
                        enabled = !event.isFull && currentUserId != null,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (event.isFull) "Full" else "Join")
                    }
                }
                
                Button(
                    onClick = { /* Navigate to chat */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chat")
                }
            }
        }
    }
}