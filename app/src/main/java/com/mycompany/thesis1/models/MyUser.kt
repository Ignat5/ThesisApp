package com.mycompany.thesis1.models

data class MyUser(
    val userName: String = "default",
    val groupIDs: List<String> = listOf()
)