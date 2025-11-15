package edu.uta.campussports.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import edu.uta.campussports.auth.FirebaseAuthViewModel
import edu.uta.campussports.data.ChatMessage
import edu.uta.campussports.data.MessageType
import edu.uta.campussports.data.SportsEvent
import edu.uta.campussports.viewmodel.EventsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealChatScreen(
    eventsViewModel: EventsViewModel = viewModel(),
    authViewModel: FirebaseAuthViewModel = viewModel()
) {
    val events by eventsViewModel.events.collectAsStateWithLifecycle()
    val eventChats by eventsViewModel.eventChats.collectAsStateWithLifecycle()
    val userProfile by authViewModel.userProfile.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    
    var selectedEvent by remember { mutableStateOf<SportsEvent?>(null) }
    var messageText by remember { mutableStateOf("") }
    
    val userEvents = remember(events, currentUser) {
        events.filter { event ->
            currentUser?.uid?.let { userId ->
                event.currentParticipants.contains(userId)
            } ?: false
        }
    }
    
    LaunchedEffect(selectedEvent) {
        selectedEvent?.let { event ->
            eventsViewModel.selectEvent(event)
        }
    }
    
    if (selectedEvent == null) {
        // Event selection screen
        EventSelectionScreen(
            userEvents = userEvents,
            onEventSelected = { event -> selectedEvent = event }
        )
    } else {
        // Chat screen for selected event
        EventChatScreen(
            event = selectedEvent!!,
            messages = eventChats[selectedEvent?.id] ?: emptyList(),
            messageText = messageText,
            onMessageChange = { messageText = it },
            onSendMessage = {
                currentUser?.let { user ->
                    userProfile?.let { profile ->
                        eventsViewModel.sendMessage(
                            eventId = selectedEvent!!.id,
                            senderId = user.uid,
                            senderName = profile.fullName,
                            message = messageText
                        )
                        messageText = ""
                    }
                }
            },
            onBackPressed = { selectedEvent = null },
            currentUserId = currentUser?.uid,
            eventsViewModel = eventsViewModel,
            isCreator = selectedEvent?.createdBy == currentUser?.uid
        )
    }
}

@Composable
private fun EventSelectionScreen(
    userEvents: List<SportsEvent>,
    onEventSelected: (SportsEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Event Chats",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (userEvents.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Event Chats",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "Join some events to start chatting!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(userEvents) { event ->
                    EventChatCard(
                        event = event,
                        onClick = { onEventSelected(event) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EventChatCard(
    event: SportsEvent,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sport icon placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF4CAF50), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = event.sport.take(2).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${event.sport} • ${event.participantCount} participants",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${event.date} ${event.time}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = "Enter chat",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventChatScreen(
    event: SportsEvent,
    messages: List<ChatMessage>,
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onBackPressed: () -> Unit,
    currentUserId: String?,
    eventsViewModel: EventsViewModel = viewModel(),
    isCreator: Boolean = false
) {
    val listState = rememberLazyListState()
    var showManagementMenu by remember { mutableStateOf(false) }
    var showCloseDialog by remember { mutableStateOf(false) }
    var showKickDialog by remember { mutableStateOf(false) }
    var selectedParticipantToKick by remember { mutableStateOf<Pair<String, String>?>(null) }

    // Auto scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat header
        Surface(
            color = Color(0xFF4CAF50),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${event.sport} • ${event.participantCount} participants",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                // Creator management menu
                if (isCreator && event.isActive) {
                    Box {
                        IconButton(onClick = { showManagementMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Event options",
                                tint = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = showManagementMenu,
                            onDismissRequest = { showManagementMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Manage Participants") },
                                onClick = {
                                    showManagementMenu = false
                                    showKickDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Close Event") },
                                onClick = {
                                    showManagementMenu = false
                                    showCloseDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.Close, contentDescription = null) }
                            )
                        }
                    }
                }
            }
        }
        
        // Messages list
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No messages yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "Be the first to say hello!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    ChatMessageBubble(
                        message = message,
                        isCurrentUser = message.senderId == currentUserId
                    )
                }
            }
        }
        
        // Message input
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = onMessageChange,
                    placeholder = { Text("Type a message...") },
                    modifier = Modifier.weight(1f),
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = onSendMessage,
                    modifier = Modifier.size(48.dp),
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }

    // Close Event Dialog
    if (showCloseDialog) {
        CloseEventDialog(
            event = event,
            onDismiss = { showCloseDialog = false },
            onConfirm = { reason ->
                eventsViewModel.closeEvent(event.id, reason)
                showCloseDialog = false
            }
        )
    }

    // Kick Participant Dialog
    if (showKickDialog) {
        KickParticipantDialog(
            event = event,
            onDismiss = { showKickDialog = false },
            onSelectParticipant = { userId, userName ->
                selectedParticipantToKick = userId to userName
                showKickDialog = false
            }
        )
    }

    // Kick Reason Dialog
    if (selectedParticipantToKick != null) {
        KickReasonDialog(
            participantName = selectedParticipantToKick!!.second,
            onDismiss = { selectedParticipantToKick = null },
            onConfirm = { reason ->
                eventsViewModel.kickParticipant(
                    eventId = event.id,
                    userId = selectedParticipantToKick!!.first,
                    userName = selectedParticipantToKick!!.second,
                    reason = reason
                )
                selectedParticipantToKick = null
            }
        )
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessage,
    isCurrentUser: Boolean
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isCurrentUser) {
            // Avatar for other users
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF4CAF50), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message.senderName.take(1).uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Column(
            horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            when (message.messageType) {
                MessageType.SYSTEM_JOIN, MessageType.SYSTEM_LEAVE -> {
                    // System message
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF0F0F0)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = message.message,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                MessageType.TEXT -> {
                    // Regular message
                    if (!isCurrentUser) {
                        Text(
                            text = message.senderName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )
                    }
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrentUser) Color(0xFF4CAF50) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp,
                            bottomStart = if (isCurrentUser) 12.dp else 4.dp,
                            bottomEnd = if (isCurrentUser) 4.dp else 12.dp
                        )
                    ) {
                        Text(
                            text = message.message,
                            modifier = Modifier.padding(12.dp),
                            color = if (isCurrentUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        text = timeFormat.format(message.timestamp.toDate()),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
        
        if (isCurrentUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // Avatar for current user
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF4CAF50), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Me",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CloseEventDialog(
    event: SportsEvent,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Close Event") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Are you sure you want to close \"${event.title}\"?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Please provide a reason for closing this event:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    placeholder = { Text("E.g., Not enough participants, Weather conditions, etc.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.isNotBlank()) {
                        onConfirm(reason)
                    }
                },
                enabled = reason.isNotBlank()
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
private fun KickParticipantDialog(
    event: SportsEvent,
    onDismiss: () -> Unit,
    onSelectParticipant: (String, String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remove Participant") },
        text = {
            if (event.currentParticipants.isEmpty() || event.currentParticipants.size == 1) {
                Text("No participants to remove (only you in the event)")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(event.currentParticipants) { userId ->
                        if (userId != event.createdBy) {
                            TextButton(
                                onClick = { onSelectParticipant(userId, userId) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = userId,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                )
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

@Composable
private fun KickReasonDialog(
    participantName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remove Participant") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Why are you removing $participantName?",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    placeholder = { Text("Provide a reason...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.isNotBlank()) {
                        onConfirm(reason)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF5350)
                ),
                enabled = reason.isNotBlank()
            ) {
                Text("Remove")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}