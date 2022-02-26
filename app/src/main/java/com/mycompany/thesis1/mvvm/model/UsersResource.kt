package com.mycompany.thesis1.mvvm.model

sealed class UsersResource(
    val data: List<User>? = null,
    val updatedUsers: List<User>? = null,
    val currentGroup: Group? = null,
    val message: String? = null
) {
    class SuccessAllUsers(data: List<User>): UsersResource(data = data)
    class SuccessNewUser(data: List<User>, updatedUsers: List<User>): UsersResource(data = data, updatedUsers = updatedUsers)
    class SuccessUpdateUser(data: List<User>, updatedUsers: List<User>): UsersResource(data = data, updatedUsers = updatedUsers)
    class SuccessDeleteUser(data: List<User>, updatedUsers: List<User>): UsersResource(data = data, updatedUsers = updatedUsers)
    class Loading(): UsersResource()
    class Error(message: String): UsersResource(message = message)
}