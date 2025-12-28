package com.example.rogueai.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rogueai.data.GameRepository
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject

class GameViewModel(
    private val repo: GameRepository,
    private val roomCode: String,
    private val isSolo: Boolean
) : ViewModel() {

    val gameState: StateFlow<String> = repo.observeGameState()
    val playerBoard: StateFlow<JSONObject?> = repo.observePlayerBoard()
    val gameEnded: StateFlow<JSONObject?> = repo.observeGameEnded()

    // DEBUG dernier message WS brut
    val lastRawMessage: StateFlow<String?> = repo.observeLastRawMessage()

    init {
        // même logique qu'avant
        if (isSolo) {
            repo.connect(roomCode)
            repo.sendReady(true)
        }
    }

    fun executeAction(commandId: String, action: String) {
        repo.executeAction(commandId, action)
    }

    fun leaveGame() {
        repo.resetAfterGameEnd()
        if (isSolo) {
            repo.disconnect()
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (isSolo) {
            repo.disconnect()
        }
    }
}

class GameViewModelFactory(
    private val repo: GameRepository,
    private val roomCode: String,
    private val isSolo: Boolean
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GameViewModel(repo, roomCode, isSolo) as T
    }
}
