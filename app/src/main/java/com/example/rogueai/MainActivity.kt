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
import com.example.rogueai.ui.game.MultiGameScreen
import com.example.rogueai.ui.game.SoloGameScreen
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

                // Room multi / join
                var lobbyRoomCode by remember { mutableStateOf<String?>(null) }

                // Sommes-nous en phase "jeu" multi (après lobby) ?
                var inMultiGame by remember { mutableStateOf(false) }

                when {
                    // 🔹 1) Mode solo : écran local de test
                    soloRoomCode != null -> {
                        SoloGameScreen(
                            roomCode = soloRoomCode!!,
                            onExit = {
                                soloRoomCode = null
                            }
                        )
                    }

                    // 🔹 2) Mode multi : partie en cours
                    inMultiGame && lobbyRoomCode != null -> {
                        MultiGameScreen(
                            roomCode = lobbyRoomCode!!,
                            onLeave = {
                                // On ferme la connexion WebSocket et on reset l'état
                                sharedSocket.resetAfterGameEnd()
                                sharedSocket.closeRoomConnection()
                                inMultiGame = false
                                lobbyRoomCode = null
                            }
                        )
                    }

                    // 🔹 3) Mode multi : lobby en attente
                    lobbyRoomCode != null -> {

                        val lobbyVm: LobbyViewModel = viewModel(
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

                    // 🔹 4) Sinon : écran d’accueil
                    else -> {
                        HomeScreen(
                            roomsApi = roomsApi,
                            onSoloRoomCreated = { code ->
                                soloRoomCode = code
                            },
                            onMultiRoomCreated = { code ->
                                lobbyRoomCode = code
                            },
                            onRoomJoined = { code ->
                                lobbyRoomCode = code
                            }
                        )
                    }
                }
            }
        }
    }
}
