package com.admin.portal.Employee

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.util.Size
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.admin.portal.Utils.ScanUtils.*
import com.admin.portal.databinding.ActivityScannerBinding
import com.google.mlkit.common.MlKitException


class ScannerActivity : AppCompatActivity() {
    lateinit var binding: ActivityScannerBinding

    private val TAG = "CameraXLivePreview"
    private val PERMISSION_REQUESTS = 1

    private val REQUEST_CHOOSE_IMAGE = 1002

    private val BARCODE_SCANNING = "Barcode Scanning"


    private val STATE_SELECTED_MODEL = "selected_model"
    private val STATE_LENS_FACING = "lens_facing"

    private var previewView: PreviewView? = null
    private var graphicOverlay: GraphicOverlay? = null

    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null
    private var analysisUseCase: ImageAnalysis? = null
    private var imageProcessor: VisionImageProcessor? = null
    private var needUpdateGraphicOverlayImageSourceInfo = false

    private val selectedModel = BARCODE_SCANNING
    private val lensFacing = CameraSelector.LENS_FACING_BACK
    private var cameraSelector: CameraSelector? = null

    var flash_btn: ImageView? = null
    var backbtn: ImageView? = null
    private val mFlash = false
    var camera: Camera? = null
    var view: View? = null


    private val SIZE_SCREEN = "w:screen" // Match screen width

    private val SIZE_1024_768 = "w:1024" // ~1024*768 in a normal ratio

    private val SIZE_640_480 = "w:640" // ~640*480 in a normal ratio


    private val selectedMode = BARCODE_SCANNING
    private val selectedSize = SIZE_SCREEN

    var isLandScape = false
    var scantype = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val intent = intent
        scantype = intent.getStringExtra("scantype").toString()



        previewView= binding.previewView
        graphicOverlay= binding.graphicOverlay

        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )
            .get(CameraXViewModel::class.java)
            .processCameraProvider
            .observe(this) { provider ->
                cameraProvider = provider
                if (allPermissionsGranted()) {
                    bindAllCameraUseCases()
                }
            }


        if (!allPermissionsGranted()) {
            getRuntimePermissions()
        }



    }


    override fun onResume() {
        super.onResume()
        bindAllCameraUseCases()
    }

    override fun onPause() {
        super.onPause()
        if (imageProcessor != null) {
            imageProcessor?.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (imageProcessor != null) {
            imageProcessor?.stop()
        }
    }

    private fun bindAllCameraUseCases() {
        if (cameraProvider != null) {
            // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
            cameraProvider?.unbindAll()
            bindPreviewUseCase()
            bindAnalysisUseCase()
        }
    }

    private fun bindPreviewUseCase() {
        if (!PreferenceUtils.isCameraLiveViewportEnabled(this)) {
            return
        }
        if (cameraProvider == null) {
            return
        }
        if (previewUseCase != null) {
            cameraProvider?.unbind(previewUseCase)
        }
        val builder = Preview.Builder()
        val targetResolution: Size? = PreferenceUtils.getCameraXTargetResolution( lensFacing)
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution)
        }
        previewUseCase = builder.build()
        previewUseCase?.setSurfaceProvider(previewView?.surfaceProvider)
        try {
            if (cameraSelector != null) {
                if (previewUseCase != null) {
                    cameraProvider?.bindToLifecycle( /* lifecycleOwner= */this,
                        cameraSelector!!,
                        previewUseCase
                    )
                }
            } else {
                Toast.makeText(
                    this,
                    "No Camera Found. Please try again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Log.e(
                TAG,
                e.message!!
            )
        }
    }

    private fun bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return
        }
        if (analysisUseCase != null) {
            cameraProvider?.unbind(analysisUseCase)
        }
        if (imageProcessor != null) {
            imageProcessor?.stop()
        }
        try {
            when (selectedModel) {
                BARCODE_SCANNING -> {
                    Log.i(
                        TAG,
                        "Using Barcode Detector Processor"
                    )
                    imageProcessor =
                        BarcodeScannerProcessor(this, this,scantype)
                }
                else -> throw IllegalStateException("Invalid model name")
            }
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Can not create image processor: $selectedModel", e
            )
            return
        }
        val builder = ImageAnalysis.Builder()
        val targetResolution: Size?=
            PreferenceUtils.getCameraXTargetResolution( lensFacing)
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution)
        }
        analysisUseCase = builder.build()
        needUpdateGraphicOverlayImageSourceInfo = true
        analysisUseCase?.setAnalyzer( // imageProcessor.processImageProxy will use another thread to run the detection underneath,
            // thus we can just runs the analyzer itself on main thread.
            ContextCompat.getMainExecutor(this),
            ImageAnalysis.Analyzer { imageProxy: ImageProxy ->
                if (needUpdateGraphicOverlayImageSourceInfo) {
                    val isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                    if (rotationDegrees == 0 || rotationDegrees == 180) {
                        graphicOverlay!!.setImageSourceInfo(
                            imageProxy.width, imageProxy.height, isImageFlipped
                        )
                    } else {
                        graphicOverlay!!.setImageSourceInfo(
                            imageProxy.height, imageProxy.width, isImageFlipped
                        )
                    }
                    needUpdateGraphicOverlayImageSourceInfo = false
                }
                try {
                    imageProcessor!!.processImageProxy(imageProxy, graphicOverlay)
                } catch (e: MlKitException) {
                    Log.e(
                        TAG,
                        "Failed to process image. Error: " + e.localizedMessage
                    )
                    Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT)
                        .show()
                }
            })
        try {
            if (cameraSelector != null) {
                if (analysisUseCase != null) {
                    camera = cameraProvider?.bindToLifecycle( /* lifecycleOwner= */this,
                        cameraSelector!!,
                        analysisUseCase
                    )
                }
            } else {
                Toast.makeText(
                    this,
                    "No Camera Found. Please try again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Log.e(
                TAG,
                e.message!!
            )
        }


//        cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, analysisUseCase);
    }

    private fun getRequiredPermissions(): Array<String?> {
        return try {
            val info: PackageInfo = packageManager
                .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
            val ps = info.requestedPermissions
            if (ps != null && ps.size > 0) {
                ps
            } else {
                arrayOfNulls(0)
            }
        } catch (e: Exception) {
            arrayOfNulls(0)
        }
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false
            }
        }
        return true
    }

    private fun getRuntimePermissions() {
        val allNeededPermissions: MutableList<String?> = ArrayList()
        for (permission in getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission)
            }
        }
        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                allNeededPermissions.toTypedArray(),
                PERMISSION_REQUESTS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        Log.i(
            TAG,
            "Permission granted!"
        )
        if (allPermissionsGranted()) {
            bindAllCameraUseCases()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

//   override fun onRequestPermissionsResult(
//        requestCode: Int, permissions: Array<String?>?, grantResults: IntArray?
//    ) {
//        Log.i(
//             TAG,
//            "Permission granted!"
//        )
//        if (allPermissionsGranted()) {
//            bindAllCameraUseCases()
//        }
//        super.onRequestPermissionsResult(requestCode, permissions!!, grantResults!!)
//    }

    private fun isPermissionGranted(context: Context, permission: String?): Boolean {
        if (ContextCompat.checkSelfPermission(context, permission!!)
            == PackageManager.PERMISSION_GRANTED
        ) {
            Log.i(
                TAG,
                "Permission granted: $permission"
            )
            return true
        }
        Log.i(
            TAG,
            "Permission NOT granted: $permission"
        )
        return false
    }
}