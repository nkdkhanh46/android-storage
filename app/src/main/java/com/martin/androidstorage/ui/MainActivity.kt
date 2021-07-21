package com.martin.androidstorage.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.martin.androidstorage.data.storage.ExternalStorageManager
import com.martin.androidstorage.utils.IntentUtils
import com.martin.androidstorage.R
import com.martin.androidstorage.databinding.ActivityMainBinding
import com.martin.androidstorage.extensions.toSharedUri
import java.io.File
import java.lang.RuntimeException

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
            openImagePicker()
        }
        binding.btCapturePhoto.setOnClickListener {
            openCamera()
        }
        binding.btShare.setOnClickListener {
            showImageSharingSelection()
        }
    }

    private fun showImageSharingSelection() {
        val imageUri = imageUri ?: return

        try {
            val chooser = IntentUtils.createImageSharingChooserIntent(
                imageUri, getString(R.string.share_image_chooser_title)
            )
            startActivity(chooser)
        } catch (ex: RuntimeException) {
            showMessage(R.string.message_share_image_no_app)
        }
    }

    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { resultUri ->
        imageUri = resultUri ?: return@registerForActivityResult

        updatePreviewImage()
        saveImage()
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

    private fun openImagePicker() {
        photoPickerLauncher.launch(IntentUtils.MIME_DATA_TYPE_IMAGE)
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
        imageUri = File(dir, fileName).toSharedUri(this)
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