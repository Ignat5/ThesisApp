package com.mycompany.thesis1.mvvm.model

import com.google.firebase.Timestamp

data class User(
    val userId: String = "",
    val userName: String = "",
    val groupIDs: List<String> = listOf(),
    val latitude: Double = -1.0,
    val longitude: Double = -1.0,
    var updatedAt: Timestamp? = null
)