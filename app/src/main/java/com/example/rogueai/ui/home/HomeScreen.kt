package com.example.rogueai.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.rogueai.network.RoomsApi
import com.example.rogueai.ui.theme.RoguePalette
import com.example.rogueai.ui.theme.RogueAccentButton
import com.example.rogueai.ui.theme.RogueChipTitle
import com.example.rogueai.ui.theme.RogueMainCard
import com.example.rogueai.ui.theme.RoguePrimaryButton
import com.example.rogueai.ui.theme.RogueSecondaryButton
import com.example.rogueai.ui.theme.rogueBackground
import com.example.rogueai.util.isValidRoomCode
import com.example.rogueai.util.normalizeRoomCode
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    roomsApi: RoomsApi,
    onSoloRoomCreated: (String) -> Unit,
    onMultiRoomCreated: (String) -> Unit,
    onRoomJoined: (String) -> Unit
) {
    var isLoadingCreate by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showJoinDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .rogueBackground()
            .padding(24.dp)
    ) {
        RogueMainCard(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RogueChipTitle()

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Prépare ton équipage",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Crée une partie ou rejoins une IA déjà en train de hurler sur quelqu’un 🤖",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFFDEE7FF)
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // SOLO
                RoguePrimaryButton(
                    text = if (isLoadingCreate) "Création en cours…" else "Jouer seul",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoadingCreate,
                    onClick = {
                        isLoadingCreate = true
                        errorMessage = null

                        scope.launch {
                            try {
                                val response = roomsApi.createRoom(
                                    soloGame = true
                                )
                                onSoloRoomCreated(response.roomCode)
                            } catch (t: Throwable) {
                                errorMessage = t.message
                                    ?: "Erreur inconnue lors de la création de la room solo"
                            } finally {
                                isLoadingCreate = false
                            }
                        }
                    },
                    leadingContent = if (isLoadingCreate) {
                        {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .height(18.dp)
                                    .padding(end = 8.dp),
                                strokeWidth = 2.dp,
                                color = RoguePalette.ButtonYellowText
                            )
                        }
                    } else null
                )

                Spacer(modifier = Modifier.height(12.dp))

                // MULTI
                RogueSecondaryButton(
                    text = "Créer une partie multijoueur",
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoadingCreate,
                    onClick = {
                        isLoadingCreate = true
                        errorMessage = null

                        scope.launch {
                            try {
                                val response = roomsApi.createRoom(
                                    soloGame = false
                                )
                                onMultiRoomCreated(response.roomCode)
                            } catch (t: Throwable) {
                                errorMessage = t.message
                                    ?: "Erreur inconnue lors de la création de la room multi"
                            } finally {
                                isLoadingCreate = false
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // JOIN
                RogueAccentButton(
                    text = "Rejoindre une partie",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        errorMessage = null
                        showJoinDialog = true
                    }
                )
            }
        }

        if (showJoinDialog) {
            JoinGameDialog(
                roomsApi = roomsApi,
                onDismiss = { showJoinDialog = false },
                onRoomJoined = { code ->
                    showJoinDialog = false
                    onRoomJoined(code)
                }
            )
        }
    }
}

@Composable
private fun JoinGameDialog(
    roomsApi: RoomsApi,
    onDismiss: () -> Unit,
    onRoomJoined: (String) -> Unit
) {
    var rawCode by remember { mutableStateOf("") }
    var isChecking by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = {
            if (!isChecking) onDismiss()
        },
        title = {
            Text(
                text = "🔑 Rejoindre une partie",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column {
                Text(
                    text = "Entre le code de la room (6 caractères, lettres ou chiffres).",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = rawCode,
                    onValueChange = { new ->
                        rawCode = new
                        localError = null
                    },
                    singleLine = true,
                    label = { Text("Code de room") }
                )
                if (localError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = localError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isChecking,
                onClick = {
                    val normalized = normalizeRoomCode(rawCode)

                    if (!isValidRoomCode(normalized)) {
                        localError =
                            "Le code doit contenir exactement 6 caractères alphanumériques."
                        return@TextButton
                    }

                    isChecking = true
                    localError = null

                    scope.launch {
                        try {
                            val exists = roomsApi.roomExists(normalized)
                            if (exists) {
                                onRoomJoined(normalized)
                            } else {
                                localError = "Aucune room trouvée avec ce code."
                            }
                        } catch (t: Throwable) {
                            localError =
                                t.message ?: "Erreur lors de la vérification de la room."
                        } finally {
                            isChecking = false
                        }
                    }
                }
            ) {
                if (isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Rejoindre")
                }
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isChecking,
                onClick = { onDismiss() }
            ) {
                Text("Annuler")
            }
        }
    )
}
