package com.example.supabasedemo.data.network

import android.content.Context
import android.util.Log
import com.example.supabasedemo.data.model.Game
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.data.network.SupabaseClient.client
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject

class SupabaseDbHelper(
    private val scope: CoroutineScope,
    val setState: (UserState) -> Unit,
    private val context: Context
) {
    fun joinGameInSupabase(
        gameUuid: String,
        onGameJoined: (Game) -> Unit,
        onError: (String) -> Unit,
        currentUser: JsonObject?
    ) {
        val user2Uuid = currentUser?.get("sub").toString().trim().replace("\"", "")

        scope.launch {
            try {
                val updatedGame =
                    client.from("games").update(
                        {
                            Game::user2 setTo user2Uuid
                        }
                    ) {
                        select()
                        filter {
                            Game::uuid eq gameUuid
                        }
                    }.decodeSingle<Game>()

                onGameJoined(updatedGame)
            } catch (e: Exception) {
                onError(e.message ?: "Unexpected error occurred.")
            }
        }
    }

    fun createGameInSupabase(
        gameUuid: String,
        onGameCreated: (Game) -> Unit,
        onError: (String) -> Unit,
        currentUser: JsonObject?
    ) {
        val gameData = Game(
            uuid = gameUuid,
            user1 = currentUser?.get("sub").toString().trim().replace("\"", ""),
        )

        scope.launch {
            try {
                val createdGame = client.from("games")
                    .insert(gameData) {
                        select()
                    }.decodeSingle<Game>()

                onGameCreated(createdGame)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.d("Supabase", "catch (e: Exception: $e")
                    onError(e.message ?: "Unexpected error occurred.")
                }
            }
        }
    }
}