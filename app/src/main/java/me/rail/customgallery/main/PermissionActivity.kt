package me.rail.customgallery.main

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.rail.customgallery.R
import me.rail.customgallery.data.DataHandler
import me.rail.customgallery.data.DataStorage
import me.rail.customgallery.databinding.PermissionActivityBinding
import me.rail.customgallery.main.permission.SettingsOpener
import me.rail.customgallery.screens.albumlist.AlbumListFragment
import java.io.IOException
import java.util.*
import javax.inject.Inject
import kotlin.system.exitProcess


@AndroidEntryPoint
class PermissionActivity() : AppCompatActivity() {
    lateinit var binding: PermissionActivityBinding
    private lateinit var activityResultLauncherPermissionRequest: ActivityResultLauncher<Array<String>>
    private var permissionGrantedGallery: Boolean = false
    private var permissionGrantedCamera: Boolean = false
    private lateinit var takePhoto: ActivityResultLauncher<Void?>
    private lateinit var takeVideo: ActivityResultLauncher<Uri?>
    private var addVideoGallery: Boolean = false
    private var selectionLimit: Boolean = false
    private var selectionLimitCount: Int = 0


    @Inject
    lateinit var navigator: Navigator
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addVideoGallery = intent.getBooleanExtra("addVideoGallery", false)
        selectionLimit = intent.getBooleanExtra("selectionLimitOn", false)
        selectionLimitCount = intent.getIntExtra("selectionLimitCount", 0)
        binding = DataBindingUtil.setContentView(this, R.layout.permission_activity)
        setSupportActionBar(binding.toolbar)
        activityResultLauncherPermissionRequest =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                permissionGrantedGallery = permissions[Manifest.permission.READ_EXTERNAL_STORAGE]
                    ?: permissionGrantedGallery
                permissionGrantedCamera =
                    permissions[Manifest.permission.CAMERA] ?: permissionGrantedCamera
                if (permissionGrantedGallery && permissionGrantedCamera) {
                    CoroutineScope(Dispatchers.IO).launch {
                        showMedia()
                    }
                } else {
                    showAlertOnPermissionDeny()
                }
            }
        requestPermission()
        takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            if (it != null) {

                lifecycleScope.launch {

                    if (savePhotoToExternalStorage(UUID.randomUUID().toString(), it)) {
                        showMedia()
                        Toast.makeText(
                            this@PermissionActivity,
                            R.string.photo_saved,
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {

                        Toast.makeText(
                            this@PermissionActivity,
                            R.string.photo_save_error,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                }
            }
        }
        takeVideo = registerForActivityResult(ActivityResultContracts.TakeVideo()) {
            lifecycleScope.launch() {
                showMedia()
            }
        }
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
            activityResultLauncherPermissionRequest.launch(permissionRequest.toTypedArray())
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                showMedia()
            }
        }
    }

    private suspend fun showMedia() {
        val job = CoroutineScope(Dispatchers.IO).launch {
            val mediaHandler = DataHandler(addVideoGallery)
            mediaHandler.findMedia(applicationContext)
        }
        job.join()
        navigator.replaceFragment(R.id.container, AlbumListFragment(addVideoGallery))
    }

    private fun quitApp() {
        this@PermissionActivity.finish()
        exitProcess(0)
    }

    private fun showAlertOnPermissionDeny() {
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

    fun showAlertSwitchToVideo() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.capture_alert_dialogue_title)
        builder.setMessage(R.string.capture_alert_dialogue_description)

        builder.setPositiveButton(R.string.capture_alert_dialogue_positive) { dialog, which ->
            captureVideo()
        }

        builder.setNegativeButton(R.string.capture_alert_dialogue_negative) { dialog, which ->
            capturePhoto()
        }

        builder.show()

    }

    private fun captureVideo() {
        var mUri: Uri? = null
        takeVideo.launch(mUri)
    }

    fun capturePhoto() {
        takePhoto.launch()
    }


    private fun savePhotoToExternalStorage(name: String, bmp: Bitmap?): Boolean {

        val imageCollection: Uri = if (sdkCheck()) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues().apply {

            put(MediaStore.Images.Media.DISPLAY_NAME, "$name.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (bmp != null) {
                put(MediaStore.Images.Media.WIDTH, bmp.width)
                put(MediaStore.Images.Media.HEIGHT, bmp.height)
            }

        }

        return try {

            contentResolver.insert(imageCollection, contentValues)?.also {

                contentResolver.openOutputStream(it).use { outputStream ->

                    if (bmp != null) {

                        if (!bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {

                            throw IOException(R.string.save_bitmap_error.toString())
                        }
                    }
                }

            } ?: throw IOException(R.string.create_media_store_error.toString())
            true
        } catch (e: IOException) {

            e.printStackTrace()
            false
        }

    }

    private fun sdkCheck(): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true
        }

        return false

    }

    override fun onBackPressed() {
        DataStorage.setAllMediasUnselected()
        binding.button3.visibility = View.INVISIBLE
        super.onBackPressed()
    }
}