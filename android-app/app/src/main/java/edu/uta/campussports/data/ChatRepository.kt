package edu.uta.campussports.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val chatListeners = mutableMapOf<String, ListenerRegistration>()
    
    private val _eventChats = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val eventChats: StateFlow<Map<String, List<ChatMessage>>> = _eventChats
    
    fun startListeningToEventChat(eventId: String) {
        // Don't create duplicate listeners
        if (chatListeners.containsKey(eventId)) return
        
        val listener = firestore.collection("chats")
            .whereEqualTo("eventId", eventId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Error listening to chat: ${error.message}")
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val messages = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            println("Error parsing chat message: ${e.message}")
                            null
                        }
                    }
                    
                    val currentChats = _eventChats.value.toMutableMap()
                    currentChats[eventId] = messages
                    _eventChats.value = currentChats
                }
            }
        
        chatListeners[eventId] = listener
    }
    
    suspend fun sendMessage(eventId: String, senderId: String, senderName: String, message: String): Result<Unit> {
        return try {
            val chatMessage = ChatMessage(
                eventId = eventId,
                senderId = senderId,
                senderName = senderName,
                message = message.trim(),
                timestamp = com.google.firebase.Timestamp.now(),
                messageType = MessageType.TEXT
            )
            
            firestore.collection("chats").add(chatMessage).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getEventMessages(eventId: String): List<ChatMessage> {
        return _eventChats.value[eventId] ?: emptyList()
    }
    
    fun stopListeningToEventChat(eventId: String) {
        chatListeners[eventId]?.remove()
        chatListeners.remove(eventId)
    }
    
    fun stopAllListeners() {
        chatListeners.values.forEach { it.remove() }
        chatListeners.clear()
    }
}