package com.example.rogueai.ui.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.rogueai.network.RoomSocket

/**
 * Version solo du jeu :
 * - on ouvre la connexion WS sur la room
 * - on envoie ready = true
 * - on réutilise MultiGameScreen
 */
@Composable
fun SoloGameScreen(
    roomCode: String,
    roomSocket: RoomSocket,
    onLeave: () -> Unit
) {
    // On connecte le WebSocket et on se met prêt automatiquement
    LaunchedEffect(roomCode) {
        roomSocket.openRoomConnection(roomCode)
        // pour être sûr de déclencher la partie si le backend en a besoin
        roomSocket.sendReadyFlag(true)
    }

    MultiGameScreen(
        roomCode = roomCode,
        roomSocket = roomSocket,
        onLeave = {
            // on ferme proprement la connexion puis on remonte l’info à MainActivity
            roomSocket.resetAfterGameEnd()
            roomSocket.closeRoomConnection()
            onLeave()
        }
    )
}
