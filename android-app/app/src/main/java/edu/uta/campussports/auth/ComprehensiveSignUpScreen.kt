package edu.uta.campussports.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.uta.campussports.data.AcademicInfo
import edu.uta.campussports.data.Sports

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprehensiveSignUpScreen(
    viewModel: FirebaseAuthViewModel,
    onNavigateBack: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var major by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var skillLevel by remember { mutableStateOf("Beginner") }
    var bio by remember { mutableStateOf("") }
    var selectedSports by remember { mutableStateOf(setOf<String>()) }

    // Password visibility toggles
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    // Dropdown states
    var majorExpanded by remember { mutableStateOf(false) }
    var yearExpanded by remember { mutableStateOf(false) }
    var skillExpanded by remember { mutableStateOf(false) }
    
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    
    // Navigate on success
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onSignUpSuccess()
        }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Text(
                text = "Create Your Profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Tell us about yourself to find the perfect teammates!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Personal Info Section
                    Text(
                        text = "Personal Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    // Full Name
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("UTA Email") },
                        placeholder = { Text("yournetid@mavs.uta.edu") },
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Default.Info else Icons.Default.Lock,
                                    contentDescription = if (showPassword) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Password strength indicator
                    val passwordStrength = remember(password) {
                        when {
                            password.isEmpty() -> "Password strength: None"
                            password.length < 8 -> "Weak (min 8 characters required)"
                            !password.any { it.isUpperCase() } || !password.any { it.isLowerCase() } ->
                                "Fair (add uppercase + lowercase)"
                            !password.any { !it.isLetterOrDigit() } ->
                                "Good (add special character like !@#$%)"
                            else -> "Strong"
                        }
                    }
                    val strengthColor = remember(password) {
                        when {
                            password.isEmpty() -> Color.Gray
                            password.length < 8 -> Color(0xFFD32F2F) // Red
                            !password.any { it.isUpperCase() } || !password.any { it.isLowerCase() } ->
                                Color(0xFFF57C00) // Orange
                            !password.any { !it.isLetterOrDigit() } ->
                                Color(0xFFFBC02D) // Yellow
                            else -> Color(0xFF4CAF50) // Green
                        }
                    }
                    Text(
                        text = passwordStrength,
                        style = MaterialTheme.typography.bodySmall,
                        color = strengthColor,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Confirm Password
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                Icon(
                                    if (showConfirmPassword) Icons.Default.Info else Icons.Default.Lock,
                                    contentDescription = if (showConfirmPassword) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Password match indicator
                    if (confirmPassword.isNotEmpty()) {
                        val matchesPassword = password == confirmPassword
                        Text(
                            text = if (matchesPassword) "✓ Passwords match" else "✗ Passwords do not match",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (matchesPassword) Color(0xFF4CAF50) else Color(0xFFD32F2F),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Academic Info Section
                    Text(
                        text = "Academic Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    // Major Dropdown
                    ExposedDropdownMenuBox(
                        expanded = majorExpanded,
                        onExpandedChange = { majorExpanded = !majorExpanded }
                    ) {
                        OutlinedTextField(
                            value = major,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Major") },
                            leadingIcon = { Icon(Icons.Default.Person, null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = majorExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = majorExpanded,
                            onDismissRequest = { majorExpanded = false }
                        ) {
                            AcademicInfo.MAJORS.forEach { majorOption ->
                                DropdownMenuItem(
                                    text = { Text(majorOption) },
                                    onClick = {
                                        major = majorOption
                                        majorExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Year Dropdown
                    ExposedDropdownMenuBox(
                        expanded = yearExpanded,
                        onExpandedChange = { yearExpanded = !yearExpanded }
                    ) {
                        OutlinedTextField(
                            value = year,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Academic Year") },
                            leadingIcon = { Icon(Icons.Default.DateRange, null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = yearExpanded,
                            onDismissRequest = { yearExpanded = false }
                        ) {
                            AcademicInfo.YEARS.forEach { yearOption ->
                                DropdownMenuItem(
                                    text = { Text(yearOption) },
                                    onClick = {
                                        year = yearOption
                                        yearExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Sports Preferences Section
                    Text(
                        text = "Sports & Skills",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    // Favorite Sports (Multi-select)
                    Text(
                        text = "Select your favorite sports:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Sports.ALL_SPORTS.chunked(2).forEach { sportsRow ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            sportsRow.forEach { sport ->
                                FilterChip(
                                    onClick = {
                                        selectedSports = if (selectedSports.contains(sport)) {
                                            selectedSports - sport
                                        } else {
                                            selectedSports + sport
                                        }
                                    },
                                    label = { Text(sport) },
                                    selected = selectedSports.contains(sport),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    
                    // Skill Level Dropdown
                    ExposedDropdownMenuBox(
                        expanded = skillExpanded,
                        onExpandedChange = { skillExpanded = !skillExpanded }
                    ) {
                        OutlinedTextField(
                            value = skillLevel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Overall Skill Level") },
                            leadingIcon = { Icon(Icons.Default.Star, null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = skillExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = skillExpanded,
                            onDismissRequest = { skillExpanded = false }
                        ) {
                            AcademicInfo.SKILL_LEVELS.forEach { skillOption ->
                                DropdownMenuItem(
                                    text = { Text(skillOption) },
                                    onClick = {
                                        skillLevel = skillOption
                                        skillExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Bio
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio (Optional)") },
                        placeholder = { Text("Tell us about yourself and your sports interests...") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        maxLines = 4
                    )
                }
            }
        }
        
        item {
            // Error/Success messages
            when (val state = authState) {
                is AuthState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                else -> {}
            }
            
            // Sign Up Button
            val isPasswordStrong = remember(password) {
                password.length >= 8 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() } &&
                password.any { !it.isLetterOrDigit() }
            }
            val passwordsMatch = password == confirmPassword && password.isNotBlank()

            Button(
                onClick = {
                    viewModel.signUp(
                        email = email,
                        password = password,
                        confirmPassword = confirmPassword,
                        fullName = fullName,
                        major = major,
                        year = year,
                        favoriteSports = selectedSports.toList(),
                        skillLevel = skillLevel,
                        bio = bio
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = authState !is AuthState.Loading &&
                         fullName.isNotBlank() &&
                         email.isNotBlank() &&
                         password.isNotBlank() &&
                         confirmPassword.isNotBlank() &&
                         passwordsMatch &&
                         isPasswordStrong &&
                         major.isNotBlank() &&
                         year.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Create Account", fontWeight = FontWeight.SemiBold)
                }
            }

            // Show password requirements if not met
            if (password.isNotEmpty() && !isPasswordStrong) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Password must include:",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC62828),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "• At least 8 characters (${password.length}/8)",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (password.length >= 8) Color(0xFF4CAF50) else Color(0xFFC62828)
                        )
                        Text(
                            text = "• Uppercase letter (${if (password.any { it.isUpperCase() }) "✓" else "✗"})",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (password.any { it.isUpperCase() }) Color(0xFF4CAF50) else Color(0xFFC62828)
                        )
                        Text(
                            text = "• Lowercase letter (${if (password.any { it.isLowerCase() }) "✓" else "✗"})",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (password.any { it.isLowerCase() }) Color(0xFF4CAF50) else Color(0xFFC62828)
                        )
                        Text(
                            text = "• Special character (${if (password.any { !it.isLetterOrDigit() }) "✓" else "✗"})",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (password.any { !it.isLetterOrDigit() }) Color(0xFF4CAF50) else Color(0xFFC62828)
                        )
                    }
                }
            }

            // Back to Sign In
            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Already have an account? Sign In")
            }
        }
    }
}