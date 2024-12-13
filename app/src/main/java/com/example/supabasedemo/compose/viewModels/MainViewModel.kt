package com.example.supabasedemo.compose.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.data.network.SupabaseAuthHelper
import com.example.supabasedemo.data.network.SupabaseDbHelper
import com.example.supabasedemo.data.network.SupabaseRealtimeHelper

class MainViewModel(
    context: Context,
    setState: (state: UserState) -> Unit
) : ViewModel() {
    val supabaseAuth: SupabaseAuthHelper =
        SupabaseAuthHelper(viewModelScope, setState = { setState(it) }, context)
    val supabaseDb: SupabaseDbHelper =
        SupabaseDbHelper(setState = { setState(it) })
    val supabaseRealtime: SupabaseRealtimeHelper =
        SupabaseRealtimeHelper(viewModelScope, setState = { setState(it) }, context)
}