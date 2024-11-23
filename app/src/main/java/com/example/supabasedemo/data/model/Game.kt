package com.example.supabasedemo.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Game(
    val id: Long? = null,
    val uuid: String,
    val startTime: String? = null,
    val endTime: String? = null,
    val roundNo: Short = 0,
    val user1: String?,
    val user2: String? = null,
    val won: Boolean? = null
)