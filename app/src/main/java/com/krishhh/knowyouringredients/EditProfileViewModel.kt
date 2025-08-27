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
    private val uid by lazy { FirebaseAuth.getInstance().currentUser!!.uid }

    private val _name = MutableStateFlow<String?>(null)
    val name: StateFlow<String?> = _name

    private val _photoUrl = MutableStateFlow<String?>(null)
    val photoUrl: StateFlow<String?> = _photoUrl

    private val _email = MutableStateFlow<String?>(null)
    val email: StateFlow<String?> = _email

    private val _age = MutableStateFlow<Int?>(null)
    val age: StateFlow<Int?> = _age

    private val _gender = MutableStateFlow<String?>(null)
    val gender: StateFlow<String?> = _gender

    private val _weight = MutableStateFlow<Float?>(null)
    val weight: StateFlow<Float?> = _weight

    private val _height = MutableStateFlow<Float?>(null)
    val height: StateFlow<Float?> = _height

    private val _dietType = MutableStateFlow<String?>(null)
    val dietType: StateFlow<String?> = _dietType

    private val _allergies = MutableStateFlow<String?>(null)
    val allergies: StateFlow<String?> = _allergies

    private val _healthConditions = MutableStateFlow<String?>(null)
    val healthConditions: StateFlow<String?> = _healthConditions

    private val _preferences = MutableStateFlow<String?>(null)
    val preferences: StateFlow<String?> = _preferences

    private val _goals = MutableStateFlow<String?>(null)
    val goals: StateFlow<String?> = _goals

    fun loadProfileData() {
        viewModelScope.launch {
            Firebase.firestore.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    _name.value = doc.getString("name")
                    _photoUrl.value = doc.getString("photoUrl")
                    _email.value = doc.getString("email")
                    _age.value = doc.getLong("age")?.toInt()
                    _gender.value = doc.getString("gender")
                    _weight.value = doc.getDouble("weight")?.toFloat()
                    _height.value = doc.getDouble("height")?.toFloat()
                    _dietType.value = doc.getString("dietType")
                    _allergies.value = doc.getString("allergies")
                    _healthConditions.value = doc.getString("healthConditions")
                    _preferences.value = doc.getString("preferences")
                    _goals.value = doc.getString("healthGoals")
                }
        }
    }

    fun updateLocalData(
        name: String,
        photoUrl: String?,
        email: String?,
        age: Int?,
        gender: String?,
        weight: Float?,
        height: Float?,
        dietType: String?,
        allergies: String?,
        healthConditions: String?,
        preferences: String?,
        goals: String?
    ) {
        _name.value = name
        _photoUrl.value = photoUrl
        _email.value = email
        _age.value = age
        _gender.value = gender
        _weight.value = weight
        _height.value = height
        _dietType.value = dietType
        _allergies.value = allergies
        _healthConditions.value = healthConditions
        _preferences.value = preferences
        _goals.value = goals
    }
}