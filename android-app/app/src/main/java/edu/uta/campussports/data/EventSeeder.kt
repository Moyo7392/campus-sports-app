package edu.uta.campussports.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

class EventSeeder {
    private val firestore = FirebaseFirestore.getInstance()
    
    suspend fun seedInitialEvents(): Boolean {
        return try {
            // Check if events already exist
            val existingEvents = firestore.collection("events").limit(1).get().await()
            if (!existingEvents.isEmpty) {
                println("Events already seeded")
                return true
            }
            
            val sampleEvents = listOf(
                SportsEvent(
                    title = "Morning Basketball",
                    sport = "Basketball",
                    location = "MAC Basketball Court 1",
                    date = "Today",
                    time = "8:00 AM",
                    maxParticipants = 10,
                    currentParticipants = emptyList(), // Empty participants
                    createdBy = "system",
                    description = "Casual morning basketball game. All skill levels welcome!",
                    difficulty = "Beginner",
                    createdAt = Timestamp.now()
                ),
                SportsEvent(
                    title = "Soccer Scrimmage",
                    sport = "Soccer",
                    location = "MAC Soccer Field A",
                    date = "Today",
                    time = "6:00 PM",
                    maxParticipants = 22,
                    currentParticipants = emptyList(), // Empty participants
                    createdBy = "system",
                    description = "11v11 soccer game. Looking for players!",
                    difficulty = "Intermediate",
                    createdAt = Timestamp.now()
                ),
                SportsEvent(
                    title = "Volleyball Practice",
                    sport = "Volleyball",
                    location = "MAC Volleyball Court 1",
                    date = "Tomorrow",
                    time = "7:00 PM",
                    maxParticipants = 12,
                    currentParticipants = emptyList(), // Empty participants
                    createdBy = "system",
                    description = "Practice session for beginners and intermediate players",
                    difficulty = "Beginner",
                    createdAt = Timestamp.now()
                ),
                SportsEvent(
                    title = "Tennis Doubles",
                    sport = "Tennis",
                    location = "MAC Tennis Court 2",
                    date = "Tomorrow",
                    time = "5:30 PM",
                    maxParticipants = 4,
                    currentParticipants = emptyList(), // Empty participants
                    createdBy = "system",
                    description = "Doubles tennis match. Need 2 more players!",
                    difficulty = "Intermediate",
                    createdAt = Timestamp.now()
                ),
                SportsEvent(
                    title = "Swimming Laps",
                    sport = "Swimming",
                    location = "MAC Swimming Pool",
                    date = "Tomorrow",
                    time = "7:00 AM",
                    maxParticipants = 8,
                    currentParticipants = emptyList(), // Empty participants
                    createdBy = "system",
                    description = "Morning swim session with lane sharing",
                    difficulty = "Beginner",
                    createdAt = Timestamp.now()
                ),
                SportsEvent(
                    title = "Badminton Tournament",
                    sport = "Badminton",
                    location = "MAC Badminton Court 1",
                    date = "Weekend",
                    time = "2:00 PM",
                    maxParticipants = 8,
                    currentParticipants = emptyList(), // Empty participants
                    createdBy = "system",
                    description = "Single elimination badminton tournament",
                    difficulty = "Advanced",
                    createdAt = Timestamp.now()
                ),
                SportsEvent(
                    title = "Ping Pong Fun",
                    sport = "Ping Pong",
                    location = "MAC Recreation Room - Table 1",
                    date = "Today",
                    time = "12:00 PM",
                    maxParticipants = 4,
                    currentParticipants = emptyList(), // Empty participants
                    createdBy = "system",
                    description = "Casual ping pong games during lunch break",
                    difficulty = "Beginner",
                    createdAt = Timestamp.now()
                ),
                SportsEvent(
                    title = "Campus Morning Run",
                    sport = "Running",
                    location = "Campus Loop Trail",
                    date = "Daily",
                    time = "6:30 AM",
                    maxParticipants = 15,
                    currentParticipants = emptyList(), // Empty participants
                    createdBy = "system",
                    description = "Join us for a morning run around campus!",
                    difficulty = "Beginner",
                    createdAt = Timestamp.now()
                ),
                SportsEvent(
                    title = "3v3 Basketball",
                    sport = "Basketball",
                    location = "MAC Basketball Court 3",
                    date = "Tonight",
                    time = "8:00 PM",
                    maxParticipants = 6,
                    currentParticipants = emptyList(), // Empty participants
                    createdBy = "system",
                    description = "Fast-paced 3v3 basketball games",
                    difficulty = "Intermediate",
                    createdAt = Timestamp.now()
                ),
                SportsEvent(
                    title = "Beach Volleyball Style",
                    sport = "Volleyball",
                    location = "MAC Volleyball Court 2",
                    date = "Weekend",
                    time = "4:00 PM",
                    maxParticipants = 8,
                    currentParticipants = emptyList(), // Empty participants
                    createdBy = "system",
                    description = "2v2 beach volleyball style games (indoor court)",
                    difficulty = "Advanced",
                    createdAt = Timestamp.now()
                )
            )
            
            // Add all events to Firestore
            sampleEvents.forEach { event ->
                firestore.collection("events").add(event).await()
                println("Added event: ${event.title}")
            }
            
            println("Successfully seeded ${sampleEvents.size} events")
            true
        } catch (e: Exception) {
            println("Error seeding events: ${e.message}")
            false
        }
    }
}