package com.example.supabasedemo.data.network

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.example.supabasedemo.data.model.Game
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.data.network.SupabaseClient.client
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.selectSingleValueAsFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SupabaseRealtimeHelper(
    private val scope: CoroutineScope,
    val setState: (UserState) -> Unit,
    private val context: Context
) {
    @OptIn(SupabaseExperimental::class)
    fun subscribeToGame(uuid: String, setSubscribedObject: (game: Game) -> Unit) {
        val flow: Flow<Game> = client.from("games").selectSingleValueAsFlow(Game::uuid) {
            Game::uuid eq uuid
        }
        scope.launch {
            flow.collect {
                setSubscribedObject(it)
                Log.e("realtime", it.uuid)
            }
        }
    }
}