package edu.uta.campussports

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import edu.uta.campussports.screens.MyEventsScreen
import edu.uta.campussports.screens.ToolsScreen
import edu.uta.campussports.screens.DatePickerField
import edu.uta.campussports.screens.TimePickerField
import edu.uta.campussports.viewmodel.EventsViewModel
import edu.uta.campussports.viewmodel.ActionState
import edu.uta.campussports.data.UserProfile
import edu.uta.campussports.data.EventSeeder

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

private enum class Tab { EVENTS, CREATE, MY_EVENTS, TOOLS, PROFILE }

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
                            Tab.MY_EVENTS -> "My Events"
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
                    selected = selectedTab == Tab.MY_EVENTS,
                    onClick = { selectedTab = Tab.MY_EVENTS },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "My Events") },
                    label = { Text("My Events") }
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
                Tab.MY_EVENTS -> MyEventsScreen()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen() {
    val eventsViewModel: EventsViewModel = viewModel()
    val authViewModel: FirebaseAuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val actionState by eventsViewModel.actionState.collectAsStateWithLifecycle()

    LaunchedEffect(actionState) {
        if (actionState is ActionState.Success || actionState is ActionState.Error) {
            kotlinx.coroutines.delay(2000) // show for 2 seconds
            eventsViewModel.clearActionState()
        }
    }

    var title by remember { mutableStateOf("") }
    var sport by remember { mutableStateOf("Basketball") }
    var place by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var max by remember { mutableStateOf("6") }
    var description by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf("") }

    var expandedSport by remember { mutableStateOf(false) }
    var expandedPlace by remember { mutableStateOf(false) }

    val sports = listOf("Basketball", "Soccer", "Volleyball", "Tennis", "Swimming", "Badminton", "Ping Pong", "Running")
    val allLocations = listOf(
        "MAC Basketball Court 1", "MAC Basketball Court 2", "MAC Basketball Court 3", "MAC Basketball Court 4",
        "MAC Volleyball Court 1", "MAC Volleyball Court 2", "MAC Gym Court 1", "MAC Gym Court 2", "MAC Gym Court 3",
        "MAC Activity Room", "MAC Aquatic Center", "MAC Fitness Center", "MAC Dance Studio",
        "MAC Outdoor Fields", "MAC Tennis Courts", "UTA Campus Loop", "MAC Track", "MAC Intramural Fields", "UTA Quad"
    )

    // Data validation function
    fun validateForm(): Boolean {
        validationError = when {
            title.isBlank() -> "Event title is required"
            sport.isBlank() -> "Sport is required"
            place.isBlank() -> "Location is required"
            date.isBlank() -> "Date is required"
            time.isBlank() -> "Time is required"
            max.toIntOrNull() == null -> "Max participants must be a number"
            max.toInt() < 2 -> "Minimum 2 participants required"
            max.toInt() > 20 -> "Maximum 20 participants allowed"
            else -> ""
        }
        return validationError.isEmpty()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Create New Event",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Fill in the details to create your sports event",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Event Title
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title") },
                    placeholder = { Text("e.g., Friendly Basketball Game") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Sport Dropdown
            item {
                ExposedDropdownMenuBox(
                    expanded = expandedSport,
                    onExpandedChange = { expandedSport = !expandedSport }
                ) {
                    OutlinedTextField(
                        value = sport,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sport") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSport) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
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

            // Location Dropdown
            item {
                ExposedDropdownMenuBox(
                    expanded = expandedPlace,
                    onExpandedChange = { expandedPlace = !expandedPlace }
                ) {
                    OutlinedTextField(
                        value = place,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Location") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPlace) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPlace,
                        onDismissRequest = { expandedPlace = false }
                    ) {
                        allLocations.forEach { locationOption ->
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

            // Date (with Calendar Picker) and Time (Separate fields)
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Date Picker
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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

                    // Time Picker Dropdown
                    TimePickerField(
                        selectedTime = time,
                        onTimeSelected = { time = it },
                        label = "Time",
                        placeholder = "Select a time"
                    )
                }
            }

            // Max Participants
            item {
                OutlinedTextField(
                    value = max,
                    onValueChange = { max = it },
                    label = { Text("Max Participants") },
                    placeholder = { Text("6") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Description
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("Tell players about your event...") },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    maxLines = 3
                )
            }

            // Validation Error Display
            if (validationError.isNotEmpty()) {
                item {
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
            }

            // Create Button
            item {
                Button(
                    onClick = {
                        if (validateForm()) {
                            eventsViewModel.createEvent(
                                title = title,
                                sport = sport,
                                location = place,
                                date = date,
                                time = time,
                                maxParticipants = max.toInt(),
                                difficulty = "Beginner",  // Default difficulty
                                description = description,
                                createdBy = currentUser?.uid ?: ""
                            )
                            // Reset form after successful submission
                            title = ""
                            sport = "Basketball"
                            place = ""
                            date = ""
                            time = ""
                            max = "6"
                            description = ""
                            validationError = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Create Event", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }

            // Status Message
            if (actionState is ActionState.Success) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
                    ) {
                        Text(
                            text = (actionState as ActionState.Success).message,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else if (actionState is ActionState.Error) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = (actionState as ActionState.Error).message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
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
            // Profile Setup Guidance Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Complete Your Profile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "Tap 'Edit Profile' to complete and update your information. Your profile helps other students find compatible teammates!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        lineHeight = androidx.compose.ui.unit.TextUnit(value = 1.5f, type = androidx.compose.ui.unit.TextUnitType.Sp)
                    )
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
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { isEditing = false },
                        modifier = Modifier.weight(1f).height(50.dp),
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
                        modifier = Modifier.fillMaxWidth().height(50.dp),
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