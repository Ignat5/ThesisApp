package com.mycompany.thesis1.mvvm.requests.active

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.mycompany.thesis1.constants.AppConstants
import com.mycompany.thesis1.constants.AppConstants.GROUPS_COLLECTION
import com.mycompany.thesis1.constants.AppConstants.GROUP_IDS
import com.mycompany.thesis1.constants.AppConstants.REQUESTS_COLLECTION
import com.mycompany.thesis1.constants.AppConstants.REQUEST_ACCEPTED
import com.mycompany.thesis1.constants.AppConstants.REQUEST_DENIED
import com.mycompany.thesis1.constants.AppConstants.REQUEST_INFO_FROM_EMAIL
import com.mycompany.thesis1.constants.AppConstants.REQUEST_INFO_TO_EMAIL
import com.mycompany.thesis1.constants.AppConstants.REQUEST_STATUS
import com.mycompany.thesis1.constants.AppConstants.USERS_COLLECTION
import com.mycompany.thesis1.mvvm.model.CommonResource
import com.mycompany.thesis1.mvvm.model.Group
import com.mycompany.thesis1.mvvm.model.Request
import com.mycompany.thesis1.utils.Utils
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class IncomingRequestsViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _incomingRequests = MutableLiveData<List<Request>>()
    val incomingRequests: LiveData<List<Request>> = _incomingRequests

    private val eventsChannel = Channel<CommonResource>()
    val eventsFlow = eventsChannel.receiveAsFlow()

    private var incomingRequestsListener: ListenerRegistration? = null

    init {
        viewModelScope.launch {
            start()
        }
    }

    private suspend fun start() {
        firebaseAuth.currentUser?.email?.let { currentUserId ->
            eventsChannel.send(CommonResource.ShowDownload())
            incomingRequestsListener = db.collection(REQUESTS_COLLECTION)
                .whereEqualTo(REQUEST_INFO_TO_EMAIL, currentUserId)
                .addSnapshotListener { value, error ->
                    if (error == null)
                        value?.documents?.let { documents ->
                            val localIncomingRequests = mutableListOf<Request>()
                            documents.forEach { document ->
                                document.toObject(Request::class.java)?.let {
                                    localIncomingRequests.add(it)
                                }
                            }
                            Log.d("myTag", "start: before sort: $localIncomingRequests")
                            localIncomingRequests.sortByDescending { it.status }
                            Log.d("myTag", "start: after sort: $localIncomingRequests")
                            _incomingRequests.postValue(localIncomingRequests)
                        }
                }
        }
    }

    fun onRequestAccepted(request: Request) {
        viewModelScope.launch {
            try {
                eventsChannel.send(CommonResource.ShowDownload())
                db.runBatch { batch ->
                    batch.update(
                        db.collection(USERS_COLLECTION)
                            .document(request.info.to.email),
                        GROUP_IDS,
                        FieldValue.arrayUnion(request.groupId)
                    )
                    batch.update(
                        db.collection(REQUESTS_COLLECTION)
                            .document(
                                Utils.getRequestId(
                                    request.info.from.email,
                                    request.info.to.email
                                )
                            ),
                        REQUEST_STATUS,
                        REQUEST_ACCEPTED
                    )
                }.await()
                eventsChannel.send(
                    CommonResource.Success("Запрос от пользователя ${request.info.from.name} успешно принят")
                )
            } catch (e: Exception) {
                eventsChannel.send(CommonResource.Error(e.message))
            }
        }
    }

    fun onRequestDenied(request: Request) {
        viewModelScope.launch {
            try {
                eventsChannel.send(CommonResource.ShowDownload())
                db.collection(REQUESTS_COLLECTION)
                    .document(Utils.getRequestId(request.info.from.email, request.info.to.email))
                    .update(
                        REQUEST_STATUS,
                        REQUEST_DENIED
                    ).await()
                eventsChannel.send(CommonResource.Success("Запрос от пользователя ${request.info.from.name} был отклонен"))
            } catch (e: Exception) {
                eventsChannel.send(CommonResource.Error(e.message))
            }
        }
    }

    fun onCancelAccepted(request: Request) {
        viewModelScope.launch {
            try {
                eventsChannel.send(CommonResource.ShowDownload())
                val localGroupsId = mutableListOf<String>()
                val task =
                    db.collection(USERS_COLLECTION).document(request.info.from.email).collection(
                        GROUPS_COLLECTION
                    ).get().await()
                task.documents.forEach {
                    it.toObject(Group::class.java)?.let { group ->
                        localGroupsId.add(group.groupId)
                    }
                }
                db.runBatch { batch ->
                    batch.update(
                        db.collection(USERS_COLLECTION).document(request.info.to.email),
                        GROUP_IDS,
                        FieldValue.arrayRemove(*localGroupsId.toTypedArray())
                    )
                    batch.update(
                        db.collection(REQUESTS_COLLECTION).document(
                            Utils.getRequestId(
                                request.info.from.email,
                                request.info.to.email
                            )
                        ),
                        REQUEST_STATUS,
                        REQUEST_DENIED
                    )
                }.await()
                eventsChannel.send(CommonResource.Success("Запрос от пользователя ${request.info.from.name} отклонен"))
            } catch (e: Exception) {
                eventsChannel.send(CommonResource.Error(e.message))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        incomingRequestsListener?.remove()
    }
}