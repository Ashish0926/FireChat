package com.example.firechat.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.firechat.utilities.Constants.Constants
import com.example.firechat.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

open class BaseActivity: AppCompatActivity() {

    private lateinit var documentReference: DocumentReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferenceManager = PreferenceManager(applicationContext)
        val db = Firebase.firestore
        documentReference = db.collection(Constants.KEY_COLLECTION_USERS)
            .document(preferenceManager.getString(Constants.KEY_USER_ID).toString())
    }

    override fun onPause() {
        super.onPause()
        documentReference.update(Constants.KEY_AVAILABILITY, 0)
    }

    override fun onResume() {
        super.onResume()
        documentReference.update(Constants.KEY_AVAILABILITY, 1)
    }
}