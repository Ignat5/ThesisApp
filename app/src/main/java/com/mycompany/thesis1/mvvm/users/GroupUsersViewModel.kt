package com.mycompany.thesis1.mvvm.users

import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.mycompany.thesis1.constants.AppConstants.GROUP_IDS
import com.mycompany.thesis1.constants.AppConstants.USERS_COLLECTION
import com.mycompany.thesis1.mvvm.model.CommonResource
import com.mycompany.thesis1.mvvm.model.User
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class GroupUsersViewModel(val state: SavedStateHandle) : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var groupId: String? = null
    private var listOfUsers = mutableListOf<User>()

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    private val eventsChannel = Channel<CommonResource>()
    val eventsFlow = eventsChannel.receiveAsFlow()
    private var usersListener: ListenerRegistration? = null

    init {
        viewModelScope.launch {
            start()
        }
    }

    suspend fun start() {
        groupId = state.get<String>("groupId")
        Log.d("myTag", "start: groupId: $groupId")
        firebaseAuth.currentUser?.email?.let { currentUserId ->
            groupId?.let {
                try {
                    eventsChannel.send(CommonResource.ShowDownload())
                    usersListener = db.collection(USERS_COLLECTION)
                        .whereArrayContains(GROUP_IDS, it)
                        .addSnapshotListener { value, error ->
                            if (error != null) throw FirebaseFirestoreException(
                                error.message!!,
                                error.code
                            )
                            listOfUsers.clear()
                            value?.documents?.let { documents ->
                                for (document in documents) {
                                    val user = document?.toObject(User::class.java)
                                    user?.let {
                                        if (user.userId != currentUserId)
                                            listOfUsers.add(user)
                                    }
                                    Log.d("myTag", "start: user: ${user?.userName}")
                                }
                                _users.postValue(listOfUsers)
                            }
                        }
                } catch (e: Exception) {
                    eventsChannel.send(CommonResource.Error(e.message))
                }
            }
        }
    }

    fun deleteUserFromGroup(userId: String) {
        viewModelScope.launch {
            groupId?.let {
                try {
                    eventsChannel.send(CommonResource.ShowDownload())
                    db.collection(USERS_COLLECTION).document(userId)
                        .update(GROUP_IDS, FieldValue.arrayRemove(it))
                        .await()
                    eventsChannel.send(CommonResource.ShowMessage("Пользователь $userId удален из группы"))
                } catch (e: Exception) {
                    eventsChannel.send(CommonResource.Error(e.message))
                }
            }
        }
    }

    fun getCurrentGroupId() = groupId

    override fun onCleared() {
        super.onCleared()
        usersListener?.remove()
    }
}