package com.martin.androidstorage.utils

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import java.lang.RuntimeException
import kotlin.jvm.Throws

class IntentUtils {
    companion object {

        const val MIME_DATA_TYPE_IMAGE = "image/*"

        /**
         * Create an app chooser for sharing an image
         *
         * @param imageUri: URI of the sharing image
         *
         * @param title: title of the chooser dialog
         *
         * @return an intent which should be called using [AppCompatActivity.startActivity]
         */
        @Throws(RuntimeException::class)
        fun createImageSharingChooserIntent(imageUri: Uri, title: String): Intent {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = MIME_DATA_TYPE_IMAGE
                putExtra(Intent.EXTRA_STREAM, imageUri)
            }
            return Intent.createChooser(intent, title)
        }
    }
}