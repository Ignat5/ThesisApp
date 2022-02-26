package com.mycompany.thesis1

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_registration.*

class RegistrationFragment: Fragment(R.layout.fragment_registration) {

    private lateinit var mAuth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private lateinit var db: FirebaseFirestore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun initListeners() {
        btnRegister.setOnClickListener {
            val userEmail: String? = etEmail.text.toString()
            val userPassword: String? = etPassword.text.toString()
            val userName = etName.text.toString()
            if(!userEmail.isNullOrEmpty() && !userPassword.isNullOrEmpty() && !userName.isNullOrEmpty())
            registerUser(userEmail, userPassword, userName)
        }
    }

    private fun registerUser(userEmail: String, userPassword: String, userName: String) {
        progressBar.visibility = View.VISIBLE
        mAuth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener {
            if(it.isSuccessful) {
                currentUser = mAuth.currentUser
                //saveUserToDB(userEmail, userName)
                Log.d("myTag", "registerUser: successful registration")
            }else {
                Log.d("myTag", "registerUser: error: ${it.exception}")
            }
        }
    }

//    private fun saveUserToDB(userEmail: String, userName: String) {
//        val map = hashMapOf("userName" to userName)
//        db.collection(USERS_COLLECTION).document(userEmail).set(map).addOnCompleteListener {
//            if(it.isSuccessful) {
//                val map = hashMapOf(
//                    "userName" to userName,
//                    "latitude" to 50.0,
//                    "longitude" to 50.0
//                )
//                db.collection(USERS_COLLECTION).document(userEmail).collection(OBSERVABLES_COLLECTION).document(userEmail).set(map).addOnCompleteListener { task ->
//                    if(task.isSuccessful) {
//                        Log.d("myTag2", "saveUserToDB: all success")
//                        val action =
//                    RegistrationFragmentDirections.actionGlobalHomeFragment()
//                    findNavController().navigate(action)
//                    } else {
//                        Log.d("myTag2", "saveUserToDB: save observable error")
//                    }
//                }
//            } else {
//                Log.d("myTag2", "saveUserToDB: save user error")
//            }
//            progressBar.visibility = View.GONE
//        }
//    }

}