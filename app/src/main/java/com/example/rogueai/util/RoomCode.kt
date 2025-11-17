package com.example.rogueai.util

import java.util.Locale

/**
 * Room code helpers: normalize and validate against server rules.
 * Format: exactly 6 chars, A-Z or 0-9 (uppercase)
 */

private val ROOM_CODE_REGEX = Regex("[A-Z0-9]{6}")

fun normalizeRoomCode(input: String): String =
    input
        .uppercase(Locale.ROOT)
        .filter { it.isLetterOrDigit() }

fun isValidRoomCode(input: String): Boolean =
    ROOM_CODE_REGEX.matches(normalizeRoomCode(input))
