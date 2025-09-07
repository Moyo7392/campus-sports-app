package edu.uta.campussports.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import edu.uta.campussports.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EventsViewModel : ViewModel() {
    private val eventsRepository = EventsRepository()
    private val chatRepository = ChatRepository()
    
    val events = eventsRepository.events
    val isLoading = eventsRepository.isLoading
    val eventChats = chatRepository.eventChats
    
    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState: StateFlow<ActionState> = _actionState
    
    private val _selectedEvent = MutableStateFlow<SportsEvent?>(null)
    val selectedEvent: StateFlow<SportsEvent?> = _selectedEvent
    
    fun createEvent(
        title: String,
        sport: String,
        location: String,
        date: String,
        time: String,
        maxParticipants: Int,
        difficulty: String,
        description: String,
        createdBy: String
    ) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            
            val event = SportsEvent(
                title = title.trim(),
                sport = sport,
                location = location,
                date = date,
                time = time,
                maxParticipants = maxParticipants,
                difficulty = difficulty,
                description = description.trim(),
                createdBy = createdBy,
                createdAt = Timestamp.now(),
                currentParticipants = listOf(createdBy) // Creator automatically joins
            )
            
            val result = eventsRepository.createEvent(event)
            _actionState.value = if (result.isSuccess) {
                ActionState.Success("Event created successfully!")
            } else {
                ActionState.Error("Failed to create event: ${result.exceptionOrNull()?.message}")
            }
        }
    }
    
    fun joinEvent(eventId: String, userId: String) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            
            val result = eventsRepository.joinEvent(eventId, userId)
            _actionState.value = if (result.isSuccess) {
                ActionState.Success("Successfully joined the event!")
            } else {
                ActionState.Error(result.exceptionOrNull()?.message ?: "Failed to join event")
            }
        }
    }
    
    fun leaveEvent(eventId: String, userId: String) {
        viewModelScope.launch {
            _actionState.value = ActionState.Loading
            
            val result = eventsRepository.leaveEvent(eventId, userId)
            _actionState.value = if (result.isSuccess) {
                ActionState.Success("Left the event")
            } else {
                ActionState.Error(result.exceptionOrNull()?.message ?: "Failed to leave event")
            }
        }
    }
    
    fun selectEvent(event: SportsEvent?) {
        _selectedEvent.value = event
        event?.let { chatRepository.startListeningToEventChat(it.id) }
    }
    
    fun sendMessage(eventId: String, senderId: String, senderName: String, message: String) {
        if (message.trim().isEmpty()) return
        
        viewModelScope.launch {
            val result = chatRepository.sendMessage(eventId, senderId, senderName, message)
            if (result.isFailure) {
                _actionState.value = ActionState.Error("Failed to send message")
            }
        }
    }
    
    fun getEventMessages(eventId: String): List<ChatMessage> {
        return chatRepository.getEventMessages(eventId)
    }
    
    fun isUserInEvent(eventId: String, userId: String): Boolean {
        val event = eventsRepository.getEvent(eventId)
        return event?.currentParticipants?.contains(userId) == true
    }
    
    fun getUserEvents(userId: String): List<SportsEvent> {
        return eventsRepository.getUserEvents(userId)
    }
    
    fun clearActionState() {
        _actionState.value = ActionState.Idle
    }
    
    override fun onCleared() {
        super.onCleared()
        eventsRepository.stopListening()
        chatRepository.stopAllListeners()
    }
}

sealed class ActionState {
    object Idle : ActionState()
    object Loading : ActionState()
    data class Success(val message: String) : ActionState()
    data class Error(val message: String) : ActionState()
}