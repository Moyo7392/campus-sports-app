package edu.uta.campussports.data

import com.google.firebase.Timestamp

// Sports Event Data Model
data class SportsEvent(
    val id: String = "",
    val title: String = "",
    val sport: String = "",
    val location: String = "",
    val date: String = "",
    val time: String = "",
    val maxParticipants: Int = 6,
    val currentParticipants: List<String> = emptyList(), // List of user IDs (kept for backward compatibility)
    val participants: List<ParticipantInfo> = emptyList(), // List of participant info with names
    val createdBy: String = "", // User ID who created the event
    val createdByName: String = "", // Name of user who created the event
    val createdAt: Timestamp = Timestamp.now(),
    val description: String = "",
    val difficulty: String = "Beginner", // Beginner, Intermediate, Advanced
    val isActive: Boolean = true, // Whether event is still active
    val closedReason: String = "", // Reason if event was closed early
    val closedAt: Timestamp? = null, // When event was closed
    val kickedParticipants: List<KickedParticipant> = emptyList() // Track who was kicked and why
) {
    val participantCount: Int get() = currentParticipants.size // Always use currentParticipants as source of truth
    val spotsRemaining: Int get() = maxParticipants - participantCount
    val isFull: Boolean get() = participantCount >= maxParticipants
    val isEventClosed: Boolean get() = !isActive
}

// Data class to track participant info with name
data class ParticipantInfo(
    val userId: String = "",
    val userName: String = ""
)

// Data class to track kicked participants and reason
data class KickedParticipant(
    val userId: String = "",
    val userName: String = "",
    val reason: String = "",
    val kickedAt: Timestamp = Timestamp.now()
)

// Chat Message Data Model
data class ChatMessage(
    val id: String = "",
    val eventId: String = "", // Which event this chat belongs to
    val senderId: String = "",
    val senderName: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val messageType: MessageType = MessageType.TEXT
)

enum class MessageType {
    TEXT, SYSTEM_JOIN, SYSTEM_LEAVE
}

// Expanded User Profile
data class UserProfile(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val major: String = "",
    val year: String = "", // Freshman, Sophomore, Junior, Senior, Graduate
    val favoritesSports: List<String> = emptyList(),
    val skillLevel: String = "Beginner", // Beginner, Intermediate, Advanced
    val bio: String = "",
    val joinedEvents: List<String> = emptyList(), // List of event IDs user joined
    val createdEvents: List<String> = emptyList(), // List of event IDs user created
    val profileImageUrl: String = "",
    val createdAt: Timestamp = Timestamp.now()
)

// Available Sports
object Sports {
    val ALL_SPORTS = listOf(
        "Basketball",
        "Soccer", 
        "Volleyball",
        "Tennis",
        "Swimming",
        "Badminton",
        "Ping Pong",
        "Running"
    )
    
    val UTA_LOCATIONS = mapOf(
        "Basketball" to listOf(
            "MAC Basketball Court 1",
            "MAC Basketball Court 2", 
            "MAC Basketball Court 3",
            "MAC Basketball Court 4"
        ),
        "Soccer" to listOf(
            "MAC Soccer Field A",
            "MAC Soccer Field B",
            "Maverick Stadium Field"
        ),
        "Volleyball" to listOf(
            "MAC Volleyball Court 1",
            "MAC Volleyball Court 2"
        ),
        "Tennis" to listOf(
            "MAC Tennis Court 1",
            "MAC Tennis Court 2",
            "MAC Tennis Court 3"
        ),
        "Swimming" to listOf(
            "MAC Swimming Pool"
        ),
        "Badminton" to listOf(
            "MAC Badminton Court 1",
            "MAC Badminton Court 2"
        ),
        "Ping Pong" to listOf(
            "MAC Recreation Room - Table 1",
            "MAC Recreation Room - Table 2"
        ),
        "Running" to listOf(
            "MAC Indoor Track",
            "Campus Loop Trail",
            "Maverick Stadium Track"
        )
    )
}

// Academic Info
object AcademicInfo {
    val YEARS = listOf("Freshman", "Sophomore", "Junior", "Senior", "Graduate")
    
    val MAJORS = listOf(
        "Computer Science",
        "Engineering", 
        "Business",
        "Biology",
        "Psychology",
        "Mathematics",
        "Physics",
        "Chemistry",
        "English",
        "History",
        "Art",
        "Music",
        "Architecture",
        "Nursing",
        "Education",
        "Criminal Justice",
        "Social Work",
        "Other"
    )
    
    val SKILL_LEVELS = listOf("Beginner", "Intermediate", "Advanced")
}