package com.altankoc.socialmedia.beuverse.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

object ImageUtils {

    fun checkAndRequestPermission(
        activity: Activity,
        view: View,
        permissionLauncher: ActivityResultLauncher<String>,
        onPermissionGranted: () -> Unit
    ) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                Snackbar.make(view, "Galeriye erişmek için izin gerekli", Snackbar.LENGTH_INDEFINITE)
                    .setAction("İzin Ver") {
                        permissionLauncher.launch(permission)
                    }.show()
            } else {
                permissionLauncher.launch(permission)
            }
        } else {
            onPermissionGranted()
        }
    }

    fun openGallery(activityResultLauncher: ActivityResultLauncher<Intent>) {
        val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activityResultLauncher.launch(intentToGallery)
    }

    fun getBitmapFromUri(context: Context, imageUri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= 28) {
                val source = ImageDecoder.createSource(context.contentResolver, imageUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
