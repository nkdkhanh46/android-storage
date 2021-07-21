package com.martin.androidstorage.extensions

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.martin.androidstorage.R
import java.io.File

fun File.toSharedUri(context: Context): Uri {
    return FileProvider.getUriForFile(
        context,
        context.getString(R.string.app_file_authorities),
        this
    )
}