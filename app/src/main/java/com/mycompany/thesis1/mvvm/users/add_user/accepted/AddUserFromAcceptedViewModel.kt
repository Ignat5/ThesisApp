package com.mycompany.thesis1.mvvm.users.add_user.accepted

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.mycompany.thesis1.constants.AppConstants.GROUP_IDS
import com.mycompany.thesis1.constants.AppConstants.REQUESTS_COLLECTION
import com.mycompany.thesis1.constants.AppConstants.REQUEST_ACCEPTED
import com.mycompany.thesis1.constants.AppConstants.REQUEST_INFO_FROM_EMAIL
import com.mycompany.thesis1.constants.AppConstants.REQUEST_STATUS
import com.mycompany.thesis1.constants.AppConstants.USERS_COLLECTION
import com.mycompany.thesis1.mvvm.model.CommonResource
import com.mycompany.thesis1.mvvm.model.Request
import com.mycompany.thesis1.mvvm.model.User
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AddUserFromAcceptedViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val eventsChannel = Channel<CommonResource>()
    val eventsFlow = eventsChannel.receiveAsFlow()

    private val _users = MutableLiveData<List<Pair<Request, Boolean>>>()
    val users: LiveData<List<Pair<Request, Boolean>>> = _users

    private val localData = mutableListOf<Pair<Request, Boolean>>()

    fun start(groupId: String) = viewModelScope.launch {
        firebaseAuth.currentUser?.email?.let { userId ->
            try {
                eventsChannel.send(CommonResource.ShowDownload())
                val task1 = db.collection(USERS_COLLECTION)
                    .whereArrayContains(GROUP_IDS, groupId)
                    .get()
                    .await()

                val localUsers = mutableListOf<User>()
                task1.documents.let { documents ->
                    for (document in documents) {
                        document.toObject(User::class.java)?.let {
                            localUsers.add(it)
                        }
                    }
                }

                val task2 = db.collection(REQUESTS_COLLECTION)
                    .whereEqualTo(REQUEST_INFO_FROM_EMAIL, userId)
                    .whereEqualTo(REQUEST_STATUS, REQUEST_ACCEPTED)
                    .get()
                    .await()

                task2.documents.let { documents ->
                    for (document in documents) {
                        document.toObject(Request::class.java)?.let { request ->
                            val user = localUsers.find { it.userId == request.info.to.email }
                            localData.add(
                                Pair(request, user != null)
                            )
                        }
                    }
                    _users.postValue(localData)
                }

            } catch (e: Exception) {
                eventsChannel.send(CommonResource.Error(e.message))
            }
        }
    }

    fun addToGroup(groupId: String, usersId: List<String>) = viewModelScope.launch {
        eventsChannel.send(CommonResource.ShowDownload())
        try {
            db.runBatch { batch ->
                usersId.forEach { id ->
                    batch.update(
                        db.collection(USERS_COLLECTION).document(id),
                        GROUP_IDS,
                        FieldValue.arrayUnion(groupId)
                    )
                }
            }.await()

            usersId.forEach { id ->
                val index = localData.indexOfFirst { it.first.info.to.email == id }
                if (index != -1) {
                    val request = localData.get(index).first
                    val pair = Pair(request, true)
                    localData[index] = pair
                }
            }
            _users.postValue(localData)
            if (usersId.size > 1)
                eventsChannel.send(CommonResource.Success("Пользователи успешно добавлены в группу"))
            if (usersId.size == 1)
                eventsChannel.send(CommonResource.Success("Пользователь успешно добавлен в группу"))
        } catch (e: Exception) {
            Log.d("myTag", "addToGroup error: ${e.message}")
            eventsChannel.send(CommonResource.Error(e.message))
        }
    }
}