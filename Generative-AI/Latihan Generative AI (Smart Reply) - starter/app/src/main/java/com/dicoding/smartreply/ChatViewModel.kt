package com.dicoding.smartreply

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.nl.smartreply.SmartReply
import com.google.mlkit.nl.smartreply.SmartReplyGenerator
import com.google.mlkit.nl.smartreply.SmartReplySuggestion
import com.google.mlkit.nl.smartreply.SmartReplySuggestionResult
import com.google.mlkit.nl.smartreply.TextMessage

class ChatViewModel : ViewModel() {

    private val anotherUserID = "101"
    private val _chatHistory = MutableLiveData<ArrayList<Message>>()
    val chatHistory: LiveData<ArrayList<Message>> = _chatHistory

    private val _pretendingAsAnotherUser = MutableLiveData(false)
    val pretendingAsAnotherUser: LiveData<Boolean> = _pretendingAsAnotherUser

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val smartReply: SmartReplyGenerator = SmartReply.getClient()
    private val _smartReplyOptions = MediatorLiveData<List<SmartReplySuggestion>>()
    val smartReplyOptions: LiveData<List<SmartReplySuggestion>> = _smartReplyOptions

    init {
        initSmartReplyOptionsGenerator()
    }

    fun switchUser() {
        _pretendingAsAnotherUser.value = _pretendingAsAnotherUser.value?.not()
    }

    fun setMessages(messages: ArrayList<Message>) {
        clearSmartReplyOptions()
        _chatHistory.value = messages
    }

    private fun clearSmartReplyOptions() {
        _smartReplyOptions.value = emptyList()
    }

    fun addMessages(message: String) {
        val isLocalUser = _pretendingAsAnotherUser.value ?: false
        val list = _chatHistory.value ?: ArrayList()
        list.add(Message(message, !isLocalUser, System.currentTimeMillis()))
        clearSmartReplyOptions()
        _chatHistory.value = list
    }

    private fun initSmartReplyOptionsGenerator() {
        _smartReplyOptions.addSource(_pretendingAsAnotherUser) { generateSmartReplyOptions() }
        _smartReplyOptions.addSource(_chatHistory) { generateSmartReplyOptions() }
    }

    private fun generateSmartReplyOptions() {
        val messages = _chatHistory.value.orEmpty()
        val isPretendingAsAnotherUser = _pretendingAsAnotherUser.value ?: false

        if (messages.isEmpty()) return

        generateSmartReplyOptions(messages, isPretendingAsAnotherUser)
            .addOnSuccessListener { result -> _smartReplyOptions.value = result }
    }

    private fun generateSmartReplyOptions(
        messages: List<Message>,
        isPretendingAsAnotherUser: Boolean
    ): Task<List<SmartReplySuggestion>> {
        val lastMessage = messages.last()
        if (lastMessage.isLocalUser == isPretendingAsAnotherUser) {
            return Tasks.forException(Exception("Tidak menjalankan smart reply!"))
        }

        val chatConversations = messages.map { message ->
            if (message.isLocalUser != isPretendingAsAnotherUser) {
                TextMessage.createForLocalUser(message.text, message.timestamp)
            } else {
                TextMessage.createForRemoteUser(message.text, message.timestamp, anotherUserID)
            }
        }

        return smartReply
            .suggestReplies(chatConversations)
            .continueWith { task ->
                val result = task.result
                when (result.status) {
                    SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE -> _errorMessage.value =
                        "Unable to generate options due to a non-English language was used"

                    SmartReplySuggestionResult.STATUS_NO_REPLY -> _errorMessage.value =
                        "No appropriate response found"
                }
                result.suggestions
            }
            .addOnFailureListener {
                _errorMessage.value = "An error has occurred on Smart Reply Instance"
            }
    }

    override fun onCleared() {
        super.onCleared()
        smartReply.close()
    }

    companion object {
        private const val TAG = "ChatViewModel"
    }
}
