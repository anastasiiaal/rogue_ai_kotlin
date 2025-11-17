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

    var ready by remember { mutableStateOf(false) }

    // Navigation automatique si le backend envoie "timer_before_start" / "game_start"
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

            Text(
                text = "Lobby Rogue AI",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Code de la room : $roomCode",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "État du jeu (backend) : $gameState",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (players.isEmpty()) {
                Text("En attente des joueurs…")
            } else {
                players.forEach { player ->
                    Text(
                        text = "• ${player.optString("name", "Joueur")} " +
                                "(ready: ${player.optBoolean("ready")})"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                ready = !ready
                viewModel.setReady(ready)
            }) {
                Text(if (ready) "Annuler Ready" else "Je suis prêt")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = onLeave) {
                Text("Quitter la room")
            }
        }
    }
}
