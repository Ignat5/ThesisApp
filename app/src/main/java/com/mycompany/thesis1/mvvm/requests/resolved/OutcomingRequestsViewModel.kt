package com.mycompany.thesis1.mvvm.requests.resolved

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.mycompany.thesis1.constants.AppConstants
import com.mycompany.thesis1.mvvm.model.CommonResource
import com.mycompany.thesis1.mvvm.model.Request
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class OutcomingRequestsViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _outcomingRequests = MutableLiveData<List<Request>>()
    val outcomingRequests: LiveData<List<Request>> = _outcomingRequests

    private val eventsChannel = Channel<CommonResource>()
    val eventsFlow = eventsChannel.receiveAsFlow()

    private var outcomingRequestsListener: ListenerRegistration? = null

    init {
        viewModelScope.launch {
            start()
        }
    }

    private suspend fun start() {
        firebaseAuth.currentUser?.email?.let { currentUserId ->
            eventsChannel.send(CommonResource.ShowDownload())
            outcomingRequestsListener = db.collection(AppConstants.REQUESTS_COLLECTION)
                .whereEqualTo(AppConstants.REQUEST_INFO_FROM_EMAIL, currentUserId)
                .addSnapshotListener { value, error ->
                    if (error == null)
                        value?.documents?.let { documents ->
                            val localIncomingRequests = mutableListOf<Request>()
                            documents.forEach { document ->
                                document.toObject(Request::class.java)?.let {
                                    localIncomingRequests.add(it)
                                }
                            }
                            _outcomingRequests.postValue(localIncomingRequests.sortedByDescending { it.status })
                        }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        outcomingRequestsListener?.remove()
    }
}