package com.example.rogueai.model

data class RoomInfo(
    val minPlayer: Int,
    val maxPlayer: Int,
    val gameDuration: Long,
    val roomRestriction: String
)

data class CreateRoomResponse(
    val roomCode: String,
    val roomInfo: RoomInfo
)
