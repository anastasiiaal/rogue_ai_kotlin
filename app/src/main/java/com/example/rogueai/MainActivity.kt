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

                // Room multi / join (lobby connecté au backend)
                var lobbyRoomCode by remember { mutableStateOf<String?>(null) }

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

                    // 🔹 2) Mode multijoueur / join : vrai LobbyScreen branché au WebSocket
                    lobbyRoomCode != null -> {

                        // On crée un LobbyViewModel branché sur le RoomSocket partagé
                        val lobbyVm: LobbyViewModel = viewModel(
                            factory = LobbyViewModelFactory(
                                repo = LobbyRepository(sharedSocket),
                                roomCode = lobbyRoomCode!!
                            )
                        )

                        LobbyScreen(
                            viewModel = lobbyVm,
                            onLeave = {
                                lobbyVm.leaveLobby()
                                lobbyRoomCode = null
                            },
                            onNavigateToGame = {
                                // TODO : ici on branchera le vrai GameScreen multijoueur plus tard
                                // Pour l’instant, on peut juste ignorer ou logger.
                            }
                        )
                    }

                    // 🔹 3) Sinon : écran d’accueil
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

