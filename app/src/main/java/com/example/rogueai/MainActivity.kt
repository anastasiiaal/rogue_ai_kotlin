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

                val roomsApi = remember { RoomsApi() }

                var lobbyRoomCode by remember { mutableStateOf<String?>(null) }

                if (lobbyRoomCode == null) {
                    HomeScreen(
                        roomsApi = roomsApi,
                        onRoomCreated = { code ->
                            lobbyRoomCode = code
                        },
                        onRoomJoined = { code ->
                            lobbyRoomCode = code
                        }
                    )
                } else {
                    SimpleLobbyPlaceholderScreen(
                        roomCode = lobbyRoomCode!!,
                        onLeave = { lobbyRoomCode = null }
                    )
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
