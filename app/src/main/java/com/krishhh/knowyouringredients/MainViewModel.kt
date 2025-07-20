package com.krishhh.knowyouringredients

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainViewModel : ViewModel() {
    var userName: String? = null
    var photoUrl: String? = null
    var hasLoadedProfile = false

    fun loadUserProfile(onLoaded: (String?, String?) -> Unit) {
        if (hasLoadedProfile) {
            onLoaded(userName, photoUrl)
            return
        }

        Firebase.firestore.collection("users")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .get()
            .addOnSuccessListener { snap ->
                userName = snap.getString("name")
                photoUrl = snap.getString("photoUrl")
                hasLoadedProfile = true
                onLoaded(userName, photoUrl)
            }
    }
}
