package com.example.rogueai.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rogueai.network.RoomSocket
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject

class GameViewModel(
    private val socket: RoomSocket,
    private val roomCode: String,
    private val isSolo: Boolean
) : ViewModel() {

    val gameState: StateFlow<String> = socket.gameState
    val playerBoard: StateFlow<JSONObject?> = socket.playerBoard
    val gameEnded: StateFlow<JSONObject?> = socket.gameEnded

    init {
        if (isSolo) {
            socket.openRoomConnection(roomCode)
            socket.sendReadyFlag(true)
        }
    }

    fun executeAction(commandId: String, action: String) {
        socket.sendExecuteAction(commandId, action)
    }

    fun leaveGame() {
        socket.resetAfterGameEnd()
        if (isSolo) {
            socket.closeRoomConnection()
        }
    }

    override fun onCleared() {
        super.onCleared()
        // On ne ferme la connexion que pour le solo : en multi c’est géré par le flux de navigation
        if (isSolo) {
            socket.closeRoomConnection()
        }
    }
}

class GameViewModelFactory(
    private val socket: RoomSocket,
    private val roomCode: String,
    private val isSolo: Boolean
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GameViewModel(socket, roomCode, isSolo) as T
    }
}
