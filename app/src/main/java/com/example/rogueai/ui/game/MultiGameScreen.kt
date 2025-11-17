package com.example.rogueai.ui.game

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.rogueai.network.RoomSocket
import org.json.JSONObject

/**
 * Écran de jeu multijoueur V1 :
 * - affiche l’état du jeu (game_state)
 * - affiche le player_board : menace, instruction, liste des commandes
 */
@Composable
fun MultiGameScreen(
    roomCode: String,
    roomSocket: RoomSocket,
    onLeave: () -> Unit
) {
    val gameState by roomSocket.gameState.collectAsState()
    val playerBoardJson by roomSocket.playerBoard.collectAsState()

    val threat = playerBoardJson?.optInt("threat")
    val instruction = playerBoardJson?.optJSONObject("instruction")
    val board = playerBoardJson?.optJSONObject("board")
    val commandsArray = board?.optJSONArray("commands")
    val commandsCount = commandsArray?.length() ?: 0

    Surface(
        modifier = Modifier.fillMaxSize()
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
                    text = "Rogue AI – Partie multijoueur",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Room : $roomCode",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "État du jeu (backend) : $gameState",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (playerBoardJson == null) {
                    Text(
                        text = "En attente du board joueur…",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                } else {
                    // Menace
                    threat?.let {
                        Text(
                            text = "Menace actuelle : $it / 100",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Instruction
                    instruction?.let { instr ->
                        val instrText = instr.optString("instruction_text", "Instruction inconnue")
                        val expectedStatus = instr.optString("expected_status", "")
                        val commandType = instr.optString("command_type", "")
                        val timeout = instr.optLong("timeout", 0L)

                        Text(
                            text = "Instruction :",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = instrText,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Type : $commandType – Attendu : $expectedStatus – Timeout : ${timeout}ms",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Liste des commandes (nom + type)
                    if (commandsArray != null && commandsCount > 0) {
                        Text(
                            text = "Commandes disponibles ($commandsCount) :",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        for (i in 0 until commandsCount) {
                            val cmd: JSONObject = commandsArray.getJSONObject(i)
                            val name = cmd.optString("name", "Commande")
                            val type = cmd.optString("type", "?")
                            val styleType = cmd.optString("styleType", "?")

                            Text(
                                text = "• $name ($type / $styleType)",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Left
                            )
                        }
                    } else {
                        Text(
                            text = "Aucune commande reçue pour l’instant.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = onLeave) {
                    Text("Quitter la partie pour moi")
                }
            }
        }
    }
}
