package com.example.rogueai.util

/**
 * Room code helpers: normalize and validate against server format
 * Format: exactly 6 chars, A-Z or 0-9 (uppercase)
 */

private val ROOM_CODE_REGEX = Regex(pattern = "[A-Z0-9]{6}")

fun normalizeRoomCode(input: String): String =
    input.uppercase().filter { it.isLetterOrDigit() }

fun isValidRoomCode(input: String): Boolean =
    ROOM_CODE_REGEX.matches(normalizeRoomCode(input))
