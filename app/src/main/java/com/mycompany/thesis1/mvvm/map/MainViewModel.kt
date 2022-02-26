package com.mycompany.thesis1.mvvm.map

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.mycompany.thesis1.constants.AppConstants.GROUPS_COLLECTION
import com.mycompany.thesis1.constants.AppConstants.GROUP_IDS
import com.mycompany.thesis1.constants.AppConstants.IS_CURRENT
import com.mycompany.thesis1.constants.AppConstants.USERS_COLLECTION
import com.mycompany.thesis1.mvvm.model.Group
import com.mycompany.thesis1.mvvm.model.User
import com.mycompany.thesis1.mvvm.model.UsersResource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class MainViewModel : ViewModel() {

    private val mainEventChannel = Channel<MainEvents>()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val mainEventFlow = mainEventChannel.receiveAsFlow()
    private val _usersResource = MutableLiveData<UsersResource>()
    val usersResource: LiveData<UsersResource> = _usersResource

    private val _currentGroup = MutableLiveData<Group>()
    val currentGroup: LiveData<Group> = _currentGroup

    private val listOfGroups = mutableListOf<Group>()
    private val listOfUsers = mutableListOf<User>()

    private var usersListener: ListenerRegistration? = null
    private var groupsListener: ListenerRegistration? = null
    var isMyFirstUpdate = true
    var profileId: String? = null

    //private var currentGroup: Group? = null
    private var isFirstQuery = true

    init {
        viewModelScope.launch {
            start()
        }
    }

    private suspend fun start() {
        firebaseAuth.currentUser?.email?.let { userId ->
            profileId = userId
            try {
                groupsListener?.remove()
                groupsListener =
                    db.collection(USERS_COLLECTION).document(userId).collection(GROUPS_COLLECTION)
                        .addSnapshotListener { value, error ->
                            if (error != null) throw FirebaseFirestoreException(
                                error.message!!,
                                error.code
                            )
                            listOfGroups.clear()
                            value?.documents?.let { documents ->
                                for (document in documents) {
                                    document.toObject(Group::class.java)?.let { group ->
                                        listOfGroups.add(group)
                                        if (group.isCurrent) _currentGroup.postValue(group)
                                        Log.d(
                                            "myTag",
                                            "start: group: ${group.groupName}, ${group.groupId} ,${group.isCurrent}"
                                        )
                                    }
                                }
                                if (isFirstQuery) {
                                    Log.d(TAG, "start: isFirstQuery: true")
                                    viewModelScope.launch { getUsersByGroupId() }
                                }
//                        else {
//                            listOfGroups.find { group -> group.isCurrent }?.let { group ->
//                                Log.d(TAG, "start: isFirstQuery: false, currentGroup: ${group.groupName}")
//                                _usersResource.postValue(UsersResource.SuccessAllUsers(
//                                    data = listOfUsers,
//                                    currentGroup = group
//                                ))
//                            }
//                        }
                            }
                        }
//                val snapshot =
//                    db.collection(USERS_COLLECTION).document(userId).collection(GROUPS_COLLECTION).get().await()
//                snapshot?.documents?.let { groups ->
//                    for (item in groups) {
//                        item.toObject(Group::class.java)?.also { group ->
//                            Log.d(
//                                TAG,
//                                "viewModel: group: id: ${group.groupId}, name: ${group.groupName}, isCurrent: ${group.isCurrent}"
//                            )
//                            listOfGroups.add(group)
//                            //if (group.isCurrent) currentGroup = group
//                        }
//                    }
//                }
            } catch (e: Exception) {
                Log.d(TAG, "viewModel error: ${e.message}")
                _usersResource.postValue(UsersResource.Error("Exception, message: ${e.message}"))
            }
        }
    }

    fun onCurrentGroupChanged(groupId: String) {
        viewModelScope.launch {
            getUsersByGroupId(groupId)
        }
    }

    private suspend fun getUsersByGroupId(groupId: String = "") {
        var oldGroupId = ""
        var newGroupId = ""
        if (groupId.isEmpty()) {
            newGroupId = (listOfGroups.find { it.isCurrent })?.groupId ?: ""
            Log.d(TAG, "getUsersByGroupId: first: true, newGroupId: $newGroupId")
        }
        else {
            oldGroupId = _currentGroup.value?.groupId ?: ""
            (listOfGroups.find { it.isCurrent })?.isCurrent = false
            (listOfGroups.find { group -> group.groupId == groupId })?.let {
                it.isCurrent = true
                newGroupId = it.groupId
                Log.d(TAG, "getUsersByGroupId: first: false, oldGroupId: $oldGroupId, newGroupId: $newGroupId")
                _currentGroup.postValue(it)
            }
        }
        try {
            usersListener?.remove()
            isFirstQuery = true
            isMyFirstUpdate = true
            usersListener = db.collection(USERS_COLLECTION)
                .whereArrayContains(GROUP_IDS, newGroupId)
                .addSnapshotListener { value, error ->
                    if (error != null) throw FirebaseFirestoreException(
                        error.message ?: "my unknown error", error.code
                    )
                    value?.documentChanges?.let { collectionChanges ->
                        dbDataChangeCallback(collectionChanges)
                    }
                }
            if (groupId.isNotEmpty()) {
                val oldGroupDoc = db.collection(USERS_COLLECTION)
                    .document(profileId!!)
                    .collection(GROUPS_COLLECTION)
                    .document(oldGroupId)

                val newGroupDoc = db.collection(USERS_COLLECTION)
                    .document(profileId!!)
                    .collection(GROUPS_COLLECTION)
                    .document(newGroupId)

                db.runBatch { batch ->
                    Log.d(TAG, "getUsersByGroupId: batch start, old: $oldGroupId, new: $newGroupId")
                    batch.update(
                        oldGroupDoc, mutableMapOf<String, Any>(
                            IS_CURRENT to false
                        )
                    )
                    batch.update(
                        newGroupDoc, mutableMapOf<String, Any>(
                            IS_CURRENT to true
                        )
                    )
                }.await()
                Log.d(TAG, "getUsersByGroupId: batch end")
            }

        } catch (e: Exception) {
            Log.d(TAG, "viewModel error: ${e.message}")
            _usersResource.postValue(UsersResource.Error("Exception, message: ${e.message}"))
        }
    }

    private fun dbDataChangeCallback(
        collectionChanges: List<DocumentChange>,
    ) {
        Log.d(TAG, "dbDataChangeCallback...")
        when {
            collectionChanges.all { change -> change.type == DocumentChange.Type.ADDED } -> {
                if (isFirstQuery) {
                    /**SuccessAllUsers**/
                    Log.d(
                        TAG,
                        "dbDataChangeCallback: DocumentChange.Type.ADDED: isFirstQuery==TRUE"
                    )
                    listOfUsers.clear()
                    for (change in collectionChanges) {
                        val user = change.document.toObject(User::class.java)
                        listOfUsers.add(user)
                        Log.d(
                            TAG,
                            "dbDataChangeCallback: ADDED: ${user.userName}, ${user.userId}, lat:${user.latitude}, long:${user.longitude}"
                        )
                    }
                    _usersResource.postValue(
                        UsersResource.SuccessAllUsers(
                            data = listOfUsers
                        )
                    )
                    isFirstQuery = false
                } else {
                    /**SuccessNewUser**/
                    Log.d(
                        TAG,
                        "dbDataChangeCallback: DocumentChange.Type.ADDED: isFirstQuery==FALSE"
                    )
                    val addedUsersList = mutableListOf<User>()
                    for (change in collectionChanges) {
                        val user = change.document.toObject(User::class.java)
                        listOfUsers.add(user)
                        addedUsersList.add(user)
                        Log.d(
                            TAG,
                            "dbDataChangeCallback: ADDED: ${user.userName}, ${user.userId}, lat:${user.latitude}, long:${user.longitude}"
                        )
                    }
                    _usersResource.postValue(
                        UsersResource.SuccessNewUser(
                            data = listOfUsers,
                            updatedUsers = addedUsersList
                        )
                    )
                }
            }

            collectionChanges.all { change -> change.type == DocumentChange.Type.MODIFIED } -> {
                Log.d(TAG, "dbDataChangeCallback: DocumentChange.Type.MODIFIED")
                val updatedUsersList = mutableListOf<User>()
                for (change in collectionChanges) {
                    val user = change.document.toObject(User::class.java)
                    val updatedUserIndex = listOfUsers.indexOfFirst { it.userId == user.userId }
                    if (updatedUserIndex >= 0) {
                        listOfUsers[updatedUserIndex] = user
                        updatedUsersList.add(user)
                        _usersResource.postValue(
                            UsersResource.SuccessUpdateUser(
                                data = listOfUsers,
                                updatedUsers = updatedUsersList
                            )
                        )
                    }
                    Log.d(
                        TAG,
                        "dbDataChangeCallback: MODIFIED: ${user.userName}, ${user.userId}, lat:${user.latitude}, long:${user.longitude}"
                    )
                }
            }

            collectionChanges.all { change -> change.type == DocumentChange.Type.REMOVED } -> {
                Log.d(TAG, "dbDataChangeCallback: DocumentChange.Type.REMOVED")
                val removedUsersList = mutableListOf<User>()
                for (change in collectionChanges) {
                    val user = change.document.toObject(User::class.java)
                    val deletedUserIndex = listOfUsers.indexOfFirst { it.userId == user.userId }
                    listOfUsers.removeAt(deletedUserIndex)
                    removedUsersList.add(user)
                    _usersResource.postValue(
                        UsersResource.SuccessDeleteUser(
                            data = listOfUsers,
                            updatedUsers = removedUsersList
                        )
                    )
                    Log.d(TAG, "dbDataChangeCallback: REMOVED: ${user.userName}, ${user.userId}")
                }
            }

            else -> {
                Log.d(TAG, "=======MainViewModel:ELSE! Data is ignored!===========")
            }
        }
    }

    fun getGroups(): List<Group> = listOfGroups

    fun getUsers(): List<User> = listOfUsers

    //fun getCurrentGroup() = currentGroup

    override fun onCleared() {
        super.onCleared()
        usersListener?.remove()
        groupsListener?.remove()
    }

    sealed class MainEvents {
        object CheckPermissions : MainEvents()
        object ShowLoading : MainEvents()
        data class ShowMessage(val message: String) : MainEvents()
    }

    companion object {
        private const val TAG = "myTag"
    }

}