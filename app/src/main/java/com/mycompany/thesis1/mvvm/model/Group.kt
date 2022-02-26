package com.mycompany.thesis1.mvvm.model

data class Group(
    val groupId: String = "",
    val groupName: String = "",
    @field:JvmField
    var isCurrent: Boolean = false
)