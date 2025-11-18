package com.example.rogueai.ui.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.rogueai.network.RoomSocket
import com.example.rogueai.ui.game.components.SliderCommandView
import com.example.rogueai.ui.game.components.ToggleCommandView
import org.json.JSONObject

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
    // 🔹 Mode solo : on ouvre la connexion & on se met prêt automatiquement
    LaunchedEffect(roomCode, isSolo) {
        if (isSolo) {
            roomSocket.openRoomConnection(roomCode)
            roomSocket.sendReadyFlag(true)
        }
    }

    val gameState by roomSocket.gameState.collectAsState()
    val playerBoardJson by roomSocket.playerBoard.collectAsState()
    val gameEndedJson by roomSocket.gameEnded.collectAsState()

    val endState = gameEndedJson

    // 🔹 Partie terminée : écran de fin
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
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = if (isSolo) "Rogue AI – Partie solo" else "Rogue AI – Partie multijoueur",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Room : $roomCode",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "État du jeu (backend) : $gameState",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

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
                threat?.let {
                    Text(
                        text = "Menace actuelle : $it / 100",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                instruction?.let { instr ->
                    val instrText = instr.optString("instruction_text", "Instruction inconnue")
                    val expectedStatus = instr.optString("expected_status", "")
                    val commandType = instr.optString("command_type", "")
                    val timeout = instr.optLong("timeout", 0L)

                    Text(
                        text = "Instruction actuelle :",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = instrText,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Type : $commandType – Attendu : $expectedStatus – Timeout : ${timeout}ms",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Grille de commandes (2 colonnes)
            if (commands.isNotEmpty()) {
                Text(
                    text = "Commandes disponibles :",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(commands) { cmd ->
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
                                roomSocket.sendExecuteAction(commandId, action)
                            }
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

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onLeave,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Quitter la partie et revenir à l'accueil")
            }
        }
    }
}

@Composable
private fun CommandCard(
    type: String,
    styleType: String,
    id: String,
    name: String,
    actualStatus: String,
    actions: List<String>,
    isHighlighted: Boolean,
    onExecuteAction: (commandId: String, action: String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
        ) {
            when (type) {
                "slider" -> {
                    SliderCommandView(
                        commandId = id,
                        name = name,
                        actualStatus = actualStatus,
                        actions = actions,
                        isHighlighted = isHighlighted,
                        onExecuteAction = onExecuteAction
                    )
                }

                "toggle" -> {
                    ToggleCommandView(
                        commandId = id,
                        name = name,
                        styleType = styleType,
                        actualStatus = actualStatus,
                        actions = actions,
                        isHighlighted = isHighlighted,
                        onExecuteAction = onExecuteAction
                    )
                }

                else -> {
                    ToggleCommandView(
                        commandId = id,
                        name = "$name (type: $type)",
                        styleType = styleType,
                        actualStatus = actualStatus,
                        actions = actions,
                        isHighlighted = isHighlighted,
                        onExecuteAction = onExecuteAction
                    )
                }
            }
        }
    }
}
