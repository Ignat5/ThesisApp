package com.mycompany.thesis1.mvvm.start

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mycompany.thesis1.constants.AppConstants.DEF_LATITUDE
import com.mycompany.thesis1.constants.AppConstants.DEF_LONGITUDE
import com.mycompany.thesis1.constants.AppConstants.GROUPS_COLLECTION
import com.mycompany.thesis1.constants.AppConstants.GROUP_ID
import com.mycompany.thesis1.constants.AppConstants.GROUP_IDS
import com.mycompany.thesis1.constants.AppConstants.GROUP_NAME
import com.mycompany.thesis1.constants.AppConstants.IS_CURRENT
import com.mycompany.thesis1.constants.AppConstants.LATITUDE
import com.mycompany.thesis1.constants.AppConstants.LONGITUDE
import com.mycompany.thesis1.constants.AppConstants.UPDATED_AT
import com.mycompany.thesis1.constants.AppConstants.USERS_COLLECTION
import com.mycompany.thesis1.constants.AppConstants.USER_ID
import com.mycompany.thesis1.constants.AppConstants.USER_NAME
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class RegViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val registrationChannel = Channel<RegistrationEvents>()
    val registrationEventFlow = registrationChannel.receiveAsFlow()

    fun onRegisterClick(name: String?, email: String?, password: String?) = viewModelScope.launch {
        if (!email.isNullOrBlank() && !password.isNullOrBlank() && !name.isNullOrBlank()) {
            registrationChannel.send(RegistrationEvents.ShowLoading)
            try {
                val response =
                    firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val groupId = System.currentTimeMillis().toString()
                val map = hashMapOf(
                    GROUP_NAME to "Моя группа",
                    GROUP_ID to groupId,
                    IS_CURRENT to true
                )
                val latitude: Double = DEF_LATITUDE
                val longitude: Double = DEF_LONGITUDE
                val userMap = hashMapOf(
                    USER_NAME to name,
                    USER_ID to email,
                    LATITUDE to latitude,
                    LONGITUDE to longitude,
                    UPDATED_AT to Timestamp(Date()),
                    GROUP_IDS to listOf(groupId)
                )
                db.runBatch { batch ->
                    batch.set(
                        db.collection(USERS_COLLECTION).document(email)
                            .collection(GROUPS_COLLECTION).document(groupId),
                        map
                    )
                    batch.set(
                        db.collection(USERS_COLLECTION).document(email),
                        userMap
                    )
                }.await()
                if (response != null) registrationChannel.send(RegistrationEvents.NavigateToMainScreen)
            } catch (e: Exception) {
                val message = e.message ?: "no message"
                registrationChannel.send(RegistrationEvents.ShowErrorMessage("Некорректные входные данные"))
                Log.d("myTag", "onRegisterClick: error: $message")
            }
        } else
            registrationChannel.send(RegistrationEvents.ShowErrorMessage("Заполните все поля"))
    }

    sealed class RegistrationEvents {
        data class ShowErrorMessage(val message: String) : RegistrationEvents()
        object NavigateToMainScreen : RegistrationEvents()
        object ShowLoading : RegistrationEvents()
    }
}