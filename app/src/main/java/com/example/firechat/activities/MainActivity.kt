package com.example.firechat.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import com.example.firechat.adapters.RecentConversationsAdapter
import com.example.firechat.databinding.ActivityMainBinding
import com.example.firechat.listeners.ConversionListener
import com.example.firechat.models.ChatMessage
import com.example.firechat.models.User
import com.example.firechat.utilities.Constants.Constants
import com.example.firechat.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : BaseActivity(), ConversionListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var preferencesManager: PreferenceManager
    private lateinit var conversations: ArrayList<ChatMessage>
    private lateinit var conversationsAdapter: RecentConversationsAdapter
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferencesManager = PreferenceManager(applicationContext)
        init()
        loadUserDetails()
        getToken()
        setListeners()
        listenConversations()
    }

    private fun init() {
        conversations = ArrayList()
        conversationsAdapter = RecentConversationsAdapter(conversations, this)
        binding.conversationsRecyclerView.adapter = conversationsAdapter
        db = Firebase.firestore
    }

    private fun setListeners() {
        binding.imageSignOut.setOnClickListener {
            signOut()
        }
        binding.fabNewChat.setOnClickListener {
            startActivity(Intent(applicationContext, UserActivity::class.java))
        }
    }

    private fun loadUserDetails() {
        binding.textName.text = preferencesManager.getString(Constants.KEY_NAME)
        val bytes = Base64.decode(preferencesManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        binding.imageProfile.setImageBitmap(bitmap)
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun listenConversations() {
        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID, preferencesManager.getString(Constants.KEY_USER_ID))
            .addSnapshotListener {
                snapshot, error ->
                if(error != null) {
                    return@addSnapshotListener
                }
                if(snapshot != null) {
                    for(documentChange in snapshot.documentChanges){
                        if(documentChange.type == DocumentChange.Type.ADDED) {
                            val senderId = documentChange.document.getString(Constants.KEY_SENDER_ID)
                            val receiverId = documentChange.document.getString(Constants.KEY_RECEIVER_ID)
                            if(preferencesManager.getString(Constants.KEY_USER_ID).equals(senderId)){
                                val chatMessage = ChatMessage(
                                    senderId.toString(),
                                    receiverId.toString(),
                                    documentChange.document.getString(Constants.KEY_LAST_MESSAGE).toString(),
                                    null,
                                    documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!,
                                    documentChange.document.getString(Constants.KEY_RECEIVER_ID).toString(),
                                    documentChange.document.getString(Constants.KEY_RECEIVER_NAME).toString(),
                                    documentChange.document.getString(Constants.KEY_RECEIVER_IMAGE).toString(),
                                )
                                conversations.add(chatMessage)
                            }else{
                                val chatMessage = ChatMessage(
                                    senderId.toString(),
                                    receiverId.toString(),
                                    documentChange.document.getString(Constants.KEY_LAST_MESSAGE).toString(),
                                    null,
                                    documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!,
                                    documentChange.document.getString(Constants.KEY_SENDER_ID).toString(),
                                    documentChange.document.getString(Constants.KEY_SENDER_NAME).toString(),
                                    documentChange.document.getString(Constants.KEY_SENDER_IMAGE).toString(),
                                )
                                conversations.add(chatMessage)
                            }
                        } else if(documentChange.type == DocumentChange.Type.MODIFIED) {
                            for(i in conversations.indices) {
                                val senderId = documentChange.document.getString(Constants.KEY_SENDER_ID)
                                val receiverId = documentChange.document.getString(Constants.KEY_RECEIVER_ID)
                                if(conversations[i].senderId == senderId && conversations[i].receiverId == receiverId){
                                    conversations[i].message = documentChange.document.getString(Constants.KEY_LAST_MESSAGE).toString()
                                    conversations[i].dateObject = documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!
                                    break
                                }
                            }
                        }
                    }
                    conversations.sortWith { obj1, obj2 ->
                        obj2.dateObject.compareTo(obj1.dateObject)
                    }
                    conversationsAdapter.notifyDataSetChanged()
                    binding.conversationsRecyclerView.smoothScrollToPosition(0)
                    binding.conversationsRecyclerView.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }
            }

        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, preferencesManager.getString(Constants.KEY_USER_ID))
            .addSnapshotListener {
                    snapshot, error ->
                if(error != null) {
                    return@addSnapshotListener
                }
                if(snapshot != null) {
                    for(documentChange in snapshot.documentChanges){
                        if(documentChange.type == DocumentChange.Type.ADDED) {
                            val senderId = documentChange.document.getString(Constants.KEY_SENDER_ID)
                            val receiverId = documentChange.document.getString(Constants.KEY_RECEIVER_ID)
                            if(preferencesManager.getString(Constants.KEY_USER_ID).equals(senderId)){
                                val chatMessage = ChatMessage(
                                    senderId.toString(),
                                    receiverId.toString(),
                                    documentChange.document.getString(Constants.KEY_LAST_MESSAGE).toString(),
                                    null,
                                    documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!,
                                    documentChange.document.getString(Constants.KEY_RECEIVER_ID).toString(),
                                    documentChange.document.getString(Constants.KEY_RECEIVER_NAME).toString(),
                                    documentChange.document.getString(Constants.KEY_RECEIVER_IMAGE).toString(),
                                )
                                conversations.add(chatMessage)
                            }else{
                                val chatMessage = ChatMessage(
                                    senderId.toString(),
                                    receiverId.toString(),
                                    documentChange.document.getString(Constants.KEY_LAST_MESSAGE).toString(),
                                    null,
                                    documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!,
                                    documentChange.document.getString(Constants.KEY_SENDER_ID).toString(),
                                    documentChange.document.getString(Constants.KEY_SENDER_NAME).toString(),
                                    documentChange.document.getString(Constants.KEY_SENDER_IMAGE).toString(),
                                )
                                conversations.add(chatMessage)
                            }
                        } else if(documentChange.type == DocumentChange.Type.MODIFIED) {
                            for(i in conversations.indices) {
                                val senderId = documentChange.document.getString(Constants.KEY_SENDER_ID)
                                val receiverId = documentChange.document.getString(Constants.KEY_RECEIVER_ID)
                                if(conversations[i].senderId == senderId && conversations[i].receiverId == receiverId){
                                    conversations[i].message = documentChange.document.getString(Constants.KEY_LAST_MESSAGE).toString()
                                    conversations[i].dateObject = documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!
                                    break
                                }
                            }
                        }
                    }
                    conversations.sortWith { obj1, obj2 ->
                        obj2.dateObject.compareTo(obj1.dateObject)
                    }
                    conversationsAdapter.notifyDataSetChanged()
                    binding.conversationsRecyclerView.smoothScrollToPosition(0)
                    binding.conversationsRecyclerView.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }
            }
    }

    private fun getToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener{
            updateToken(it)
        }
    }
    
    private fun updateToken(token: String) {
        preferencesManager.putString(Constants.KEY_FCM_TOKEN, token)
        val db = Firebase.firestore
        val documentReference = db.collection(Constants.KEY_COLLECTION_USERS)
            .document(preferencesManager.getString(Constants.KEY_USER_ID)!!)
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
//            .addOnSuccessListener {
//                showToast("Token Created Successfully")
//            }
            .addOnFailureListener {
                showToast("Unable to update token")
            }
    }

    private fun signOut() {
        showToast("Signing out...")
        val db = Firebase.firestore
        val documentReference = db.collection(Constants.KEY_COLLECTION_USERS)
            .document(preferencesManager.getString(Constants.KEY_USER_ID)!!)
        val updates = HashMap<String, Any>()
        updates[Constants.KEY_FCM_TOKEN] = FieldValue.delete()
        documentReference.update(updates)
            .addOnSuccessListener {
                preferencesManager.clear()
                startActivity(Intent(applicationContext, SignInActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                showToast("Unable to sign out")
            }
    }

    override fun onConversionClicked(user: User) {
        val intent = Intent(applicationContext, ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER, user)
        startActivity(intent)
    }
}