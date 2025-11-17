package com.example.rogueai.data

import com.example.rogueai.network.RoomSocket
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject

class LobbyRepository(private val socket: RoomSocket) {

    fun connect(roomCode: String) {
        socket.openRoomConnection(roomCode)
    }

    fun disconnect() {
        socket.closeRoomConnection()
    }

    fun observeRoomInfo(): StateFlow<JSONObject?> = socket.roomInfo

    fun observePlayers(): StateFlow<List<JSONObject>> = socket.players

    fun observeGameStarted(): StateFlow<Boolean> = socket.gameStarted

    fun sendReady(ready: Boolean) {
        socket.sendReadyFlag(ready)
    }
}
