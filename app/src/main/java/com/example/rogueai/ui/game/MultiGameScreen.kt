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
 * Écran de jeu multijoueur :
 * - affiche l’état du jeu (game_state)
 * - affiche le player_board : menace, instruction, commandes
 * - permet d’envoyer des actions (execute_action) au backend
 * - montre l’écran de fin quand end_state arrive
 */
@Composable
fun MultiGameScreen(
    roomCode: String,
    roomSocket: RoomSocket,
    onLeave: () -> Unit
) {
    val gameState by roomSocket.gameState.collectAsState()
    val playerBoardJson by roomSocket.playerBoard.collectAsState()
    val gameEndedJson by roomSocket.gameEnded.collectAsState()

    // Copie locale pour permettre le smart cast
    val endState = gameEndedJson

    // 🔹 Si la partie est terminée → on affiche l’écran de fin
    if (endState != null) {
        val win = endState.optBoolean("win", false)
        val tryHistoryArray = endState.optJSONArray("tryHistory")
        val tryCount = tryHistoryArray?.length() ?: 0

        GameOverScreen(
            win = win,
            tryCount = tryCount,
            onBackToHome = onLeave
        )
        return
    }

    // 🔹 Sinon, on affiche l’écran de jeu "normal"
    val threat = playerBoardJson?.optInt("threat")
    val instruction = playerBoardJson?.optJSONObject("instruction")
    val board = playerBoardJson?.optJSONObject("board")
    val commandsArray = board?.optJSONArray("commands")
    val commandsCount = commandsArray?.length() ?: 0
    val highlightedCommandId = instruction?.optString("command_id")

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
                            text = "Instruction actuelle :",
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

                    // Liste des commandes interactives
                    if (commandsArray != null && commandsCount > 0) {
                        Text(
                            text = "Commandes disponibles :",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        for (i in 0 until commandsCount) {
                            val cmd: JSONObject = commandsArray.getJSONObject(i)
                            CommandRow(
                                command = cmd,
                                isHighlighted = (cmd.optString("id") == highlightedCommandId),
                                onExecuteAction = { commandId, action ->
                                    roomSocket.sendExecuteAction(commandId, action)
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
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
                    Text("Quitter la partie et revenir à l'accueil")
                }
            }
        }
    }
}

/**
 * Affiche une commande + les boutons d’actions possibles.
 * Pour les sliders, on affiche une série de boutons (1,2,3…).
 * Pour les toggles, un bouton unique "Exécuter".
 */
@Composable
private fun CommandRow(
    command: JSONObject,
    isHighlighted: Boolean,
    onExecuteAction: (commandId: String, action: String) -> Unit
) {
    val id = command.optString("id", "")
    val name = command.optString("name", "Commande")
    val type = command.optString("type", "?")
    val styleType = command.optString("styleType", "?")
    val actualStatus = command.optString("actual_status", "?")

    val actionsArray = command.optJSONArray("action_possible")
    val actions = mutableListOf<String>()
    if (actionsArray != null) {
        for (i in 0 until actionsArray.length()) {
            actions.add(actionsArray.getString(i))
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        val titlePrefix = if (isHighlighted) ">>> " else ""

        Text(
            text = titlePrefix + name,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "Type: $type [$styleType] – État: $actualStatus",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (actions.isEmpty()) {
            Text(
                text = "Aucune action possible 🤔",
                style = MaterialTheme.typography.bodySmall
            )
            return
        }

        when (type) {
            "slider" -> {
                // On affiche un bouton par valeur possible (1,2,3…)
                Text(
                    text = "Choisis une valeur :",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))

                FlowRowButtons(
                    actions = actions,
                    onClick = { action ->
                        onExecuteAction(id, action)
                    }
                )
            }

            "toggle" -> {
                // En général un seul "toggle" dans actions
                val actionLabel = actions.firstOrNull() ?: "toggle"
                Button(onClick = { onExecuteAction(id, actionLabel) }) {
                    Text("Exécuter : $actionLabel")
                }
            }

            else -> {
                // Fallback générique : un bouton par action possible
                Text(
                    text = "Actions possibles :",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))

                FlowRowButtons(
                    actions = actions,
                    onClick = { action ->
                        onExecuteAction(id, action)
                    }
                )
            }
        }
    }
}

/**
 * Affiche une ligne (ou plusieurs lignes) de boutons pour les valeurs possibles.
 * On reste très simple : pas de vraie "flow layout", juste une colonne de lignes.
 */
@Composable
private fun FlowRowButtons(
    actions: List<String>,
    onClick: (String) -> Unit
) {
    // On coupe en groupes de 4 pour éviter une ligne infinie
    val chunkSize = 4
    actions.chunked(chunkSize).forEach { chunk ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            chunk.forEach { action ->
                Button(
                    onClick = { onClick(action) },
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(action)
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}
