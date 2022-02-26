package com.mycompany.thesis1.mvvm.groups

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
import com.mycompany.thesis1.constants.AppConstants.GROUP_ID
import com.mycompany.thesis1.constants.AppConstants.GROUP_IDS
import com.mycompany.thesis1.constants.AppConstants.GROUP_NAME
import com.mycompany.thesis1.constants.AppConstants.IS_CURRENT
import com.mycompany.thesis1.constants.AppConstants.USERS_COLLECTION
import com.mycompany.thesis1.mvvm.map.MainViewModel
import com.mycompany.thesis1.mvvm.model.Group
import com.mycompany.thesis1.mvvm.model.CommonResource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class GroupsViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val listOfGroups = mutableListOf<Group>()
    private val _groups = MutableLiveData<List<Group>>()
    val groups: LiveData<List<Group>> = _groups
    private var dataListener: ListenerRegistration? = null
    private val eventsChannel = Channel<CommonResource>()
    val eventFlow = eventsChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            start()
        }
    }

    private fun start() {
        firebaseAuth.currentUser?.email?.let { userId ->
            try {
                dataListener =
                    db.collection(USERS_COLLECTION).document(userId).collection(GROUPS_COLLECTION)
                        .addSnapshotListener { value, error ->
                            if (error != null) throw FirebaseFirestoreException(
                                error.message!!,
                                error.code
                            )
                            listOfGroups.clear()
                            value?.documents?.let { documents ->
                                for (document in documents) {
                                    val group = document?.toObject(Group::class.java)
                                    group?.let { listOfGroups.add(it) }
                                }
                                _groups.postValue(listOfGroups)
                            }
                        }
            } catch (e: Exception) {
                Log.d("myTag", "error_groups: ${e.message}")
            }
        }
    }

    fun createGroup(groupName: String) = viewModelScope.launch {
        firebaseAuth.currentUser?.email?.let { userId ->
            try {
                eventsChannel.send(CommonResource.ShowDownload())
                val groupId = System.currentTimeMillis().toString()
                val newGroupDocument = db.collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(GROUPS_COLLECTION)
                    .document(groupId)

                val userDocument = db.collection(USERS_COLLECTION)
                    .document(userId)

                db.runBatch { batch ->
                    batch.set(
                        newGroupDocument, hashMapOf(
                            GROUP_NAME to groupName,
                            GROUP_ID to groupId,
                            IS_CURRENT to false
                        )
                    )
                    batch.update(
                        userDocument, mutableMapOf<String, Any>(
                            GROUP_IDS to FieldValue.arrayUnion(groupId)
                        )
                    )
                }.await()
            } catch (e: Exception) {
                eventsChannel.send(CommonResource.Error(e.message))
            }
        }
    }

    fun updateGroupName(groupId: String, groupName: String) = viewModelScope.launch {
        firebaseAuth.currentUser?.email?.let { userId ->
            try {
                eventsChannel.send(CommonResource.ShowDownload())
                db.collection(USERS_COLLECTION).document(userId).collection(GROUPS_COLLECTION)
                    .document(groupId).update(
                        mutableMapOf<String, Any>(
                            GROUP_NAME to groupName
                        )
                    ).await()
            } catch (e: Exception) {
                eventsChannel.send(CommonResource.Error(e.message))
            }
        }
    }

    fun deleteGroupById(groupId: String) = viewModelScope.launch {
        firebaseAuth.currentUser?.email?.let { userId ->
            try {
                eventsChannel.send(CommonResource.ShowDownload())
                db.collection(USERS_COLLECTION).document(userId).collection(GROUPS_COLLECTION)
                    .document(groupId).delete().await()
            } catch (e: Exception) {
                eventsChannel.send(CommonResource.Error(e.message))
            }
        }
    }

    fun getGroupById(groupId: String) = listOfGroups.find { it.groupId == groupId }

    override fun onCleared() {
        super.onCleared()
        dataListener?.remove()
    }
}