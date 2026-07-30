package com.rogue.brainrottracker.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val name: String,
    val pictureUrl: String? = null
)
