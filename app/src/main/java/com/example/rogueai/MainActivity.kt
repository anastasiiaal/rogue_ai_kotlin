package com.example.rogueai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rogueai.data.LobbyRepository
import com.example.rogueai.data.GameRepository
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

                val roomsApi = remember { RoomsApi() }
                val sharedSocket = remember { RoomSocket() }

                // ✅ repos uniques (un par socket partagé)
                val lobbyRepo = remember { LobbyRepository(sharedSocket) }
                val gameRepo  = remember { GameRepository(sharedSocket) }

                var soloRoomCode by remember { mutableStateOf<String?>(null) }
                var inSoloGame by remember { mutableStateOf(false) }

                var lobbyRoomCode by remember { mutableStateOf<String?>(null) }
                var inMultiGame by remember { mutableStateOf(false) }

                when {
                    soloRoomCode != null && inSoloGame -> {
                        GameScreen(
                            roomCode = soloRoomCode!!,
                            // 👇 au lieu de passer socket, on passera le repo (voir note plus bas)
                            // roomSocket = sharedSocket,
                            isSolo = true,
                            onLeave = {
                                sharedSocket.resetAfterGameEnd()
                                sharedSocket.closeRoomConnection()
                                inSoloGame = false
                                soloRoomCode = null
                            },
                            gameRepo = gameRepo
                        )
                    }

                    lobbyRoomCode != null && inMultiGame -> {
                        GameScreen(
                            roomCode = lobbyRoomCode!!,
                            isSolo = false,
                            onLeave = {
                                sharedSocket.resetAfterGameEnd()
                                sharedSocket.closeRoomConnection()
                                inMultiGame = false
                                lobbyRoomCode = null
                            },
                            gameRepo = gameRepo
                        )
                    }

                    lobbyRoomCode != null -> {
                        val lobbyVm: LobbyViewModel = viewModel(
                            key = "lobby-${lobbyRoomCode!!}",
                            factory = LobbyViewModelFactory(
                                repo = lobbyRepo,
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
                            onNavigateToGame = { inMultiGame = true }
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
