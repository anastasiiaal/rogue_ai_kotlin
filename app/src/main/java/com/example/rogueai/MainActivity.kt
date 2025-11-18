package com.example.rogueai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rogueai.data.LobbyRepository
import com.example.rogueai.network.RoomSocket
import com.example.rogueai.network.RoomsApi
import com.example.rogueai.ui.game.GameScreen
import com.example.rogueai.ui.home.HomeScreen
import com.example.rogueai.ui.lobby.LobbyScreen
import com.example.rogueai.ui.lobby.LobbyViewModel
import com.example.rogueai.ui.lobby.LobbyViewModelFactory
import com.example.rogueai.ui.theme.RogueaiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RogueaiTheme {

                // REST client pour créer / vérifier les rooms
                val roomsApi = remember { RoomsApi() }

                // WebSocket partagé pour le mode multijoueur
                val sharedSocket = remember { RoomSocket() }

                // Room solo en cours (mode test local)
                var soloRoomCode by remember { mutableStateOf<String?>(null) }
                var inSoloGame by remember { mutableStateOf(false) }

                // Room multi / join
                var lobbyRoomCode by remember { mutableStateOf<String?>(null) }

                // Sommes-nous en phase "jeu" multi (après lobby) ?
                var inMultiGame by remember { mutableStateOf(false) }

                when {
                    // 🔹 SOLO GAME
                    soloRoomCode != null && inSoloGame -> {
                        GameScreen(
                            roomCode = soloRoomCode!!,
                            roomSocket = sharedSocket,
                            isSolo = true,
                            onLeave = {
                                sharedSocket.resetAfterGameEnd()
                                sharedSocket.closeRoomConnection()
                                inSoloGame = false
                                soloRoomCode = null
                            }
                        )
                    }

                    // 🔹 MULTI : en partie
                    lobbyRoomCode != null && inMultiGame -> {
                        GameScreen(
                            roomCode = lobbyRoomCode!!,
                            roomSocket = sharedSocket,
                            isSolo = false,
                            onLeave = {
                                sharedSocket.resetAfterGameEnd()
                                sharedSocket.closeRoomConnection()
                                inMultiGame = false
                                lobbyRoomCode = null
                            }
                        )
                    }

                    // 🔹 MULTI : lobby
                    lobbyRoomCode != null -> {
                        val lobbyVm: LobbyViewModel = viewModel(
                            key = "lobby-${lobbyRoomCode!!}",
                            factory = LobbyViewModelFactory(
                                repo = LobbyRepository(sharedSocket),
                                roomCode = lobbyRoomCode!!
                            )
                        )

                        LobbyScreen(
                            roomCode = lobbyRoomCode!!,
                            viewModel = lobbyVm,
                            onLeave = {
                                lobbyVm.leaveLobby()
                                lobbyRoomCode = null
                            },
                            onNavigateToGame = {
                                inMultiGame = true
                            }
                        )
                    }

                    else -> {
                        HomeScreen(
                            roomsApi = roomsApi,
                            onSoloRoomCreated = { code ->
                                soloRoomCode = code
                                inSoloGame = true
                            },
                            onMultiRoomCreated = { code ->
                                lobbyRoomCode = code
                                inMultiGame = false
                            },
                            onRoomJoined = { code ->
                                lobbyRoomCode = code
                                inMultiGame = false
                            }
                        )
                    }
                }
            }
        }
    }
}
