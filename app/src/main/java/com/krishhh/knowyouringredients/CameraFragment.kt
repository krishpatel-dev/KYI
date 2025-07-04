package com.krishhh.knowyouringredients

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.guava.await


class CameraFragment : Fragment() {

    private lateinit var previewView: PreviewView
    private val permission = Manifest.permission.CAMERA
    private val requestPermission =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera() else requireActivity().finish()
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        previewView = inflater.inflate(R.layout.fragment_camera, container, false) as PreviewView
        return previewView
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(requireContext(), permission)
            == PackageManager.PERMISSION_GRANTED
        ) startCamera()
        else requestPermission.launch(permission)
    }

    private fun startCamera() = lifecycleScope.launch {
        val provider = ProcessCameraProvider.getInstance(requireContext()).await()
        provider.unbindAll()

        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }

        provider.bindToLifecycle(
            viewLifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview
        )
    }

}
