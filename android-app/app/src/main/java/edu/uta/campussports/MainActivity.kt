package edu.uta.campussports

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

private enum class Tab { EVENTS, CREATE, MY_EVENTS, TOOLS, ACCOUNT }

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
                            Tab.ACCOUNT -> "Account"
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
                    selected = selectedTab == Tab.ACCOUNT,
                    onClick = { selectedTab = Tab.ACCOUNT },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Account") },
                    label = { Text("Account") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                Tab.EVENTS -> RealEventsScreen()
                Tab.CREATE -> NewCreateEventScreen()
                Tab.MY_EVENTS -> MyEventsScreen()
                Tab.TOOLS -> ToolsScreen()
                Tab.ACCOUNT -> ProfileScreen()
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
fun NewCreateEventScreen() {
    val eventsViewModel: EventsViewModel = viewModel()
    val authViewModel: FirebaseAuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val actionState by eventsViewModel.actionState.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var sport by remember { mutableStateOf("Basketball") }
    var location by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var maxParticipants by remember { mutableStateOf("6") }
    var description by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf("") }
    var showSuccessMessage by remember { mutableStateOf(false) }

    val sports = listOf("Basketball", "Soccer", "Volleyball", "Tennis", "Swimming", "Badminton", "Ping Pong", "Running")
    val locations = listOf(
        "MAC Basketball Court 1", "MAC Basketball Court 2", "MAC Basketball Court 3", "MAC Basketball Court 4",
        "MAC Volleyball Court 1", "MAC Volleyball Court 2", "MAC Gym Court 1", "MAC Gym Court 2",
        "MAC Activity Room", "MAC Aquatic Center", "MAC Fitness Center", "MAC Dance Studio",
        "MAC Outdoor Fields", "MAC Tennis Courts", "UTA Campus Loop", "MAC Track", "MAC Intramural Fields", "UTA Quad"
    )

    // Auto-clear after success
    LaunchedEffect(actionState) {
        if (actionState is ActionState.Success) {
            showSuccessMessage = true
            kotlinx.coroutines.delay(3000)

            // Clear form
            title = ""
            sport = "Basketball"
            location = ""
            date = ""
            time = ""
            maxParticipants = "6"
            description = ""
            validationError = ""
            showSuccessMessage = false
            eventsViewModel.clearActionState()
        }
    }

    fun validateAndCreate() {
        // Reset error
        validationError = ""

        // Validate
        if (title.isBlank()) {
            validationError = "Event title is required"
            return
        }
        if (location.isBlank()) {
            validationError = "Location is required"
            return
        }
        if (date.isBlank()) {
            validationError = "Date is required"
            return
        }
        if (time.isBlank()) {
            validationError = "Time is required"
            return
        }

        val max = maxParticipants.toIntOrNull()
        if (max == null) {
            validationError = "Max participants must be a number"
            return
        }
        if (max < 2) {
            validationError = "Minimum 2 participants required"
            return
        }
        if (max > 20) {
            validationError = "Maximum 20 participants allowed"
            return
        }

        // All validation passed - create event
        eventsViewModel.createEvent(
            title = title,
            sport = sport,
            location = location,
            date = date,
            time = time,
            maxParticipants = max,
            difficulty = "Beginner",
            description = description,
            createdBy = currentUser?.uid ?: ""
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            text = "Create New Event",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Event Title Input
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Event Title") },
            placeholder = { Text("e.g., Basketball Game") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Sport Dropdown
        var expandedSport by remember { mutableStateOf(false) }
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
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
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

        // Location Dropdown
        var expandedLocation by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedLocation,
            onExpandedChange = { expandedLocation = !expandedLocation }
        ) {
            OutlinedTextField(
                value = location,
                onValueChange = {},
                readOnly = true,
                label = { Text("Location") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLocation) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedLocation,
                onDismissRequest = { expandedLocation = false }
            ) {
                locations.forEach { locationOption ->
                    DropdownMenuItem(
                        text = { Text(locationOption) },
                        onClick = {
                            location = locationOption
                            expandedLocation = false
                        }
                    )
                }
            }
        }

        // Date Input
        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Date (MM/DD/YYYY)") },
            placeholder = { Text("e.g., 12/25/2025") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Time Input
        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Time (HH:MM AM/PM)") },
            placeholder = { Text("e.g., 3:00 PM") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Max Participants
        OutlinedTextField(
            value = maxParticipants,
            onValueChange = { maxParticipants = it },
            label = { Text("Max Participants") },
            placeholder = { Text("6") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Description
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description (Optional)") },
            placeholder = { Text("Tell players about your event...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            maxLines = 4
        )

        // Error Display
        if (validationError.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = validationError,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Success Display
        if (showSuccessMessage) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Event created successfully!",
                        color = Color(0xFF2E7D32),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Create Button
        Button(
            onClick = { validateAndCreate() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = actionState !is ActionState.Loading && !showSuccessMessage,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            if (actionState is ActionState.Loading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Event", fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AccountScreen() {
    val authViewModel: FirebaseAuthViewModel = viewModel()
    val userProfile by authViewModel.userProfile.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var major by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var sports by remember { mutableStateOf("") }
    var skillLevel by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // Update profile info when userProfile changes
    LaunchedEffect(userProfile, currentUser) {
        println("🔍 AccountScreen - userProfile: $userProfile, currentUser: ${currentUser?.email}")

        if (currentUser != null) {
            if (userProfile != null) {
                println("✅ Profile loaded: ${userProfile!!.fullName}")
                name = userProfile!!.fullName

                major = if (userProfile!!.major.isNotBlank() && userProfile!!.year.isNotBlank()) {
                    "${userProfile!!.major} - ${userProfile!!.year}"
                } else if (userProfile!!.major.isNotBlank()) {
                    userProfile!!.major
                } else {
                    "Major not set"
                }

                year = userProfile!!.year

                sports = if (userProfile!!.favoritesSports.isNotEmpty()) {
                    userProfile!!.favoritesSports.joinToString(", ")
                } else {
                    "No favorite sports selected"
                }

                skillLevel = userProfile!!.skillLevel

                bio = if (userProfile!!.bio.isNotEmpty()) {
                    userProfile!!.bio
                } else {
                    "No bio available"
                }

                isLoading = false
            } else if (currentUser != null) {
                println("⏳ Profile still loading...")
                name = "Loading..."
                major = "Loading profile..."
                year = "Loading profile..."
                sports = "Loading profile..."
                skillLevel = "Loading profile..."
                bio = "Loading profile..."
                isLoading = true
            }
        } else {
            name = "Not Logged In"
            major = "Please sign in"
            sports = "None"
            isLoading = false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(vertical = 24.dp)
    ) {
        // Header Section
        item {
            AccountHeaderSection(
                name = name,
                isLoading = isLoading
            )
        }

        // Personal Information Card
        item {
            PersonalInfoCard(
                name = name,
                major = major,
                isEditing = isEditing,
                onNameChange = { name = it },
                onMajorChange = { major = it }
            )
        }

        // Sports Preferences Card
        item {
            SportsPreferencesCard(
                sports = sports,
                isEditing = isEditing,
                onSportsChange = { sports = it }
            )
        }

        // Edit Profile Button
        item {
            EditProfileButton(
                isEditing = isEditing,
                onEditClick = { isEditing = true },
                onCancelClick = { isEditing = false },
                onSaveClick = { isEditing = false }
            )
        }

        // Add bottom padding
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AccountHeaderSection(
    name: String,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
            if (isLoading && name.isEmpty()) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(60.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Account Picture",
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Text(
            text = if (name.isEmpty()) "Loading..." else name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "UTA Student",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PersonalInfoCard(
    name: String,
    major: String,
    isEditing: Boolean,
    onNameChange: (String) -> Unit,
    onMajorChange: (String) -> Unit
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Personal Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            AccountField(
                label = "Full Name",
                value = name,
                onValueChange = onNameChange,
                isEditing = isEditing,
                icon = Icons.Default.AccountCircle
            )

            AccountField(
                label = "Major & Year",
                value = major,
                onValueChange = onMajorChange,
                isEditing = isEditing,
                icon = Icons.Default.Person
            )
        }
    }
}

@Composable
private fun SportsPreferencesCard(
    sports: String,
    isEditing: Boolean,
    onSportsChange: (String) -> Unit
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Sports Preferences",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            AccountField(
                label = "Preferred Sports",
                value = sports,
                onValueChange = onSportsChange,
                isEditing = isEditing,
                icon = Icons.Default.Star
            )
        }
    }
}

@Composable
private fun EditProfileButton(
    isEditing: Boolean,
    onEditClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    if (isEditing) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancelClick,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
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
        }
    } else {
        Button(
            onClick = onEditClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
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

@Composable
private fun AccountField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEditing: Boolean,
    icon: ImageVector
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

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
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
fun ProfileScreen() {
    AccountScreen()
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