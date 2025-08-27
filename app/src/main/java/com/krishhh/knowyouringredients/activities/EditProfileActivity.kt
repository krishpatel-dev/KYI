package com.krishhh.knowyouringredients.activities

import android.R
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.krishhh.knowyouringredients.EditProfileViewModel
import com.krishhh.knowyouringredients.databinding.ActivityEditProfileBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private var imageUri: Uri? = null
    private val uid by lazy { FirebaseAuth.getInstance().currentUser!!.uid }
    private val viewModel: EditProfileViewModel by viewModels()

    private val dietOptions = listOf("None", "Vegetarian", "Vegan", "Pescatarian", "Keto", "Halal", "Jain", "Other")
    private val genderOptions = listOf("Prefer not to say", "Male", "Female", "Other")
    private val goalsOptions = listOf("Maintain weight", "Lose weight", "Gain muscle", "Improve heart health", "General wellness")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarEdit)
        supportActionBar?.title = "Edit Profile"
        binding.toolbarEdit.setNavigationOnClickListener { finish() }

        binding.etEmail.apply {
            isEnabled = false
            alpha = 0.6f
        }

        (binding.actvDietType as AutoCompleteTextView).setAdapter(
            ArrayAdapter(this, R.layout.simple_list_item_1, dietOptions)
        )
        (binding.actvGender as AutoCompleteTextView).setAdapter(
            ArrayAdapter(this, R.layout.simple_list_item_1, genderOptions)
        )
        (binding.actvGoals as AutoCompleteTextView).setAdapter(
            ArrayAdapter(this, R.layout.simple_list_item_1, goalsOptions)
        )

        observeProfile()
        viewModel.loadProfileData()

        val picker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                it.data?.data?.let { uri ->
                    imageUri = uri
                    Glide.with(this).load(uri).placeholder(com.krishhh.knowyouringredients.R.mipmap.ic_launcher_round).into(binding.ivEditPhoto)
                    MainActivity.instance?.viewModel?.localPhotoUri?.value = uri
                }
            }
        }

        binding.ivEditPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            picker.launch(intent)
        }

        binding.btnSave.setOnClickListener {
            val newName = binding.etName.text.toString().trim()
            MainActivity.instance?.viewModel?.localUserName?.value = newName
            imageUri?.let { MainActivity.instance?.viewModel?.localPhotoUri?.value = it }
            saveProfile()
        }
    }

    private fun observeProfile() {
        lifecycleScope.launch { viewModel.name.collectLatest { it?.let { binding.etName.setText(it) } } }
        lifecycleScope.launch { viewModel.photoUrl.collectLatest { url -> Glide.with(this@EditProfileActivity).load(url).placeholder(
            com.krishhh.knowyouringredients.R.mipmap.ic_launcher_round).into(binding.ivEditPhoto) } }
        lifecycleScope.launch { viewModel.email.collectLatest { it?.let { binding.etEmail.setText(it) } } }
        lifecycleScope.launch { viewModel.age.collectLatest { it?.let { binding.etAge.setText(it.toString()) } } }
        lifecycleScope.launch { viewModel.gender.collectLatest { it?.let { binding.actvGender.setText(it, false) } } }
        lifecycleScope.launch { viewModel.weight.collectLatest { it?.let { binding.etWeight.setText(it.toString()) } } }
        lifecycleScope.launch { viewModel.height.collectLatest { it?.let { binding.etHeight.setText(it.toString()) } } }
        lifecycleScope.launch { viewModel.dietType.collectLatest { it?.let { binding.actvDietType.setText(it, false) } } }
        lifecycleScope.launch { viewModel.allergies.collectLatest { it?.let { binding.etAllergies.setText(it) } } }
        lifecycleScope.launch { viewModel.healthConditions.collectLatest { it?.let { binding.etHealth.setText(it) } } }
        lifecycleScope.launch { viewModel.preferences.collectLatest { it?.let { binding.etPreferences.setText(it) } } }
        lifecycleScope.launch { viewModel.goals.collectLatest { it?.let { binding.actvGoals.setText(it, false) } } }
    }

    private fun saveProfile() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        if (name.isEmpty()) { Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show(); return }

        val age = binding.etAge.text.toString().trim().toIntOrNull()
        val weight = binding.etWeight.text.toString().trim().toFloatOrNull()
        val height = binding.etHeight.text.toString().trim().toFloatOrNull()
        val gender = binding.actvGender.text.toString().takeIf { it.isNotBlank() }
        val diet = binding.actvDietType.text.toString().takeIf { it.isNotBlank() }
        val allergies = binding.etAllergies.text.toString().trim().takeIf { it.isNotBlank() }
        val health = binding.etHealth.text.toString().trim().takeIf { it.isNotBlank() }
        val preferences = binding.etPreferences.text.toString().trim().takeIf { it.isNotBlank() }
        val goals = binding.actvGoals.text.toString().takeIf { it.isNotBlank() }

        if (imageUri != null) {
            uploadImageThenSave(name, email, age, gender, weight, height, diet, allergies, health, preferences, goals)
        } else {
            updateFirestore(name, viewModel.photoUrl.value, email, age, gender, weight, height, diet, allergies, health, preferences, goals)
        }
    }

    private fun uploadImageThenSave(
        name: String, email: String, age: Int?, gender: String?, weight: Float?, height: Float?,
        diet: String?, allergies: String?, health: String?, preferences: String?, goals: String?
    ) {
        val ref = FirebaseStorage.getInstance().reference.child("profile_images/${uid}_${UUID.randomUUID()}.jpg")
        val bmp = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        val baos = ByteArrayOutputStream().apply { bmp.compress(Bitmap.CompressFormat.JPEG, 80, this) }

        ref.putBytes(baos.toByteArray())
            .continueWithTask { ref.downloadUrl }
            .addOnSuccessListener { url ->
                updateFirestore(name, url.toString(), email, age, gender, weight, height, diet, allergies, health, preferences, goals)
            }
            .addOnFailureListener { Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show() }
    }

    private fun updateFirestore(
        name: String, photoUrl: String?, email: String, age: Int?, gender: String?, weight: Float?, height: Float?,
        dietType: String?, allergies: String?, healthConditions: String?, preferences: String?, goals: String?
    ) {
        val data = hashMapOf<String, Any>(
            "name" to name,
            "email" to email,
            "photoUrl" to (photoUrl ?: "")
        )
        data["age"] = age ?: FieldValue.delete()
        data["gender"] = gender?.takeIf { it.isNotBlank() } ?: FieldValue.delete()
        data["weight"] = weight ?: FieldValue.delete()
        data["height"] = height ?: FieldValue.delete()
        data["dietType"] = dietType?.takeIf { it.isNotBlank() } ?: FieldValue.delete()
        data["allergies"] = allergies?.takeIf { it.isNotBlank() } ?: FieldValue.delete()
        data["healthConditions"] = healthConditions?.takeIf { it.isNotBlank() } ?: FieldValue.delete()
        data["preferences"] = preferences?.takeIf { it.isNotBlank() } ?: FieldValue.delete()
        data["healthGoals"] = goals?.takeIf { it.isNotBlank() } ?: FieldValue.delete()

        Firebase.firestore.collection("users").document(uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                getSharedPreferences("user_prefs", MODE_PRIVATE).edit {
                    putString("userName", name)
                    putString("photoUrl", photoUrl)
                }
                MainActivity.instance?.loadProfile()
                finish()
            }
            .addOnFailureListener { Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show() }
    }
}
