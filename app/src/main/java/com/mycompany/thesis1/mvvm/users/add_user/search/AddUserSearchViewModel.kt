package com.mycompany.thesis1.mvvm.users.add_user.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mycompany.thesis1.constants.AppConstants.REQUESTS_COLLECTION
import com.mycompany.thesis1.constants.AppConstants.REQUEST_INFO_FROM_EMAIL
import com.mycompany.thesis1.constants.AppConstants.REQUEST_INFO_TO_EMAIL
import com.mycompany.thesis1.constants.AppConstants.REQUEST_ACCEPTED
import com.mycompany.thesis1.constants.AppConstants.REQUEST_DENIED
import com.mycompany.thesis1.constants.AppConstants.REQUEST_WAITING
import com.mycompany.thesis1.constants.AppConstants.USERS_COLLECTION
import com.mycompany.thesis1.mvvm.map.MainFragment
import com.mycompany.thesis1.mvvm.model.*
import com.mycompany.thesis1.utils.Utils
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AddUserSearchViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _searchedUser = MutableLiveData<User?>()
    val searchedUser: LiveData<User?> = _searchedUser

    private val eventsChannel = Channel<CommonResource>()
    val eventsFlow = eventsChannel.receiveAsFlow()

    fun searchUser(userId: String) {
        viewModelScope.launch {
            firebaseAuth.currentUser?.email?.let { currentUserId ->
                try {
                    eventsChannel.send(CommonResource.ShowDownload())
                    val task = db.collection(USERS_COLLECTION).document(userId).get().await()
                    task?.let {
                        if (it.exists())
                            it.toObject(User::class.java)?.let { user ->
                                Log.d("myTag", "searchUser: exists=true: $user")
                                if (user.userId != currentUserId)
                                    _searchedUser.postValue(user)
                                else
                                    _searchedUser.postValue(null)
                            }
                        else
                            _searchedUser.postValue(null)
                    }
                } catch (e: Exception) {
                    Log.d("myTag", "searchUser: error: ${e.message}")
                    eventsChannel.send(CommonResource.Error(e.message))
                }
            }
        }
    }

    fun sendRequest(user: User, groupId: String) {
        viewModelScope.launch {
            firebaseAuth.currentUser?.email?.let { currentUserId ->
                try {
                    eventsChannel.send(CommonResource.ShowDownload())
                    val task = db.collection(REQUESTS_COLLECTION)
                        .whereEqualTo(REQUEST_INFO_FROM_EMAIL, currentUserId)
                        .whereEqualTo(REQUEST_INFO_TO_EMAIL, user.userId)
                        .get().await()
                    task.documents.let { documents ->
                        if (documents.isEmpty())
                            createRequest(
                                Request(
                                    info = Info(
                                        from = InfoUser(currentUserId, MainFragment.CURRENT_USER_NAME),
                                        to = InfoUser(user.userId, user.userName)
                                    ),
                                    groupId = groupId,
                                    status = REQUEST_WAITING
                                )
                            )
                        else {
                            documents[0].toObject(Request::class.java)?.let { request ->
                                when (request.status) {
                                    REQUEST_ACCEPTED ->
                                        eventsChannel.send(CommonResource.ShowMessage(
                                            "Данный пользователь уже принял ваш запрос"
                                        ))
                                    REQUEST_WAITING ->
                                        eventsChannel.send(CommonResource.ShowMessage(
                                            "Данный пользователь еще не ответил на ваш прошлый запрос"
                                        ))
                                    REQUEST_DENIED -> createRequest(
                                        Request(
                                            info = Info(
                                                from = InfoUser(currentUserId, MainFragment.CURRENT_USER_NAME),
                                                to = InfoUser(user.userId, user.userName)
                                            ),
                                            groupId = groupId,
                                            status = REQUEST_WAITING
                                        )
                                    )
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d("myTag", "sendRequest: error: ${e.message}")
                    eventsChannel.send(CommonResource.Error(e.message))
                }
            }
        }
    }

    private suspend fun createRequest(request: Request) {
        Utils.getRequestId(request.info.from.email, request.info.to.email).let { requestId ->
            try {
                db.collection(REQUESTS_COLLECTION).document(requestId).set(request).await()
                eventsChannel.send(CommonResource.Success("Запрос успешно отправлен"))
            } catch (e: Exception) {
                eventsChannel.send(CommonResource.Error(e.message))
            }
        }
    }

}