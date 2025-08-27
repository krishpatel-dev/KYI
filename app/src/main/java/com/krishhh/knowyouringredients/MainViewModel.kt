package com.krishhh.knowyouringredients

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow

class MainViewModel : ViewModel() {
    var photoUrl: String? = null
    var hasLoadedProfile = false

    val localPhotoUri = MutableStateFlow<Uri?>(null) // For immediate image
    val localUserName = MutableStateFlow<String?>(null) // NEW: For immediate name update

    fun loadUserProfile(onLoaded: (String?, String?) -> Unit) {
        if (hasLoadedProfile) {
            onLoaded(localUserName.value ?: "", photoUrl)
            return
        }

        Firebase.firestore.collection("users")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .get()
            .addOnSuccessListener { snap ->
                localUserName.value = snap.getString("name")
                photoUrl = snap.getString("photoUrl")
                hasLoadedProfile = true
                onLoaded(localUserName.value, photoUrl)
            }
    }
}
