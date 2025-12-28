package com.example.rogueai.data

import com.example.rogueai.network.RoomSocket
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject

class GameRepository(private val socket: RoomSocket) {

    // --- Observations (flows du serveur) ---
    fun observeGameState(): StateFlow<String> = socket.gameState
    fun observePlayerBoard(): StateFlow<JSONObject?> = socket.playerBoard
    fun observeGameEnded(): StateFlow<JSONObject?> = socket.gameEnded
    fun observeLastRawMessage(): StateFlow<String?> = socket.lastRawMessage

    // --- Actions (messages vers le serveur) ---
    fun connect(roomCode: String) = socket.openRoomConnection(roomCode)
    fun disconnect() = socket.closeRoomConnection()

    fun resetAfterGameEnd() = socket.resetAfterGameEnd()

    fun sendReady(ready: Boolean) = socket.sendReadyFlag(ready)

    fun executeAction(commandId: String, action: String) =
        socket.sendExecuteAction(commandId, action)
}
