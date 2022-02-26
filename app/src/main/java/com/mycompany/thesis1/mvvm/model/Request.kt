package com.mycompany.thesis1.mvvm.model

data class Request(
    val info: Info = Info(),
    val groupId: String = "",
    val status: String = ""
)

data class Info(
    val from: InfoUser = InfoUser(),
    val to: InfoUser = InfoUser()
)

data class InfoUser(
    val email: String = "",
    val name: String = ""
)
