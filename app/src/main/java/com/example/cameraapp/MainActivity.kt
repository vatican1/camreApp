package com.example.cameraapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Display
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.io.File


class MainActivity : AppCompatActivity() {
    private val PERMISSION_REQUEST_CAMERA = 239
    private val PERMISSION_REQUEST_STORAGE = 2390

    private lateinit var processCameraProvider: ListenableFuture<ProcessCameraProvider>
    private lateinit var imageCapture: ImageCapture
    private var pattern = ""
    private var amountPhotos = 0

    companion object {
        var MAINPATTERN = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        amountPhotos = 0

        val pattern1 = intent.getStringExtra(MAINPATTERN)
        Log.d("ACT2", "Create" + pattern1.toString())
        if (!pattern1.isNullOrEmpty()) pattern = pattern1.toString()
        R.string.app_name
        if (needToRequestPermission(Manifest.permission.CAMERA, PERMISSION_REQUEST_CAMERA)) {
            initCamera()
        }
        needToRequestPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            PERMISSION_REQUEST_STORAGE
        )

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.d("LOG_TAG", "onRestoreInstanceState pattern = $pattern")
        amountPhotos = savedInstanceState.getInt("amountPhotos")
        findViewById<TextView>(R.id.amount).text = amountPhotos.toString()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d("LOG_TAG", "onSaveInstanceState")
        outState.putInt("amountPhotos", amountPhotos)
    }

    private fun needToRequestPermission(permission: String, permissionInt: Int): Boolean {
        if (ContextCompat.checkSelfPermission(this, permission) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, Array(1) { permission },
                permissionInt
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CAMERA && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            initCamera()
        }
    }

    private fun initCamera() {
        processCameraProvider = ProcessCameraProvider.getInstance(this)
        processCameraProvider.addListener(Runnable {
            var cameraProvider = processCameraProvider.get()

            var preview: Preview = Preview.Builder().build()
            var cameraSelector: CameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            val display: Display =
                (this.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay

            imageCapture = ImageCapture.Builder()
                .setTargetRotation(display.rotation)
                .build()

            preview.setSurfaceProvider(findViewById<PreviewView>(R.id.viewFinder).surfaceProvider)
            cameraProvider.bindToLifecycle(
                this as LifecycleOwner,
                cameraSelector,
                imageCapture,
                preview
            )


        }, ContextCompat.getMainExecutor(this))

    }


    fun setFileName(): String {
        val parts = pattern.split("%d")
        var name = ""
        Log.d("ACT2", "parts[0] = " + parts[0] + "\n")

        var insertNumber = ""
        if (amountPhotos < 10) insertNumber = "00$amountPhotos"
        if ((amountPhotos >= 10) && (amountPhotos < 100)) insertNumber = "0$amountPhotos"
        if (amountPhotos >= 1000) insertNumber = "$amountPhotos"

        if (parts.size == 1) name = parts[0] + insertNumber
        if (parts.size == 2) name = parts[0] + insertNumber + parts[1]
        name = "$name.jpg"
        return name
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun takePhoto(view: View) {

        if (pattern == "") {
            Toast.makeText(applicationContext, "Set file name", Toast.LENGTH_SHORT).show()
            return
        }

        var name = setFileName()
        amountPhotos++

        findViewById<TextView>(R.id.amount).text = amountPhotos.toString()


        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            .toString()

        val myImageDir = File("$path/CameraApp", name)
        myImageDir.exists() || myImageDir.mkdirs()
        Log.d("PHOTO", "$path/CameraApp/$name")

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(myImageDir)
            .build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(error: ImageCaptureException) {
                    Log.d("PHOTO", "Photo : error\n")
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.d("PHOTO", "Photo : Ok\n")
                }
            })
    }

    fun toSecondActivity(view: View) {
        val myIntent = Intent(this, SecondActivity::class.java)
        myIntent.putExtra(SecondActivity.PATTERN, pattern)
        startActivity(myIntent)
    }

}