package com.martin.androidstorage

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.martin.androidstorage.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private val externalStorageManager: ExternalStorageManager by lazy { ExternalStorageManager() }
    private lateinit var binding: ActivityMainBinding
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViewEvents()
    }

    private fun setupViewEvents() {
        binding.btPickImage.setOnClickListener {
            openPhotoPicker()
        }
        binding.btCapturePhoto.setOnClickListener {
            openCamera()
        }
    }

    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            updatePreviewImage()
            saveImage()
        }
    }

    private fun saveImage() {
        val imageUri = imageUri ?: return

        try {
            externalStorageManager.saveImage(this, imageUri, getImagePreviewName())
        } catch (ex: Exception) {
            showMessage(R.string.message_save_picked_image_failed)
        }
    }

    private fun updatePreviewImage() {
        binding.ivPreview.setImageURI(imageUri)
    }

    private val capturePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { result ->
        if (result == true) {
            updatePreviewImage()
        } else {
            showMessage(R.string.message_capture_photo_failed)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        var permissionsNotGranted = false
        for (result in results) {
            if (!result.value) {
                permissionsNotGranted = true
                break
            }
        }

        if (permissionsNotGranted) {
            showMessage(R.string.message_permission_denied)
        } else {
            openCamera()
        }
    }

    private fun showMessage(message: Int) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun openPhotoPicker() {
        val intent = Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = "image/*"
        }
        photoPickerLauncher.launch(intent)
    }

    private fun getImagePreviewName() = "preview_image_${System.currentTimeMillis()}.jpg"

    private fun openCamera() {
        val notGrantedPermissions = getNotGrantedPermissions(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )

        if (notGrantedPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(notGrantedPermissions)
            return
        }

        val fileName = getImagePreviewName()
        val dir = externalStorageManager.getImageDir(this)
        imageUri = FileProvider.getUriForFile(
            this,
            getString(R.string.app_file_authorities),
            File(dir, fileName)
        )
        capturePhotoLauncher.launch(imageUri)
    }

    private fun getNotGrantedPermissions(permissions: Array<String>): Array<String> {
        val notGrantedPermissions = arrayListOf<String>()
        permissions.forEach { permission ->
            if (!permission.isPermissionsGranted(this)) {
                notGrantedPermissions.add(permission)
            }
        }
        return notGrantedPermissions.toArray(arrayOfNulls(notGrantedPermissions.size))
    }

    private fun String.isPermissionsGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, this) == PackageManager.PERMISSION_GRANTED
    }
}