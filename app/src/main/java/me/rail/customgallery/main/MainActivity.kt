package me.rail.customgallery.main

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import me.rail.customgallery.R
import me.rail.customgallery.databinding.ActivityMainBinding
import me.rail.customgallery.main.permission.SettingsOpener
import me.rail.customgallery.media.MediaHandler
import me.rail.customgallery.screens.albumlist.AlbumListFragment
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import javax.inject.Inject
import kotlin.system.exitProcess


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Array<String>>
    private var permissionGrantedGallery: Boolean = false
    private var permissionGrantedCamera: Boolean = false

    @Inject
    lateinit var navigator: Navigator


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                permissionGrantedGallery = permissions[Manifest.permission.READ_EXTERNAL_STORAGE]
                    ?: permissionGrantedGallery
                permissionGrantedCamera =
                    permissions[Manifest.permission.CAMERA] ?: permissionGrantedCamera
                if (permissionGrantedGallery && permissionGrantedCamera) {
                    showMedia()
                } else {
                    showAlert()
                }
            }
        requestPermission()
    }

    private fun requestPermission() {
        permissionGrantedGallery = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        permissionGrantedCamera = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        var permissionRequest: MutableList<String> = ArrayList()
        if (!permissionGrantedGallery) {
            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (!permissionGrantedCamera) {
            permissionRequest.add(Manifest.permission.CAMERA)
        }
        if (permissionRequest.isNotEmpty()) {
            activityResultLauncher.launch(permissionRequest.toTypedArray())
        } else {
            showMedia()
        }
    }

    private fun showMedia() {
        val mediaHandler = MediaHandler()
        mediaHandler.findMedia(applicationContext)

        navigator.replaceFragment(R.id.container, AlbumListFragment())
    }

    private fun quitApp() {
        this@MainActivity.finish()
        exitProcess(0)
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.permission_alert_dialogue_title)
        builder.setMessage(R.string.permission_alert_dialogue_description)

        builder.setPositiveButton(R.string.settings) { dialog, which ->
            SettingsOpener.openSettings(this, Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            quitApp()
        }

        builder.setNegativeButton(R.string.exit) { dialog, which ->
            quitApp()
        }

        builder.show()

    }

    private var mUri: Uri? = null
    private val OPERATION_CAPTURE_PHOTO = 1
    fun capturePhoto() {
        println("********IMAGE STORAGE PATH -  $externalCacheDir*********")
        val capturedImage = File(externalCacheDir, "${System.currentTimeMillis()}.jpg")
        if (capturedImage.exists()) {
            capturedImage.delete()
        }
        capturedImage.createNewFile()
        mUri = if (Build.VERSION.SDK_INT >= 24) {
            FileProvider.getUriForFile(
                this, "me.rail.customgallery.provider",
                capturedImage
            )
        } else {
            Uri.fromFile(capturedImage)
        }

        val intent = Intent("android.media.action.IMAGE_CAPTURE")
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri)
        startActivityForResult(intent, OPERATION_CAPTURE_PHOTO)
        try {
            mUri?.let {
                if (Build.VERSION.SDK_INT < 28) {
                    val bitmap = MediaStore.Images.Media.getBitmap(
                        this.contentResolver,
                        it
                    )
                    saveMediaToStorage(bitmap)
                } else {
                    val source = ImageDecoder.createSource(this.contentResolver, it)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                    saveMediaToStorage(bitmap)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // this method saves the image to gallery
    private fun saveMediaToStorage(bitmap: Bitmap) {
        // Generating a file name
        val filename = "${System.currentTimeMillis()}.jpg"

        // Output stream
        var fos: OutputStream? = null

        // For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // getting the contentResolver
            this.contentResolver?.also { resolver ->

                // Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {

                    // putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                // Inserting the contentValues to
                // contentResolver and getting the Uri
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                // Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            // These for devices running on android < Q
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            // Finally writing the bitmap to the output stream that we opened
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this, "Captured View and saved to Gallery", Toast.LENGTH_SHORT).show()
        }
    }
}