package com.mycompany.thesis1.mvvm.start

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class AuthViewModel: ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseUser = FirebaseAuth.getInstance().currentUser

    private val authChannel = Channel<AuthEvents>()
    val authFlow = authChannel.receiveAsFlow()

    init {
        Log.d("myTag", "init: currentUser: $firebaseUser")
        viewModelScope.launch {
            authChannel.send(AuthEvents.CheckPermissions)
        }
    }

    fun onAuthenticateClick(email: String?, password: String?) = viewModelScope.launch {
        if (!email.isNullOrBlank() && !password.isNullOrBlank()) {
            authChannel.send(AuthEvents.ShowLoading)
            try {
                val response = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                if (response != null) authChannel.send(AuthEvents.NavigateToMainScreen)
            } catch (e: Exception) {
                Log.d("myTag", "onAuthenticateClick: exception: ${e.message}")
                authChannel.send(AuthEvents.ShowErrorMessage("Аутентификация не удалась. Проверьте входные данные"))
            }
        } else
            authChannel.send(AuthEvents.ShowErrorMessage("Заполните все поля"))
    }

    fun onRegistrationClick() = viewModelScope.launch {
        authChannel.send(AuthEvents.NavigateToRegistrationScreen)
    }


    sealed class AuthEvents {
        object NavigateToMainScreen: AuthEvents()
        data class ShowErrorMessage(val message: String): AuthEvents()
        object ShowLoading: AuthEvents()
        object NavigateToRegistrationScreen: AuthEvents()
        object CheckPermissions: AuthEvents()
    }

}