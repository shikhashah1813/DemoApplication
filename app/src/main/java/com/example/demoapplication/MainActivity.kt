package com.example.demoapplication

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.SparseArray
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.isNotEmpty
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private val requestCodeCameraPermission = 1001
    private lateinit var cameraSource: CameraSource
    private lateinit var detector: BarcodeDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //check if user granted camera permission
        if(ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            ) {
            askForCameraPermission()
        } else {
            setupControls()
        }
    }

    private fun setupControls() {
        //build detector
        detector = BarcodeDetector.Builder(this@MainActivity).build()
        //build camera source with detector
        cameraSource = CameraSource.Builder(this@MainActivity, detector)
            .setAutoFocusEnabled(true)
            .build()
        // set callback value for surfaceview
        cameraSurfaceView.holder.addCallback(surfaceCallBack)
        // set processor value for detector
        detector.setProcessor(processor)
    }

    //prompt user for camera permission
    private fun askForCameraPermission() {
        ActivityCompat.requestPermissions(this@MainActivity,
            arrayOf(Manifest.permission.CAMERA),
            requestCodeCameraPermission)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //if request code matches 1001 and grant results exists
        if(requestCode == requestCodeCameraPermission && grantResults.isNotEmpty()) {
            //if grant results show user has granted permission
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupControls()
            } else {
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val surfaceCallBack = object : SurfaceHolder.Callback {
        override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        }

        override fun surfaceDestroyed(holder: SurfaceHolder?) {
            cameraSource.stop()
        }

        override fun surfaceCreated(holder: SurfaceHolder?) {
            try {
                //start camera source on surface view holder
                cameraSource.start(holder)
            }catch (exception: Exception) {
                Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private val processor = object : Detector.Processor<Barcode>{
        override fun release() {
        }

        override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
            //check if camera detects barcode
            if (detections != null && detections.detectedItems.isNotEmpty()) {
                // set detected items to value
                val qrCodes: SparseArray<Barcode> = detections.detectedItems
                // set value of detected items to value
                val code = qrCodes.valueAt(0)
                // display value as text
                textScanResult.text = code.displayValue
            } else {
                textScanResult.text = ""
            }
        }
    }
}
