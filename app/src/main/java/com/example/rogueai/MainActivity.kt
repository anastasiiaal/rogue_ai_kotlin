package com.example.rogueai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rogueai.network.RoomsApi
import com.example.rogueai.ui.home.HomeScreen
import com.example.rogueai.ui.theme.RogueaiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RogueaiTheme {

                // Client REST pour parler au backend
                val roomsApi = remember { RoomsApi() }

                // Code de la room actuelle (null = on est sur l'écran d'accueil)
                var lobbyRoomCode by remember { mutableStateOf<String?>(null) }

                if (lobbyRoomCode == null) {
                    // Écran d'accueil : on peut créer une partie
                    HomeScreen(
                        roomsApi = roomsApi,
                        onRoomCreated = { code ->
                            lobbyRoomCode = code
                        }
                    )
                } else {
                    // Écran placeholder simple pour le lobby
                    SimpleLobbyPlaceholderScreen(
                        roomCode = lobbyRoomCode!!,
                        onLeave = {
                            // Pour l'instant on se contente de "quitter" la room
                            lobbyRoomCode = null
                        }
                    )

                    /*
                    // 🔜 PLUS TARD : on remettra ici toute la vraie logique Lobby / Game / GameOver.
                    // Exemple (pseudocode) – à réactiver seulement quand tout le reste sera codé :

                    // val sharedSocket = remember { RoomSocket() }
                    // var inGame by remember { mutableStateOf(false) }
                    // var showGameOver by remember { mutableStateOf(false) }
                    // var lastWin by remember { mutableStateOf(false) }
                    // var lastHistory by remember { mutableStateOf(emptyList<TryEntry>()) }

                    // val lobbyVm: LobbyViewModel = viewModel(
                    //     factory = LobbyViewModelFactory(sharedSocket, lobbyRoomCode!!)
                    // )

                    // val endResult by sharedSocket.observeGameEnd().collectAsState(initial = null)

                    // LaunchedEffect(endResult) { ... }

                    // when {
                    //     showGameOver -> GameOverScreen(...)
                    //     inGame -> GameScreen(...)
                    //     else -> LobbyScreen(...)
                    // }
                    */
                }
            }
        }
    }
}

/**
 * Écran ultra simple pour vérifier que la navigation fonctionne
 * quand une room est créée.
 */
@Composable
private fun SimpleLobbyPlaceholderScreen(
    roomCode: String,
    onLeave: () -> Unit
) {
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
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Room créée avec succès : $roomCode",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onLeave) {
                Text("Quitter la room et revenir à l'accueil")
            }
        }
    }
}
