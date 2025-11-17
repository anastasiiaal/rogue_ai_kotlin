package com.example.rogueai.ui.lobby

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LobbyScreen(
    roomCode: String,
    viewModel: LobbyViewModel,
    onLeave: () -> Unit,
    onNavigateToGame: () -> Unit
) {
    val players by viewModel.players.collectAsState()
    val gameStarted by viewModel.gameStarted.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val roomInfo by viewModel.roomInfo.collectAsState()
    val lastRawMessage by viewModel.lastRawMessage.collectAsState()

    var ready by remember { mutableStateOf(false) }

    LaunchedEffect(roomCode) {
        viewModel.connect()
    }

    LaunchedEffect(gameStarted) {
        if (gameStarted) {
            onNavigateToGame()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Lobby Rogue AI", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))

            Text("Code de la room (client) : $roomCode")
            Spacer(Modifier.height(4.dp))

            Text("État du jeu (backend) : $gameState")
            Spacer(Modifier.height(4.dp))

            Text("Joueurs vus : ${players.size}")
            Spacer(Modifier.height(8.dp))

            if (players.isEmpty()) {
                Text("Aucun joueur (selon backend)")
            } else {
                players.forEach { player ->
                    Text(
                        text = "• ${player.optString("name", "Joueur")} (ready: ${player.optBoolean("ready")})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(onClick = {
                ready = !ready
                viewModel.setReady(ready)
            }) {
                Text(if (ready) "Annuler Ready" else "Je suis prêt")
            }

            Spacer(Modifier.height(12.dp))

            Button(onClick = onLeave) {
                Text("Quitter la room")
            }

            Spacer(Modifier.height(24.dp))

            // DEBUG: room_info brut tronqué
            roomInfo?.let { info ->
                Text(
                    text = "room_info:\n" + info.toString(2).take(220) + if (info.toString().length > 220) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(12.dp))

            // DEBUG: dernier message WS brut
            lastRawMessage?.let { raw ->
                Text(
                    text = "Dernier WS:\n" + raw.take(220) + if (raw.length > 220) "..." else "",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
