package com.example.rogueai.ui.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rogueai.network.RoomSocket
import com.example.rogueai.ui.game.components.CommandCard
import org.json.JSONObject
import androidx.compose.foundation.layout.FlowRow
import com.example.rogueai.ui.game.components.ThreatBar

/**
 * Écran de jeu principal, utilisé à la fois pour le mode solo et le mode multi.
 *
 * @param isSolo  true = on ouvre la connexion WS ici + ready auto
 *                false = la connexion est gérée par le Lobby (multi)
 */
@Composable
fun GameScreen(
    roomCode: String,
    roomSocket: RoomSocket,
    isSolo: Boolean,
    onLeave: () -> Unit
) {
    val gameVm: GameViewModel = viewModel(
        key = "game-$roomCode-$isSolo",
        factory = GameViewModelFactory(
            socket = roomSocket,
            roomCode = roomCode,
            isSolo = isSolo
        )
    )

    val gameState by gameVm.gameState.collectAsState()
    val playerBoardJson by gameVm.playerBoard.collectAsState()
    val gameEndedJson by gameVm.gameEnded.collectAsState()

    val endState = gameEndedJson

    if (endState != null) {
        val win = endState.optBoolean("win", false)
        val tryHistoryArray = endState.optJSONArray("tryHistory")
        val tryCount = tryHistoryArray?.length() ?: 0

        GameOverScreen(
            win = win,
            tryCount = tryCount,
            onBackToHome = {
                gameVm.leaveGame()
                onLeave()
            }
        )
        return
    }

    // 🔹 Partie en cours
    val threat = playerBoardJson?.optInt("threat")
    val instruction = playerBoardJson?.optJSONObject("instruction")
    val board = playerBoardJson?.optJSONObject("board")
    val commandsArray = board?.optJSONArray("commands")

    val highlightedCommandId = instruction?.optString("command_id")

    val commands: List<JSONObject> = if (commandsArray != null) {
        List(commandsArray.length()) { index ->
            commandsArray.getJSONObject(index)
        }
    } else {
        emptyList()
    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())   // tout l’écran est scrollable
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        gameVm.leaveGame()
                        onLeave()
                    }
                ) {
                    Text("↩️ Accueil", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "$roomCode " + if (isSolo) "\uD83D\uDC64" else "\uD83D\uDC65",
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

//            Text(
//                text = "Room : $roomCode",
//                style = MaterialTheme.typography.bodyMedium,
//                modifier = Modifier.align(Alignment.CenterHorizontally)
//            )

            Spacer(modifier = Modifier.height(4.dp))

//            Text(
//                text = "État du jeu (backend) : $gameState",
//                style = MaterialTheme.typography.bodySmall,
//                modifier = Modifier.align(Alignment.CenterHorizontally)
//            )

            Spacer(modifier = Modifier.height(12.dp))

            // Menace + instruction
            if (playerBoardJson == null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "En attente du board joueur…",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                ThreatBar(threatLevel = threat)

                Spacer(modifier = Modifier.height(8.dp))

                instruction?.let { instr ->
                    val instrText = instr.optString("instruction_text", "Instruction inconnue")
                    val expectedStatus = instr.optString("expected_status", "")
                    val commandType = instr.optString("command_type", "")
                    val timeout = instr.optLong("timeout", 0L)

//                    Text(
//                        text = "Instruction actuelle :",
//                        style = MaterialTheme.typography.titleMedium,
//                        modifier = Modifier.align(Alignment.CenterHorizontally)
//                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = instrText,
                        // 🔹 plus gros pour être bien visible
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
//                    Spacer(modifier = Modifier.height(6.dp))
//                    Text(
//                        text = "Type : $commandType – Attendu : $expectedStatus – Timeout : ${timeout}ms",
//                        style = MaterialTheme.typography.bodySmall,
//                        textAlign = TextAlign.Center,
//                        modifier = Modifier.align(Alignment.CenterHorizontally)
//                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Grille de commandes (2 colonnes)
            if (commands.isNotEmpty()) {
                Text(
                    text = "Commandes disponibles :",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    maxItemsInEachRow = 2
                ) {
                    commands.forEach { cmd ->
                        val id = cmd.optString("id", "")
                        val name = cmd.optString("name", "Commande")
                        val type = cmd.optString("type", "?")
                        val styleType = cmd.optString("styleType", "?")
                        val actualStatus = cmd.optString("actual_status", "?")

                        val actionsArray = cmd.optJSONArray("action_possible")
                        val actions = mutableListOf<String>()
                        if (actionsArray != null) {
                            for (j in 0 until actionsArray.length()) {
                                actions.add(actionsArray.getString(j))
                            }
                        }

                        val isHighlighted = (id == highlightedCommandId)

                        CommandCard(
                            type = type,
                            styleType = styleType,
                            id = id,
                            name = name,
                            actualStatus = actualStatus,
                            actions = actions,
                            isHighlighted = isHighlighted,
                            onExecuteAction = { commandId, action ->
                                gameVm.executeAction(commandId, action)
                            },
                            modifier = Modifier
                                .weight(1f, fill = true)   // 2 cards par row
                        )
                    }
                }
            } else {
                if (playerBoardJson != null) {
                    Text(
                        text = "Aucune commande reçue pour l’instant.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
