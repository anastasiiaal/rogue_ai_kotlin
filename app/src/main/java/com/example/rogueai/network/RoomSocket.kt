package com.example.rogueai.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class RoomSocket {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val client = OkHttpClient.Builder()
        .pingInterval(10, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private var webSocket: WebSocket? = null
    private var currentRoomCode: String? = null

    // --- StateFlows exposés ---

    private val _roomInfo = MutableStateFlow<JSONObject?>(null)
    val roomInfo: StateFlow<JSONObject?> = _roomInfo

    private val _players = MutableStateFlow<List<JSONObject>>(emptyList())
    val players: StateFlow<List<JSONObject>> = _players

    private val _gameState = MutableStateFlow("unknown")
    val gameState: StateFlow<String> = _gameState

    private val _gameStarted = MutableStateFlow(false)
    val gameStarted: StateFlow<Boolean> = _gameStarted

    private val _gameEnded = MutableStateFlow<JSONObject?>(null)
    val gameEnded: StateFlow<JSONObject?> = _gameEnded

    private val _playerBoard = MutableStateFlow<JSONObject?>(null)
    val playerBoard: StateFlow<JSONObject?> = _playerBoard

    // 🔍 Debug : dernier message brut reçu
    private val _lastRawMessage = MutableStateFlow<String?>(null)
    val lastRawMessage: StateFlow<String?> = _lastRawMessage

    // ----------------------------------------------------------------

    fun openRoomConnection(roomCode: String) {
        val normalized = roomCode.trim().uppercase()

        // Si un socket existe, on le ferme proprement pour éviter les doubles listeners
        if (webSocket != null) {
            println("WS: closing previous socket before reconnect")
            webSocket?.close(1000, "Reconnect")
            webSocket = null
        }

        currentRoomCode = normalized

        // reset AVANT d’ouvrir la connexion
        _gameState.value = "unknown"
        _gameStarted.value = false
        _gameEnded.value = null
        _playerBoard.value = null
        _roomInfo.value = null
        _players.value = emptyList()
        _lastRawMessage.value = null

        val request = Request.Builder()
            .url("wss://backend.rogueai.surpuissant.io/?room=$normalized")
            .build()

        println("WS: trying to connect to ${request.url}")
        webSocket = client.newWebSocket(request, socketListener)
    }

    fun closeRoomConnection() {
        println("WS: closing socket for room $currentRoomCode")
        webSocket?.close(1000, "Leaving room")
        webSocket = null
        currentRoomCode = null
    }

    fun resetAfterGameEnd() {
        _gameEnded.value = null
        _gameStarted.value = false
        _playerBoard.value = null
        _gameState.value = "unknown"
    }

    // ----------------------------------------------------------------
    // ENVOI
    // ----------------------------------------------------------------

    fun sendReadyFlag(ready: Boolean): Boolean {
        val msg = JSONObject()
            .put("type", "room")
            .put("payload", JSONObject().put("ready", ready))

        println("WS SEND: $msg")
        return webSocket?.send(msg.toString()) ?: false
    }

    fun sendExecuteAction(commandId: String, action: String): Boolean {
        val payload = JSONObject()
            .put("command_id", commandId)
            .put("action", action)

        val msg = JSONObject()
            .put("type", "execute_action")
            .put("payload", payload)

        println("WS SEND: $msg")
        return webSocket?.send(msg.toString()) ?: false
    }

    // ----------------------------------------------------------------
    // LISTENER
    // ----------------------------------------------------------------

    private val socketListener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            println("WS OPENED for room $currentRoomCode (code=${response.code})")
            _gameState.value = "opened"  // DEBUG pour voir que la connexion est OK
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            println("WS RECV: $text")
            _lastRawMessage.value = text

            try {
                val json = JSONObject(text)
                val type = json.optString("type")
                val payload = json.optJSONObject("payload") ?: JSONObject()

                when (type) {

                    "room_info" -> {
                        _roomInfo.value = payload

                        val playersArray = payload.optJSONArray("players")
                        if (playersArray != null) {
                            val list = List(playersArray.length()) {
                                playersArray.getJSONObject(it)
                            }
                            _players.value = list
                        }
                    }

                    "game_state" -> {
                        val state = payload.optString("state")
                        _gameState.value = state   // ICI qu’on met _gameState.value = state

                        when (state) {
                            "lobby_waiting",
                            "lobby_ready" -> {
                                _gameStarted.value = false
                            }

                            "timer_before_start",
                            "game_start" -> {
                                _gameStarted.value = true
                            }

                            "end_state" -> {
                                _gameEnded.value = payload
                                _gameStarted.value = false
                            }
                        }
                    }

                    "player_board" -> {
                        _playerBoard.value = payload
                    }

                    else -> {
                        println("WS Received unknown message: $type → $payload")
                    }
                }

            } catch (t: Throwable) {
                println("WS ERROR parsing message: $t")
                _gameState.value = "parse_error"
            }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            // Ignore binary
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            println("WS FAILURE: $t ; response=$response")
            _gameState.value = "failure:${t.javaClass.simpleName}"
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            println("WS CLOSED: code=$code reason=$reason")
        }
    }
}
