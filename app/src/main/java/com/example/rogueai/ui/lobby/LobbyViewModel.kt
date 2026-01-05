package com.example.rogueai.ui.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rogueai.data.LobbyRepository

class LobbyViewModel(
    private val repo: LobbyRepository,
    private val roomCode: String
) : ViewModel() {

    val roomInfo = repo.observeRoomInfo()
    val players = repo.observePlayers()
    val gameStarted = repo.observeGameStarted()
    val gameState = repo.observeGameState()
    val playerBoard = repo.observePlayerBoard()
    val lastRawMessage = repo.observeLastRawMessage()

    fun connect() {
        repo.connect(roomCode)
    }

    fun setReady(ready: Boolean) {
        repo.sendReady(ready)
    }

    fun leaveLobby() {
        repo.disconnect()
    }

    override fun onCleared() {
        super.onCleared()
        repo.disconnect()
    }
}

class LobbyViewModelFactory(
    private val repo: LobbyRepository,
    private val roomCode: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LobbyViewModel(repo, roomCode) as T
    }
}
