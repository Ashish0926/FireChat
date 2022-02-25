package com.example.firechat.activities

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import com.example.firechat.adapters.ChatAdapter
import com.example.firechat.databinding.ActivityChatBinding
import com.example.firechat.models.ChatMessage
import com.example.firechat.models.User
import com.example.firechat.network.ApiClient
import com.example.firechat.network.ApiService
import com.example.firechat.utilities.Constants.Constants
import com.example.firechat.utilities.PreferenceManager
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatActivity : BaseActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var receiverUser: User
    private lateinit var chatMessages: ArrayList<ChatMessage>
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var db: FirebaseFirestore
    private var conversionId: String? = null
    private var isReceiverAvailable: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
        loadReceiverDetails()
        init()
        listenMessages()
    }

    private fun init() {
        preferenceManager = PreferenceManager(applicationContext)
        chatMessages = ArrayList()
        chatAdapter = ChatAdapter(
            chatMessages,
            preferenceManager.getString(Constants.KEY_USER_ID).toString(),
            getBitmapFromEncodedString(receiverUser.image!!)
        )
        binding.chatRecyclerView.adapter = chatAdapter
        db = Firebase.firestore
    }

    private fun sendMessage() {
        if(binding.inputMessage.text.toString().trim().isEmpty()){
            return
        }
        val message = HashMap<String, Any>()
        message[Constants.KEY_SENDER_ID] = preferenceManager.getString(Constants.KEY_USER_ID).toString()
        message[Constants.KEY_RECEIVER_ID] = receiverUser.id
        message[Constants.KEY_MESSAGE] = binding.inputMessage.text.toString()
        message[Constants.KEY_TIMESTAMP] = Date()
        db.collection(Constants.KEY_COLLECTION_CHAT).add(message)
        if(conversionId != null){
            updateConversion(binding.inputMessage.text.toString())
        }else{
            val conversion = HashMap<String, Any>()
            conversion[Constants.KEY_SENDER_ID] = preferenceManager.getString(Constants.KEY_USER_ID).toString()
            conversion[Constants.KEY_SENDER_NAME] = preferenceManager.getString(Constants.KEY_NAME).toString()
            conversion[Constants.KEY_SENDER_IMAGE] = preferenceManager.getString(Constants.KEY_IMAGE).toString()
            conversion[Constants.KEY_RECEIVER_ID] = receiverUser.id
            conversion[Constants.KEY_RECEIVER_NAME] = receiverUser.name
            conversion[Constants.KEY_RECEIVER_IMAGE] = receiverUser.image!!
            conversion[Constants.KEY_LAST_MESSAGE] = binding.inputMessage.text.toString()
            conversion[Constants.KEY_TIMESTAMP] = Date()
            addConversion(conversion)
        }
        if(!isReceiverAvailable) {
            try {
                val tokens = JSONArray()
                tokens.put(receiverUser.token)

                val data = JSONObject()
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME))
                data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN))
                data.put(Constants.KEY_MESSAGE, binding.inputMessage.text.toString())

                val body = JSONObject()
                body.put(Constants.REMOTE_MSG_DATA, data)
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens)

                sendNotification(body.toString())
            } catch(e: Exception) {
                showToast(e.message.toString())
            }
        }
        binding.inputMessage.text = null
    }

    private fun showToast(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun sendNotification(messageBody: String) {
        ApiClient.getClient().create(ApiService::class.java).sendMessage(
            Constants.getRemoteMsgHeaders(),
            messageBody
        ).enqueue(object: retrofit2.Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.isSuccessful) {
                    try {
                        if(response.body() != null){
                            val responseJson = JSONObject(response.body()!!)
                            val results = responseJson.getJSONArray("results")
                            if(responseJson.getInt("failure") == 1){
                                val error = results[0] as JSONObject
                                showToast(error.getString("error"))
                                return
                            }
                        }
                    } catch(e: JSONException) {
                        e.printStackTrace()
                    }
                    showToast("Notification sent successfullly")
                } else {
                    showToast("Error " + response.code())
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                showToast(t.message.toString())
            }
        })
    }

    private fun listenAvailabilityOfReceiver() {
        db.collection(Constants.KEY_COLLECTION_USERS)
            .document(receiverUser.id)
            .addSnapshotListener {
                value, error ->
                if(error != null){
                    return@addSnapshotListener
                }
                if(value != null){
                    if(value.getLong(Constants.KEY_AVAILABILITY) != null) {
                        val availability = Objects.requireNonNull(
                            value.getLong(Constants.KEY_AVAILABILITY)
                        )?.toInt()
                        isReceiverAvailable = availability == 1
                    }
                    receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN)
                }
                if(isReceiverAvailable){
                    binding.textAvailability.visibility = View.VISIBLE
                }else{
                    binding.textAvailability.visibility = View.GONE
                }
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun listenMessages() {
        db.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
            .addSnapshotListener {snapshot, error ->
                if(error != null){
                    return@addSnapshotListener
                }
                if(snapshot != null) {
                    val count = chatMessages.size
                    for(documentChange in snapshot.documentChanges) {
                        if(documentChange.type == DocumentChange.Type.ADDED){
                            val chatMessage = ChatMessage(
                                documentChange.document.getString(Constants.KEY_SENDER_ID).toString(),
                                documentChange.document.getString(Constants.KEY_RECEIVER_ID).toString(),
                                documentChange.document.getString(Constants.KEY_MESSAGE).toString(),
                                getReadableDateTime(documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!),
                                documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!
                            )
                            chatMessages.add(chatMessage)
                        }
                    }
                    chatMessages.sortWith { obj1, obj2 ->
                        obj1.dateObject.compareTo(obj2.dateObject)
                    }
                    if(count == 0){
                        chatAdapter.notifyDataSetChanged()
                    }else{
                        chatAdapter.notifyItemRangeInserted(chatMessages.size, chatMessages.size)
                        binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size - 1)
                    }
                    binding.chatRecyclerView.visibility = View.VISIBLE
                }
                binding.progressBar.visibility = View.GONE
                if(conversionId == null) {
                    checkForConversion()
                }
            }

        db.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
            .addSnapshotListener {value, error ->
                if(error != null){
                    return@addSnapshotListener
                }
                if(value != null) {
                    val count = chatMessages.size
                    for(documentChange in value.documentChanges) {
                        if(documentChange.type == DocumentChange.Type.ADDED){
                            val chatMessage = ChatMessage(
                                documentChange.document.getString(Constants.KEY_SENDER_ID).toString(),
                                documentChange.document.getString(Constants.KEY_RECEIVER_ID).toString(),
                                documentChange.document.getString(Constants.KEY_MESSAGE).toString(),
                                getReadableDateTime(documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!),
                                documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!
                            )
                            chatMessages.add(chatMessage)
                        }
                    }
                    chatMessages.sortWith { obj1, obj2 ->
                        obj1.dateObject.compareTo(obj2.dateObject)
                    }
                    if(count == 0){
                        chatAdapter.notifyDataSetChanged()
                    }else{
                        chatAdapter.notifyItemRangeInserted(chatMessages.size, chatMessages.size)
                        binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size - 1)
                    }
                    binding.chatRecyclerView.visibility = View.VISIBLE
                }
                binding.progressBar.visibility = View.GONE
                if(conversionId == null) {
                    checkForConversion()
                }
            }
    }


    private fun getBitmapFromEncodedString(encodedImage: String): Bitmap {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun loadReceiverDetails() {
        receiverUser = intent.getSerializableExtra(Constants.KEY_USER) as User
        binding.textName.text = receiverUser.name
    }

    private fun setListeners() {
        binding.imageBack.setOnClickListener {
            onBackPressed()
        }
        binding.layoutSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun getReadableDateTime(date: Date): String {
        return SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date)
    }

    private fun addConversion(conversion: HashMap<String, Any>){
        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .add(conversion)
            .addOnSuccessListener {
                conversionId = it.id
            }
    }

    private fun updateConversion(message: String) {
        val documentReference = db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .document(conversionId.toString())
        documentReference.update(
            Constants.KEY_LAST_MESSAGE, message,
            Constants.KEY_TIMESTAMP, Date()
        )
    }

    private fun checkForConversion() {
        if(chatMessages.size != 0) {
            checkForConversionRemotely(
                preferenceManager.getString(Constants.KEY_USER_ID).toString(),
                receiverUser.id
            )
            checkForConversionRemotely(
                receiverUser.id,
                preferenceManager.getString(Constants.KEY_USER_ID).toString()
            )
        }
    }

    private fun checkForConversionRemotely(senderId: String, receiverId: String) {
        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
            .get()
            .addOnCompleteListener {
                if(it.isSuccessful && it.result != null && it.result!!.documents.size > 0) {
                    val documentSnapshot = it.result!!.documents[0]
                    conversionId = documentSnapshot.id
                }
            }
    }

    override fun onResume() {
        super.onResume()
        listenAvailabilityOfReceiver()
    }
}

