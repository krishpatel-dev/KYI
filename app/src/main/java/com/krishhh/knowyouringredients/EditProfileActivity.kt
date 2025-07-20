package com.krishhh.knowyouringredients

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.krishhh.knowyouringredients.databinding.ActivityEditProfileBinding
import kotlinx.coroutines.flow.collectLatest
import java.io.ByteArrayOutputStream
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private var imageUri: Uri? = null
    private val uid by lazy { FirebaseAuth.getInstance().currentUser!!.uid }
    private val viewModel: EditProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarEdit)
        supportActionBar?.title = "Edit Profile"
        binding.toolbarEdit.setNavigationOnClickListener { finish() }

        observeProfile()
        viewModel.loadProfileData()

        val picker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                imageUri = it.data?.data
                binding.ivEditPhoto.setImageURI(imageUri)
            }
        }

        binding.ivEditPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            picker.launch(intent)
        }

        binding.btnSave.setOnClickListener { saveProfile() }
    }

    private fun observeProfile() {
        lifecycleScope.launchWhenStarted {
            viewModel.name.collectLatest { name ->
                if (name != null) binding.etName.setText(name)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.photoUrl.collectLatest { url ->
                Glide.with(this@EditProfileActivity)
                    .load(url)
                    .placeholder(R.mipmap.ic_launcher_round)
                    .into(binding.ivEditPhoto)
            }
        }
    }

    private fun saveProfile() {
        val name = binding.etName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri != null) uploadImageThenSave(name) else updateFirestore(name, null)
    }

    private fun uploadImageThenSave(name: String) {
        val ref = FirebaseStorage.getInstance().reference
            .child("profile_images/${uid}_${UUID.randomUUID()}.jpg")

        val bmp = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        val baos = ByteArrayOutputStream().apply {
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, this)
        }

        ref.putBytes(baos.toByteArray())
            .continueWithTask { ref.downloadUrl }
            .addOnSuccessListener { url -> updateFirestore(name, url.toString()) }
            .addOnFailureListener {
                Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateFirestore(name: String, photoUrl: String?) {
        val data = hashMapOf<String, Any>("name" to name)
        photoUrl?.let { data["photoUrl"] = it }

        Firebase.firestore.collection("users").document(uid)
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                viewModel.updateLocalData(name, photoUrl)
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
            }
    }
}
