package com.example.rogueai.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Gère la connexion WebSocket à une room.
 * Fournit les flux pour : room info, liste des joueurs, game state, fin de partie, etc.
 */
class RoomSocket {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val client = OkHttpClient.Builder()
        .pingInterval(10, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private var webSocket: WebSocket? = null
    private var currentRoomCode: String? = null

    // --- StateFlows exposés au reste de l’app ---
    private val _roomInfo = MutableStateFlow<JSONObject?>(null)
    val roomInfo: StateFlow<JSONObject?> = _roomInfo

    private val _players = MutableStateFlow<List<JSONObject>>(emptyList())
    val players: StateFlow<List<JSONObject>> = _players

    private val _gameStarted = MutableStateFlow(false)
    val gameStarted: StateFlow<Boolean> = _gameStarted

    private val _gameEnded = MutableStateFlow<JSONObject?>(null)
    val gameEnded: StateFlow<JSONObject?> = _gameEnded

    // ----------------------------------------------------------------

    fun openRoomConnection(roomCode: String) {
        currentRoomCode = roomCode

        val request = Request.Builder()
            .url("wss://backend.rogueai.surpuissant.io/ws/room/$roomCode")
            .build()

        webSocket = client.newWebSocket(request, socketListener)
    }

    fun closeRoomConnection() {
        webSocket?.close(1000, "Leaving room")
        webSocket = null
    }

    fun resetAfterGameEnd() {
        _gameEnded.value = null
        _gameStarted.value = false
    }

    // ----------------------------------------------------------------
    // 🔹 ENVOI DES COMMANDES AU SERVEUR
    // ----------------------------------------------------------------

    fun sendReadyFlag(ready: Boolean): Boolean {
        val json = JSONObject()
            .put("type", "player_ready")
            .put("payload", JSONObject().put("ready", ready))

        return webSocket?.send(json.toString()) ?: false
    }

    fun sendExecuteAction(commandId: String, action: String): Boolean {
        val payload = JSONObject()
            .put("command_id", commandId)
            .put("action", action)

        val msg = JSONObject()
            .put("type", "execute_action")   // 👈 correction du texte tronqué
            .put("payload", payload)

        return webSocket?.send(msg.toString()) ?: false
    }

    // ----------------------------------------------------------------
    // 🔹 LISTENER DU WEBSOCKET
    // ----------------------------------------------------------------

    private val socketListener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            println("WS OPENED for room $currentRoomCode")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            try {
                val json = JSONObject(text)

                val type = json.optString("type")
                val payload = json.optJSONObject("payload") ?: JSONObject()

                when (type) {

                    "room_info" -> {
                        _roomInfo.value = payload
                    }

                    "players_update" -> {
                        val playersArray = payload.optJSONArray("players") ?: return
                        val list = List(playersArray.length()) {
                            playersArray.getJSONObject(it)
                        }
                        _players.value = list
                    }

                    "game_started" -> {
                        _gameStarted.value = true
                    }

                    "game_end" -> {
                        _gameEnded.value = payload
                    }

                    else -> {
                        println("WS Received unknown message: $type → $payload")
                    }
                }

            } catch (t: Throwable) {
                println("WS ERROR parsing message: $t")
            }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            // Ignore binary
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            println("WS FAILURE: $t")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            println("WS CLOSED: $reason")
        }
    }
}
