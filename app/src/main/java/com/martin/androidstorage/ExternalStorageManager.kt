package com.martin.androidstorage

import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.*
import java.lang.Exception
import java.lang.RuntimeException

class ExternalStorageManager {

    /**
     * Get app default Pictures directory in the external storage
     * @param context: application context
     */
    fun getImageDir(context: Context): File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

    /**
     * Save an image into app default Pictures directory
     * @param context: application context
     * @param sourceUri: URI of the source file
     * @param destFileName: name of the file when saving into the Pictures directory
     *
     * @return the file in the Pictures directory if success
     */
    @Throws(FileNotFoundException::class, IOException::class, RuntimeException::class)
    fun saveImage(context: Context, sourceUri: Uri, destFileName: String): File {
        val dir = getImageDir(context)
        return saveFile(context, sourceUri, dir, destFileName)
    }

    @Throws(FileNotFoundException::class, IOException::class, RuntimeException::class)
    fun saveFile(context: Context, sourceUri: Uri, destDir: File?, destFileName: String): File {
        if (destDir?.exists() == false) {
            destDir.mkdirs()
        }

        val destFile = File(destDir, destFileName)
        var inputStream: BufferedInputStream? = null
        var outputStream: BufferedOutputStream? = null
        try {
            inputStream = BufferedInputStream(context.contentResolver.openInputStream(sourceUri))
            outputStream = BufferedOutputStream(FileOutputStream(destFile))
            val buffer = ByteArray(1024)
            inputStream.read(buffer)
            do {
                outputStream.write(buffer)
            } while (inputStream.read(buffer) != -1)
        } catch (ex: Exception) {
            throw ex
        } finally {
            try {
                inputStream?.close()
                outputStream?.flush()
                outputStream?.close()
            } catch (ex: Exception) {
                throw ex
            }
        }

        return destFile
    }
}