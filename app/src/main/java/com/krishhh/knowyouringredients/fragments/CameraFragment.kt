package com.krishhh.knowyouringredients.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.krishhh.knowyouringredients.R
import com.krishhh.knowyouringredients.TextSelectionActivity
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private lateinit var previewView: PreviewView
    private lateinit var imageCapture: ImageCapture
    private var camera: Camera? = null

    private val camPerm = Manifest.permission.CAMERA

    private val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) startCamera() else requireActivity().finish() }

    /** Flash states */
    private enum class FlashMode { OFF, ON, AUTO }
    private var currentFlash = FlashMode.OFF

    /** Gallery launcher */
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val path = uriToFile(it)
            startActivity(TextSelectionActivity.intent(requireContext(), path))
        }
    }

    /** Threshold for AUTO flash */
    private val BRIGHTNESS_THRESHOLD = 50f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_camera, container, false)
        previewView = v.findViewById(R.id.previewView)

        v.findViewById<View>(R.id.fabScan).setOnClickListener { snap() }
        v.findViewById<View>(R.id.fabGallery).setOnClickListener { openGallery() }
        v.findViewById<View>(R.id.fabFlash).setOnClickListener { toggleFlash() }

        return v
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(requireContext(), camPerm)
            == PackageManager.PERMISSION_GRANTED
        ) startCamera() else permLauncher.launch(camPerm)
    }

    private fun startCamera() {
        val fut = ProcessCameraProvider.getInstance(requireContext())
        fut.addListener({
            val provider = fut.get()
            provider.unbindAll()

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

            camera = provider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun snap() {
        // Handle flash at the moment of capture
        if (currentFlash == FlashMode.ON) {
            camera?.cameraControl?.enableTorch(true)
        } else if (currentFlash == FlashMode.AUTO) {
            // Estimate brightness from current preview frame
            previewView.bitmap?.let { bmp ->
                val brightness = estimateBrightness(bmp)
                if (brightness < BRIGHTNESS_THRESHOLD) {
                    camera?.cameraControl?.enableTorch(true)
                }
            }
        }

        imageCapture.takePicture(
            Executors.newSingleThreadExecutor(),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(proxy: ImageProxy) {
                    val bmp = proxy.toRotatedBitmap()
                    proxy.close()
                    camera?.cameraControl?.enableTorch(false) // Turn off torch after capture
                    val path = bmp.save(requireContext().cacheDir)
                    startActivity(TextSelectionActivity.intent(requireContext(), path))
                }

                override fun onError(exception: ImageCaptureException) {
                    toast("Error: ${exception.message}")
                }
            }
        )
    }

    private fun toggleFlash() {
        currentFlash = when (currentFlash) {
            FlashMode.OFF -> FlashMode.ON
            FlashMode.ON -> FlashMode.AUTO
            FlashMode.AUTO -> FlashMode.OFF
        }

        val iconRes = when (currentFlash) {
            FlashMode.OFF -> R.drawable.flash_off_30dp
            FlashMode.ON -> R.drawable.flash_on_30dp
            FlashMode.AUTO -> R.drawable.flash_auto_30dp
        }

        view?.findViewById<ImageButton>(R.id.fabFlash)?.setImageResource(iconRes)
    }

    private fun openGallery() = galleryLauncher.launch("image/*")

    /** Helpers */

    // Camera capture bitmap rotation
    private fun ImageProxy.toRotatedBitmap(): Bitmap {
        val buf: ByteBuffer = planes[0].buffer
        val bytes = ByteArray(buf.remaining()).also { buf.get(it) }
        val src = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val m = Matrix().apply { postRotate(imageInfo.rotationDegrees.toFloat()) }
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, true)
    }

    // Save bitmap to file
    private fun Bitmap.save(dir: File): String {
        val f = File(dir, "snap_${System.currentTimeMillis()}.jpg")
        FileOutputStream(f).use { compress(Bitmap.CompressFormat.JPEG, 90, it) }
        return f.absolutePath
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    // Gallery image rotation handled via EXIF
    private fun uriToFile(uri: android.net.Uri): String {
        val inputStream = requireContext().contentResolver.openInputStream(uri)!!
        val tempFile = File(requireContext().cacheDir, "gallery_${System.currentTimeMillis()}.jpg")
        FileOutputStream(tempFile).use { it.write(inputStream.readBytes()) }

        // Read EXIF orientation
        val exif = ExifInterface(tempFile.absolutePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        if (rotationDegrees != 0f) {
            val bitmap = BitmapFactory.decodeFile(tempFile.absolutePath)
            val matrix = Matrix().apply { postRotate(rotationDegrees) }
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            FileOutputStream(tempFile).use { rotated.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        }

        return tempFile.absolutePath
    }

    // Estimate brightness for AUTO flash
    private fun estimateBrightness(bitmap: Bitmap): Float {
        var sum = 0L
        for (y in 0 until bitmap.height step 10) {
            for (x in 0 until bitmap.width step 10) {
                val pixel = bitmap.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                sum += (r + g + b) / 3
            }
        }
        val count = (bitmap.width / 10) * (bitmap.height / 10)
        return if (count > 0) sum.toFloat() / count else 255f
    }
}
