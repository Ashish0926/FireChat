package com.example.firechat.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.firechat.adapters.UsersAdapter
import com.example.firechat.databinding.ActivityUserBinding
import com.example.firechat.listeners.UserListener
import com.example.firechat.models.User
import com.example.firechat.utilities.Constants.Constants
import com.example.firechat.utilities.PreferenceManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserActivity : BaseActivity(), UserListener {

    private lateinit var binding: ActivityUserBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        setListeners()
        getUsers()
    }

    private fun setListeners() {
        binding.imageBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getUsers() {
        loading(true)
        val db = Firebase.firestore
        db.collection(Constants.KEY_COLLECTION_USERS)
            .get()
            .addOnCompleteListener{
                loading(false)
                val currentUserId = preferenceManager.getString(Constants.KEY_USER_ID)
                //Log.d("currentId", currentUserId.toString())
                if(it.isSuccessful && it.result != null) {
                    //Log.d("successful", it.toString())
                    val users = ArrayList<User>()
                    val userListInDB = it.result
                    for(queryDocumentSnapshot in userListInDB!!) {
                        if(currentUserId.equals(queryDocumentSnapshot.id)){
                            continue
                        }
                        val name = queryDocumentSnapshot.getString(Constants.KEY_NAME).toString()
                        val email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL).toString()
                        val image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE).toString()
                        val fcm = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN).toString()
                        val id = queryDocumentSnapshot.id
                        //Log.d("Values", "$name,$email,$image,$fcm")
                        val user = User (
                            name,
                            email,
                            image,
                            fcm,
                            id
                        )
                        users.add(user)
                    }
                    if(users.size > 0) {
                        val userAdapter = UsersAdapter(users, this)
                        binding.userRecyclerView.adapter = userAdapter
                        binding.userRecyclerView.visibility = View.VISIBLE
                    }else {
                        showErrorMessage()
                    }
                } else {
                    showErrorMessage()
                }
            }
    }

    private fun showErrorMessage() {
        binding.textErrorMessage.text = String.format("%s", "No users available")
        binding.textErrorMessage.visibility = View.VISIBLE
    }

    private fun loading(isLoading: Boolean) {
        if(isLoading){
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onUserClicked(user: User) {
        val intent = Intent(applicationContext, ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER, user)
        startActivity(intent)
        finish()
    }
}