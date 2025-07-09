package com.krishhh.knowyouringredients

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.view.*
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private lateinit var previewView: PreviewView
    private lateinit var imageCapture: ImageCapture
    private val camPerm = Manifest.permission.CAMERA

    private val permLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { if (it) startCamera() else requireActivity().finish() }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        val v = i.inflate(R.layout.fragment_camera, c, false)
        previewView = v.findViewById(R.id.previewView)
        v.findViewById<View>(R.id.fabScan).setOnClickListener { snap() }
        return v
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(requireContext(), camPerm)
            == PackageManager.PERMISSION_GRANTED) startCamera()
        else permLauncher.launch(camPerm)
    }

    private fun startCamera() {
        val fut = ProcessCameraProvider.getInstance(requireContext())
        fut.addListener({
            val provider = fut.get(); provider.unbindAll()
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }
            provider.bindToLifecycle(
                this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun snap() {
        imageCapture.takePicture(
            Executors.newSingleThreadExecutor(),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(proxy: ImageProxy) {
                    val bmp = proxy.toRotatedBitmap(); proxy.close()
                    val path = bmp.save(requireContext().cacheDir)
                    startActivity(TextSelectionActivity.intent(requireContext(), path))
                }
                override fun onError(e: ImageCaptureException) = toast("Error: ${e.message}")
            })
    }

    /* helpers */
    private fun ImageProxy.toRotatedBitmap(): Bitmap {
        val buf: ByteBuffer = planes[0].buffer
        val bytes = ByteArray(buf.remaining()).also { buf.get(it) }
        val src = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val m = Matrix().apply { postRotate(imageInfo.rotationDegrees.toFloat()) }
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, true)
    }
    private fun Bitmap.save(dir: File): String {
        val f = File(dir, "snap_${System.currentTimeMillis()}.jpg")
        FileOutputStream(f).use { compress(Bitmap.CompressFormat.JPEG, 90, it) }
        return f.absolutePath
    }

    private fun toast(m: String) {
        android.widget.Toast.makeText(requireContext(), m, android.widget.Toast.LENGTH_SHORT).show()
    }
}

