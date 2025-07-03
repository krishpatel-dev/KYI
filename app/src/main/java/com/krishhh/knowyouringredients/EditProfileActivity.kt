package com.krishhh.knowyouringredients

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.firestore.ktx.firestore
import com.krishhh.knowyouringredients.databinding.ActivityEditProfileBinding
import java.util.UUID

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private var selectedUri: Uri? = null
    private val uid get() = FirebaseAuth.getInstance().currentUser!!.uid

    /* ---------- Activity‑result launchers ---------- */

    // Ask for READ_MEDIA_IMAGES / READ_EXTERNAL_STORAGE
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) pickImage.launch("image/*")
            else toast("Permission denied")
        }

    // System picker for an image
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedUri = uri
                Glide.with(this).load(uri).into(binding.ivEditPhoto)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Insets so content sits below notch
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootEdit) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(0, top, 0, 0)
            WindowInsetsCompat.CONSUMED
        }

        prefill()
        binding.ivEditPhoto.setOnClickListener { checkPermissionAndPick() }
        binding.btnSave.setOnClickListener { saveProfile() }
    }

    /* ---------- Prefill current profile ---------- */
    private fun prefill() {
        Firebase.firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { snap ->
                binding.etName.setText(snap.getString("name") ?: "")
                Glide.with(this)
                    .load(snap.getString("photoUrl"))
                    .placeholder(R.mipmap.ic_launcher_round)
                    .into(binding.ivEditPhoto)
            }
    }

    /* ---------- Permission then gallery ---------- */
    private fun checkPermissionAndPick() {
        if (Build.VERSION.SDK_INT >= 33) {
            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    /* ---------- Save profile ---------- */
    private fun saveProfile() {
        val name = binding.etName.text.toString().trim()
        if (name.isEmpty()) { toast("Name required"); return }

        if (selectedUri != null) {
            uploadImageThenSave(name)
        } else {
            saveToFirestore(name, null)
        }
    }

    private fun uploadImageThenSave(name: String) {
        val ref = Firebase.storage.reference
            .child("profileImages/$uid/${UUID.randomUUID()}.jpg")

        // Show simple progress
        toast("Uploading…")

        ref.putFile(selectedUri!!)
            .addOnSuccessListener { snap ->
                // Upload done – now get the HTTPS download URL
                snap.storage.downloadUrl
                    .addOnSuccessListener { uri ->
                        saveToFirestore(name, uri.toString())
                    }
                    .addOnFailureListener { e ->
                        toast("URL error: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                toast("Upload failed: ${e.message}")
            }
    }


    private fun saveToFirestore(name: String, photoUrl: String?) {
        val data = hashMapOf<String, Any>("name" to name)
        photoUrl?.let { data["photoUrl"] = it }

        Firebase.firestore.collection("users").document(uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { toast("Profile updated"); finish() }
            .addOnFailureListener { e -> toast("Update failed: ${e.message}") }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
