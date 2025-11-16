package com.example.rogueai.model

/**
 * Models for Lobby screen.
 */

data class LobbyPlayer(
    val id: String,
    val displayName: String,
    val isReady: Boolean,
    val isHost: Boolean
)
