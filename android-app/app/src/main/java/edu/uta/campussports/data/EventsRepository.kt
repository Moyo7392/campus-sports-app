package edu.uta.campussports.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class EventsRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private var eventsListener: ListenerRegistration? = null
    
    private val _events = MutableStateFlow<List<SportsEvent>>(emptyList())
    val events: StateFlow<List<SportsEvent>> = _events
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    init {
        startListeningToEvents()
    }
    
    private fun startListeningToEvents() {
        eventsListener = firestore.collection("events")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Error listening to events: ${error.message}")
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val eventsList = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(SportsEvent::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            println("Error parsing event: ${e.message}")
                            null
                        }
                    }
                    _events.value = eventsList
                }
            }
    }
    
    suspend fun createEvent(event: SportsEvent): Result<String> {
        return try {
            _isLoading.value = true
            println("📝 Creating event: ${event.title} at ${event.location}")
            val docRef = firestore.collection("events").add(event).await()
            _isLoading.value = false
            println("✅ Event created successfully with ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            _isLoading.value = false
            println("❌ Error creating event: ${e.message}")
            println("❌ Error details: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }
    
    suspend fun joinEvent(eventId: String, userId: String): Result<Unit> {
        return try {
            println("📝 Attempting to join event: $eventId with userId: $userId")
            val eventRef = firestore.collection("events").document(eventId)
            val eventDoc = eventRef.get().await()

            if (eventDoc.exists()) {
                val event = eventDoc.toObject(SportsEvent::class.java)
                if (event != null) {
                    if (event.currentParticipants.contains(userId)) {
                        println("❌ User already joined this event")
                        return Result.failure(Exception("Already joined this event"))
                    }

                    if (event.currentParticipants.size >= event.maxParticipants) {
                        println("❌ Event is full")
                        return Result.failure(Exception("Event is full"))
                    }

                    // Fetch user profile to get their full name
                    var userName = userId
                    try {
                        val userDoc = firestore.collection("users").document(userId).get().await()
                        if (userDoc.exists()) {
                            val userProfile = userDoc.toObject(UserProfile::class.java)
                            if (userProfile != null && userProfile.fullName.isNotEmpty()) {
                                userName = userProfile.fullName
                                println("✅ Fetched user name: $userName")
                            }
                        }
                    } catch (e: Exception) {
                        println("⚠️ Error fetching user profile: ${e.message} - using userId as fallback")
                    }

                    val updatedParticipants = event.currentParticipants + userId
                    val updatedParticipantInfo = event.participants + ParticipantInfo(userId = userId, userName = userName)

                    println("📤 Updating event with new participant list. New count: ${updatedParticipants.size}")
                    eventRef.update(
                        mapOf(
                            "currentParticipants" to updatedParticipants,
                            "participants" to updatedParticipantInfo
                        )
                    ).await()

                    println("✅ Event updated successfully")

                    // Add system message to chat
                    addSystemMessage(eventId, userId, "$userName joined the event", MessageType.SYSTEM_JOIN)

                    println("✅ Successfully joined event!")
                    Result.success(Unit)
                } else {
                    println("❌ Could not parse event object")
                    Result.failure(Exception("Event not found"))
                }
            } else {
                println("❌ Event document does not exist")
                Result.failure(Exception("Event not found"))
            }
        } catch (e: Exception) {
            println("❌ Error joining event: ${e.message}")
            println("❌ Stack trace: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }
    
    suspend fun leaveEvent(eventId: String, userId: String): Result<Unit> {
        return try {
            val eventRef = firestore.collection("events").document(eventId)
            val eventDoc = eventRef.get().await()

            if (eventDoc.exists()) {
                val event = eventDoc.toObject(SportsEvent::class.java)
                if (event != null) {
                    val updatedParticipants = event.currentParticipants - userId
                    val updatedParticipantInfo = event.participants.filter { it.userId != userId }

                    eventRef.update(
                        mapOf(
                            "currentParticipants" to updatedParticipants,
                            "participants" to updatedParticipantInfo
                        )
                    ).await()

                    // Add system message to chat
                    addSystemMessage(eventId, userId, "$userId left the event", MessageType.SYSTEM_LEAVE)

                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Event not found"))
                }
            } else {
                Result.failure(Exception("Event not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun addSystemMessage(eventId: String, userId: String, message: String, messageType: MessageType) {
        try {
            val systemMessage = ChatMessage(
                eventId = eventId,
                senderId = "system",
                senderName = "System",
                message = message,
                messageType = messageType,
                timestamp = com.google.firebase.Timestamp.now()
            )
            firestore.collection("chats").add(systemMessage).await()
        } catch (e: Exception) {
            println("Error adding system message: ${e.message}")
        }
    }
    
    fun getEvent(eventId: String): SportsEvent? {
        return _events.value.find { it.id == eventId }
    }
    
    fun getUserEvents(userId: String): List<SportsEvent> {
        return _events.value.filter { event ->
            event.currentParticipants.contains(userId) || event.createdBy == userId
        }
    }

    suspend fun closeEvent(eventId: String, reason: String): Result<Unit> {
        return try {
            val eventRef = firestore.collection("events").document(eventId)
            val eventDoc = eventRef.get().await()

            if (eventDoc.exists()) {
                val event = eventDoc.toObject(SportsEvent::class.java)
                if (event != null) {
                    // Update event to closed status
                    eventRef.update(
                        mapOf(
                            "isActive" to false,
                            "closedReason" to reason,
                            "closedAt" to com.google.firebase.Timestamp.now()
                        )
                    ).await()

                    // Add system message
                    addSystemMessage(eventId, "system", "Event closed by organizer: $reason", MessageType.SYSTEM_LEAVE)

                    println("✅ Event closed successfully")
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Event not found"))
                }
            } else {
                Result.failure(Exception("Event not found"))
            }
        } catch (e: Exception) {
            println("❌ Error closing event: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun kickParticipant(eventId: String, userId: String, userName: String, reason: String): Result<Unit> {
        return try {
            val eventRef = firestore.collection("events").document(eventId)
            val eventDoc = eventRef.get().await()

            if (eventDoc.exists()) {
                val event = eventDoc.toObject(SportsEvent::class.java)
                if (event != null) {
                    // Remove participant from list
                    val updatedParticipants = event.currentParticipants - userId

                    // Add to kicked participants list
                    val kickedParticipant = KickedParticipant(
                        userId = userId,
                        userName = userName,
                        reason = reason,
                        kickedAt = com.google.firebase.Timestamp.now()
                    )
                    val updatedKicked = event.kickedParticipants + kickedParticipant

                    // Update event
                    eventRef.update(
                        mapOf(
                            "currentParticipants" to updatedParticipants,
                            "kickedParticipants" to updatedKicked
                        )
                    ).await()

                    // Add system message
                    addSystemMessage(
                        eventId,
                        "system",
                        "$userName was removed from the event. Reason: $reason",
                        MessageType.SYSTEM_LEAVE
                    )

                    println("✅ Participant kicked successfully")
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Event not found"))
                }
            } else {
                Result.failure(Exception("Event not found"))
            }
        } catch (e: Exception) {
            println("❌ Error kicking participant: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateEventMaxParticipants(eventId: String, newMax: Int): Result<Unit> {
        return try {
            val eventRef = firestore.collection("events").document(eventId)
            eventRef.update("maxParticipants", newMax).await()

            println("✅ Max participants updated successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ Error updating max participants: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun terminateEvent(eventId: String, reason: String): Result<Unit> {
        return try {
            val eventRef = firestore.collection("events").document(eventId)
            val eventDoc = eventRef.get().await()

            if (eventDoc.exists()) {
                val event = eventDoc.toObject(SportsEvent::class.java)
                if (event != null) {
                    // Mark event as terminated (isActive = false)
                    eventRef.update(
                        mapOf(
                            "isActive" to false,
                            "closedReason" to if (reason.isNotEmpty()) reason else "Event terminated by organizer",
                            "closedAt" to com.google.firebase.Timestamp.now()
                        )
                    ).await()

                    // Add system message notifying participants that event was terminated
                    addSystemMessage(
                        eventId,
                        "system",
                        "Event has been terminated by the organizer. ${if (reason.isNotEmpty()) "Reason: $reason" else ""}",
                        MessageType.SYSTEM_LEAVE
                    )

                    println("✅ Event terminated successfully")
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Event not found"))
                }
            } else {
                Result.failure(Exception("Event not found"))
            }
        } catch (e: Exception) {
            println("❌ Error terminating event: ${e.message}")
            Result.failure(e)
        }
    }

    fun stopListening() {
        eventsListener?.remove()
    }
}