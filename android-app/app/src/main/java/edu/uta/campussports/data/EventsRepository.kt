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
            val docRef = firestore.collection("events").add(event).await()
            _isLoading.value = false
            Result.success(docRef.id)
        } catch (e: Exception) {
            _isLoading.value = false
            Result.failure(e)
        }
    }
    
    suspend fun joinEvent(eventId: String, userId: String): Result<Unit> {
        return try {
            val eventRef = firestore.collection("events").document(eventId)
            val eventDoc = eventRef.get().await()
            
            if (eventDoc.exists()) {
                val event = eventDoc.toObject(SportsEvent::class.java)
                if (event != null) {
                    if (event.currentParticipants.contains(userId)) {
                        return Result.failure(Exception("Already joined this event"))
                    }
                    
                    if (event.currentParticipants.size >= event.maxParticipants) {
                        return Result.failure(Exception("Event is full"))
                    }
                    
                    val updatedParticipants = event.currentParticipants + userId
                    eventRef.update("currentParticipants", updatedParticipants).await()
                    
                    // Add system message to chat
                    addSystemMessage(eventId, userId, "$userId joined the event", MessageType.SYSTEM_JOIN)
                    
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
    
    suspend fun leaveEvent(eventId: String, userId: String): Result<Unit> {
        return try {
            val eventRef = firestore.collection("events").document(eventId)
            val eventDoc = eventRef.get().await()
            
            if (eventDoc.exists()) {
                val event = eventDoc.toObject(SportsEvent::class.java)
                if (event != null) {
                    val updatedParticipants = event.currentParticipants - userId
                    eventRef.update("currentParticipants", updatedParticipants).await()
                    
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
    suspend fun cancelEvent(eventId: String): Result<Unit> {
        return try {
            FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun stopListening() {
        eventsListener?.remove()
    }
}