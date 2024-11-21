package com.example.supabasedemo.compose.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.data.network.SupabaseHelper

class MainViewModel(
    context: Context,
    setState: (state: UserState) -> Unit
    ) : ViewModel() {
    val supabase: SupabaseHelper = SupabaseHelper(viewModelScope, setState = { setState(it) } , context)
}