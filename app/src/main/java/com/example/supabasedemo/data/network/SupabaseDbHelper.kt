package com.example.supabasedemo.data.network

import android.util.Log
import com.example.supabasedemo.compose.views.Reading
import com.example.supabasedemo.data.model.Game
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.data.network.SupabaseClient.client
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

class SupabaseDbHelper(
//    private val scope: CoroutineScope,
    val setState: (UserState) -> Unit,
//    private val context: Context
) {
    fun joinGameInSupabase(
        gameUuid: String,
        onGameJoined: (Game) -> Unit,
        onError: (String) -> Unit,
        currentUser: JsonObject?
    ) {
        val user2Uuid = currentUser?.get("sub").toString().trim().replace("\"", "")

        runBlocking {
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
    ): Game {
        val gameData = Game(
            uuid = gameUuid,
            user1 = currentUser?.get("sub").toString().trim().replace("\"", ""),
        )

        runBlocking {
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

        return gameData
    }

    fun sendReadingToDb(
        distance: Double,
        angle: Double,
        stDev: Double,
        accelerometer: Reading,
        gyroscope: Reading,
        compass: Reading,
        isFront: Boolean
    ) {
        CoroutineScope(Dispatchers.Main.immediate).launch {
            try{
                client.from("readings").insert(TestData(
                    distance = distance,
                    angle = angle,
                    st_dev = stDev,
                    acc_x = accelerometer.x.toDouble(),
                    acc_y = accelerometer.y.toDouble(),
                    acc_z = accelerometer.z.toDouble(),
                    gyr_x = gyroscope.x.toDouble(),
                    gyr_y = gyroscope.y.toDouble(),
                    gyr_z = gyroscope.z.toDouble(),
                    com_x = compass.x.toDouble(),
                    com_y = compass.y.toDouble(),
                    com_z = compass.z.toDouble(),
                    is_front = isFront
                ))
            } catch (_: Exception) {

            }
        }
    }
}

@Serializable
class TestData(
    val distance: Double,
    val angle: Double,
    val st_dev: Double,
    val acc_x: Double,
    val acc_y: Double,
    val acc_z: Double,
    val gyr_x: Double,
    val gyr_y: Double,
    val gyr_z: Double,
    val com_x: Double,
    val com_y: Double,
    val com_z: Double,
    val is_front: Boolean
)