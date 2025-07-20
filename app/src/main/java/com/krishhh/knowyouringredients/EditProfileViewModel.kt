package com.krishhh.knowyouringredients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EditProfileViewModel : ViewModel() {

    private val _name = MutableStateFlow<String?>(null)
    val name: StateFlow<String?> = _name

    private val _photoUrl = MutableStateFlow<String?>(null)
    val photoUrl: StateFlow<String?> = _photoUrl

    private val uid by lazy { FirebaseAuth.getInstance().currentUser!!.uid }

    fun loadProfileData() {
        if (_name.value != null && _photoUrl.value != null) return // already loaded

        viewModelScope.launch {
            Firebase.firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { snap ->
                    _name.value = snap.getString("name")
                    _photoUrl.value = snap.getString("photoUrl")
                }
        }
    }

    fun updateLocalData(name: String, photoUrl: String?) {
        _name.value = name
        if (photoUrl != null) _photoUrl.value = photoUrl
    }
}
