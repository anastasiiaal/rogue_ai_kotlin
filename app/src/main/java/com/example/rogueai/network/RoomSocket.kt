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

    // --- StateFlows exposés au reste de l’app ---

    // Infos de la room (you, players, room_state, level, etc.)
    private val _roomInfo = MutableStateFlow<JSONObject?>(null)
    val roomInfo: StateFlow<JSONObject?> = _roomInfo

    // Liste de joueurs (extraite de room_info.players)
    private val _players = MutableStateFlow<List<JSONObject>>(emptyList())
    val players: StateFlow<List<JSONObject>> = _players

    // Dernier game_state brut ("lobby_waiting", "lobby_ready", "timer_before_start", "game_start", "end_state", etc.)
    private val _gameState = MutableStateFlow("unknown")
    val gameState: StateFlow<String> = _gameState

    // True si on considère que la partie est "en cours" côté client
    private val _gameStarted = MutableStateFlow(false)
    val gameStarted: StateFlow<Boolean> = _gameStarted

    // Fin de partie (payload de game_state avec state = end_state)
    private val _gameEnded = MutableStateFlow<JSONObject?>(null)
    val gameEnded: StateFlow<JSONObject?> = _gameEnded

    // Board du joueur + instruction + threat (message player_board)
    private val _playerBoard = MutableStateFlow<JSONObject?>(null)
    val playerBoard: StateFlow<JSONObject?> = _playerBoard

    // ----------------------------------------------------------------
    // Connexion / déconnexion
    // ----------------------------------------------------------------

    fun openRoomConnection(roomCode: String) {
        currentRoomCode = roomCode

        // D’après ta doc : wss://backend.rogueai.surpuissant.io/?room=${roomCode}
        val request = Request.Builder()
            .url("wss://backend.rogueai.surpuissant.io/?room=$roomCode")
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
        _playerBoard.value = null
        _gameState.value = "unknown"
    }

    // ----------------------------------------------------------------
    // ENVOIS vers le backend
    // ----------------------------------------------------------------

    /**
     * D’après la doc :
     * { type: "room", payload: { ready: true } }
     */
    fun sendReadyFlag(ready: Boolean): Boolean {
        val msg = JSONObject()
            .put("type", "room")
            .put("payload", JSONObject().put("ready", ready))

        return webSocket?.send(msg.toString()) ?: false
    }

    /**
     * D’après la doc :
     * {
     *   type: "execute_action",
     *   payload: { command_id: "...", action: "..." }
     * }
     */
    fun sendExecuteAction(commandId: String, action: String): Boolean {
        val payload = JSONObject()
            .put("command_id", commandId)
            .put("action", action)

        val msg = JSONObject()
            .put("type", "execute_action")
            .put("payload", payload)

        return webSocket?.send(msg.toString()) ?: false
    }

    // ----------------------------------------------------------------
    // Listener WebSocket
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

                    // Infos de room complètes
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

                    // Changement d’état global de la partie
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

                    // Plateau + instruction + menace pour le joueur
                    "player_board" -> {
                        _playerBoard.value = payload   // 👈 Maintenant ce champ existe, donc plus d’erreur
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
