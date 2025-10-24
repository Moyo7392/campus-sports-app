package edu.uta.campussports

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.uta.campussports.ui.theme.CampusSportsTheme
import edu.uta.campussports.auth.FirebaseAuthViewModel
import edu.uta.campussports.auth.FirebaseAuthScreen
import edu.uta.campussports.auth.AuthState
import edu.uta.campussports.screens.RealEventsScreen
import edu.uta.campussports.screens.RealChatScreen
import edu.uta.campussports.screens.ToolsScreen
import edu.uta.campussports.viewmodel.EventsViewModel
import edu.uta.campussports.data.UserProfile
import edu.uta.campussports.data.EventSeeder
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CampusSportsTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    CampusSportsApp()
                }
            }
        }
    }
}

@Composable
fun CampusSportsApp() {
    val authViewModel: FirebaseAuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    
    when (authState) {
        is AuthState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "Campus Sports",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        is AuthState.Authenticated -> {
            HomeScaffold(
                onSignOut = { authViewModel.signOut() }
            )
        }
        else -> {
            FirebaseAuthScreen(
                viewModel = authViewModel,
                onNavigateToMain = { }
            )
        }
    }
}

private enum class Tab { EVENTS, CREATE, CHAT, TOOLS, PROFILE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScaffold(onSignOut: () -> Unit) {
    var selectedTab by remember { mutableStateOf(Tab.EVENTS) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    // Seed initial events once
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val seeder = EventSeeder()
            seeder.seedInitialEvents()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        when (selectedTab) {
                            Tab.EVENTS -> "Events"
                            Tab.CREATE -> "Create Event"
                            Tab.CHAT -> "Chat"
                            Tab.TOOLS -> "Tools"
                            Tab.PROFILE -> "Profile"
                        }
                    )
                },
                actions = {
                    IconButton(onClick = { showSignOutDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == Tab.EVENTS,
                    onClick = { selectedTab = Tab.EVENTS },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Events") },
                    label = { Text("Events") }
                )
                NavigationBarItem(
                    selected = selectedTab == Tab.CREATE,
                    onClick = { selectedTab = Tab.CREATE },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "Create") },
                    label = { Text("Create") }
                )
                NavigationBarItem(
                    selected = selectedTab == Tab.CHAT,
                    onClick = { selectedTab = Tab.CHAT },
                    icon = { Icon(Icons.Default.Email, contentDescription = "Chat") },
                    label = { Text("Chat") }
                )
                NavigationBarItem(
                    selected = selectedTab == Tab.TOOLS,
                    onClick = { selectedTab = Tab.TOOLS },
                    icon = { Icon(Icons.Default.Build, contentDescription = "Tools") },
                    label = { Text("Tools") }
                )
                NavigationBarItem(
                    selected = selectedTab == Tab.PROFILE,
                    onClick = { selectedTab = Tab.PROFILE },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                Tab.EVENTS -> RealEventsScreen()
                Tab.CREATE -> CreateEventScreen()
                Tab.CHAT -> RealChatScreen()
                Tab.TOOLS -> ToolsScreen()
                Tab.PROFILE -> ProfileScreen()
            }
        }
    }
    
    // Sign out confirmation dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSignOut()
                        showSignOutDialog = false
                    }
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

data class Event(
    val id: String,
    val sport: String,
    val place: String,
    val time: String,
    val players: Int,
    val maxPlayers: Int,
    val category: String,
    val icon: ImageVector,
    val color: Color
)

private fun sampleEvents(): List<Event> = listOf(
    Event("1", "Basketball 3v3", "MAC Basketball Court 2", "Sat 5:00 PM", 4, 6, "Indoor", Icons.Default.Star, Color(0xFFFF6B35)),
    Event("2", "Soccer Pickup", "MAC Outdoor Fields", "Sun 3:30 PM", 9, 14, "Outdoor", Icons.Default.Star, Color(0xFF4CAF50)),
    Event("3", "Volleyball", "MAC Volleyball Court 1", "Today 7:00 PM", 6, 10, "Indoor", Icons.Default.Star, Color(0xFF2196F3)),
    Event("4", "Tennis Match", "MAC Tennis Courts", "Mon 4:30 PM", 2, 4, "Outdoor", Icons.Default.Star, Color(0xFFFFC107)),
    Event("5", "Swimming Laps", "MAC Aquatic Center", "Tue 6:00 AM", 8, 12, "Indoor", Icons.Default.Star, Color(0xFF00BCD4)),
    Event("6", "Badminton", "MAC Gym Court 3", "Wed 8:00 PM", 4, 8, "Indoor", Icons.Default.Star, Color(0xFF9C27B0)),
    Event("7", "Ping Pong Tournament", "MAC Activity Room", "Thu 7:30 PM", 12, 16, "Indoor", Icons.Default.Star, Color(0xFFE91E63)),
    Event("8", "Morning Run", "UTA Campus Loop", "Fri 7:00 AM", 15, 25, "Outdoor", Icons.Default.Star, Color(0xFF795548))
)

@Composable
fun EventsListScreen() {
    val events = remember { sampleEvents() }
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Basketball", "Soccer", "Volleyball", "Tennis", "Swimming", "Badminton", "Running")
    
    val filteredEvents = if (selectedFilter == "All") {
        events
    } else {
        events.filter { it.sport.contains(selectedFilter, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { filter ->
                FilterChip(
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) },
                    selected = selectedFilter == filter,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredEvents) { event ->
                ModernEventCard(event)
            }
        }
    }
}

@Composable
fun ModernEventCard(event: Event) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(event.color.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = event.icon,
                        contentDescription = null,
                        tint = event.color,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.sport,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = event.place,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                AssistChip(
                    onClick = { },
                    label = { Text(event.category) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = event.color.copy(alpha = 0.1f),
                        labelColor = event.color
                    )
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = event.time,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(minOf(event.players, 4)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                CircleShape
                            )
                            .offset(x = (-8 * it).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (event.players > 4) {
                    Text(
                        text = "+${event.players - 4}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.offset(x = (-32).dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "${event.players}/${event.maxPlayers} joined",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { event.players.toFloat() / event.maxPlayers.toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = event.color,
                trackColor = event.color.copy(alpha = 0.1f)
            )
            
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = event.color
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Details")
                }
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = event.color
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Join")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen() {
    var sport by remember { mutableStateOf("") }
    var place by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var max by remember { mutableStateOf("10") }
    var locationType by remember { mutableStateOf("Indoor") }
    var expandedSport by remember { mutableStateOf(false) }
    var expandedLocation by remember { mutableStateOf(false) }
    var expandedPlace by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val sports = listOf("Basketball", "Soccer", "Volleyball", "Tennis", "Swimming", "Badminton", "Ping Pong", "Running")
    val locationTypes = listOf("Indoor", "Outdoor")
    val indoorLocations = listOf("MAC Basketball Court 1", "MAC Basketball Court 2", "MAC Basketball Court 3", "MAC Basketball Court 4", 
                                "MAC Volleyball Court 1", "MAC Volleyball Court 2", "MAC Gym Court 1", "MAC Gym Court 2", "MAC Gym Court 3", 
                                "MAC Activity Room", "MAC Aquatic Center", "MAC Fitness Center", "MAC Dance Studio")
    val outdoorLocations = listOf("MAC Outdoor Fields", "MAC Tennis Courts", "UTA Campus Loop", "MAC Track", "MAC Intramural Fields", "UTA Quad")
    
    val locationOptions = if (locationType == "Indoor") indoorLocations else outdoorLocations

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text(
                    text = "Create New Event",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Fill in the details to create your sports event",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            item {
                ExposedDropdownMenuBox(
                    expanded = expandedSport,
                    onExpandedChange = { expandedSport = it }
                ) {
                    OutlinedTextField(
                        value = sport,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Choose Sport") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSport) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSport,
                        onDismissRequest = { expandedSport = false }
                    ) {
                        sports.forEach { sportOption ->
                            DropdownMenuItem(
                                text = { Text(sportOption) },
                                onClick = {
                                    sport = sportOption
                                    expandedSport = false
                                }
                            )
                        }
                    }
                }
            }
            
            item {
                ExposedDropdownMenuBox(
                    expanded = expandedPlace,
                    onExpandedChange = { expandedPlace = it }
                ) {
                    OutlinedTextField(
                        value = place,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Choose Location") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPlace) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPlace,
                        onDismissRequest = { expandedPlace = false }
                    ) {
                        locationOptions.forEach { locationOption ->
                            DropdownMenuItem(
                                text = { Text(locationOption) },
                                onClick = {
                                    place = locationOption
                                    expandedPlace = false
                                }
                            )
                        }
                    }
                }
            }
            
            item {
                ExposedDropdownMenuBox(
                    expanded = expandedLocation,
                    onExpandedChange = { expandedLocation = it }
                ) {
                    OutlinedTextField(
                        value = locationType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Location Type") },
                        leadingIcon = {
                            Icon(
                                imageVector = if (locationType == "Indoor") Icons.Default.Home else Icons.Default.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLocation) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedLocation,
                        onDismissRequest = { expandedLocation = false }
                    ) {
                        locationTypes.forEach { locationOption ->
                            DropdownMenuItem(
                                text = { Text(locationOption) },
                                onClick = {
                                    locationType = locationOption
                                    place = "" // Reset place when location type changes
                                    expandedLocation = false
                                }
                            )
                        }
                    }
                }
            }
            
            item {
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Date & Time") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    placeholder = { Text("e.g., Sat 5:00 PM") }
                )
            }
            
            item {
                OutlinedTextField(
                    value = max,
                    onValueChange = { max = it },
                    label = { Text("Max Players") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
            
            item {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Event Created! 🎉",
                                duration = SnackbarDuration.Short
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Create Event",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

data class ChatMessage(
    val id: String,
    val message: String,
    val isMe: Boolean,
    val time: String
)

private fun sampleMessages(): List<ChatMessage> = listOf(
    ChatMessage("1", "Hey, anyone up for basketball later?", false, "2:30 PM"),
    ChatMessage("2", "I'm in! What time?", true, "2:32 PM"),
    ChatMessage("3", "How about 5 PM at MAC Court 2?", false, "2:35 PM"),
    ChatMessage("4", "Perfect! See you at the MAC 🏀", true, "2:36 PM"),
    ChatMessage("5", "Anyone else joining? Courts 1 & 3 are free too", false, "2:45 PM"),
    ChatMessage("6", "I'll be there! Just finished at the UTA gym", true, "3:10 PM")
)

@Composable
fun ChatScreen() {
    val messages = remember { sampleMessages() }
    
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { message ->
                ChatBubble(message)
            }
            item {
                Text(
                    text = "MAC Basketball Group Chat",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
        
        FloatingActionButton(
            onClick = { },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Start new chat",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isMe) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.width(8.dp))
        }
        
        Column(
            horizontalAlignment = if (message.isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.isMe) 16.dp else 4.dp,
                    bottomEnd = if (message.isMe) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (message.isMe) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Text(
                    text = message.message,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isMe) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = message.time,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        
        if (message.isMe) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Me",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ProfileScreen() {
    val authViewModel: FirebaseAuthViewModel = viewModel()
    val userProfile by authViewModel.userProfile.collectAsStateWithLifecycle()
    
    var name by remember { mutableStateOf("Loading...") }
    var major by remember { mutableStateOf("Loading...") }
    var year by remember { mutableStateOf("Loading...") }
    var sports by remember { mutableStateOf("Loading...") }
    var skillLevel by remember { mutableStateOf("Loading...") }
    var bio by remember { mutableStateOf("Loading...") }
    var isEditing by remember { mutableStateOf(false) }
    
    // Update profile info when userProfile changes
    LaunchedEffect(userProfile) {
        userProfile?.let { profile ->
            name = profile.fullName
            major = "${profile.major} - ${profile.year}"
            year = profile.year
            sports = if (profile.favoritesSports.isNotEmpty()) {
                profile.favoritesSports.joinToString(", ")
            } else {
                "No favorite sports selected"
            }
            skillLevel = profile.skillLevel
            bio = if (profile.bio.isNotEmpty()) profile.bio else "No bio available"
        } ?: run {
            name = "Unknown User"
            major = "Unknown"
            sports = "None"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = "UTA Student",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        item {
            ProfileInfoCard(
                title = "Personal Information",
                icon = Icons.Default.Person
            ) {
                ProfileField(
                    label = "Full Name",
                    value = name,
                    onValueChange = { name = it },
                    isEditing = isEditing,
                    icon = Icons.Default.AccountCircle
                )
                
                Spacer(Modifier.height(16.dp))
                
                ProfileField(
                    label = "Major & Year",
                    value = major,
                    onValueChange = { major = it },
                    isEditing = isEditing,
                    icon = Icons.Default.Person
                )
            }
        }
        
        item {
            ProfileInfoCard(
                title = "Sports Preferences",
                icon = Icons.Default.Star
            ) {
                ProfileField(
                    label = "Preferred Sports",
                    value = sports,
                    onValueChange = { sports = it },
                    isEditing = isEditing,
                    icon = Icons.Default.Star
                )
            }
        }
        
        item {
            ProfileInfoCard(
                title = "Statistics",
                icon = Icons.Default.Settings
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatCard("12", "Events Joined")
                    StatCard("8", "Events Created")
                    StatCard("4.9", "Rating")
                }
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isEditing) {
                    OutlinedButton(
                        onClick = { isEditing = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = { isEditing = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Save Changes")
                    }
                } else {
                    Button(
                        onClick = { isEditing = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Edit Profile")
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileInfoCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEditing: Boolean,
    icon: ImageVector
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(8.dp))
        
        if (isEditing) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun StatCard(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}