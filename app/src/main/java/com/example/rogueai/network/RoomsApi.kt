package com.example.rogueai.network

import com.example.rogueai.model.CreateRoomResponse
import com.example.rogueai.model.RoomInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class RoomsApi(
    private val client: OkHttpClient = OkHttpClient(),
    private val baseUrl: String = "https://backend.rogueai.surpuissant.io"
) {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun createRoom(
        soloGame: Boolean
    ): CreateRoomResponse = withContext(Dispatchers.IO) {
        val bodyJson = JSONObject().apply {
            put("soloGame", soloGame)
        }

        val body = bodyJson.toString().toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url("$baseUrl/create-room")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("createRoom failed: HTTP ${response.code}")
            }

            val responseBody = response.body?.string()
                ?: throw IOException("Empty body from create-room")

            val obj = JSONObject(responseBody)
            val roomCode = obj.getString("roomCode")

            val roomInfoJson = obj.getJSONObject("roomInfo")
            val roomInfo = RoomInfo(
                minPlayer = roomInfoJson.getInt("minPlayer"),
                maxPlayer = roomInfoJson.getInt("maxPlayer"),
                gameDuration = roomInfoJson.getLong("gameDuration"),
                roomRestriction = roomInfoJson.getString("roomRestriction")
            )

            CreateRoomResponse(
                roomCode = roomCode,
                roomInfo = roomInfo
            )
        }
    }

    suspend fun roomExists(code: String): Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/room-exists/$code")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("roomExists failed: HTTP ${response.code}")
            }

            val responseBody = response.body?.string()
                ?: throw IOException("Empty body from room-exists")

            val obj = JSONObject(responseBody)
            obj.getBoolean("exists")
        }
    }
}
