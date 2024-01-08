package com.example.arsample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.ar.core.Anchor
import com.google.ar.core.ArCoreApk
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await

class MainActivity : AppCompatActivity() {
    private var arFragment: ArFragment? = null
    private val arViewModel: ARViewModel by viewModels()
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val arFragmentContainer = findViewById<FrameLayout>(R.id.arFragmentContainer)

        arFragment = supportFragmentManager.findFragmentById(arFragmentContainer.id) as? ArFragment
            ?: ArFragment()

        arFragment!!.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane?, motionEvent: MotionEvent? ->
            if (hitResult.trackable != null && hitResult.trackable is Plane) {
                try {
                    arViewModel.placeModel(hitResult.createAnchor())
                } catch (e: Exception) {
                    Log.e("$TAG arFragment catch", "Error placing model", e)
                    showToast("Error placing model")
                }
            } else {
                Log.e("$TAG else", "Invalid hit result")
                showToast("Invalid hit result")
            }
        }

        arViewModel.modelRenderable.observe(this, Observer { modelRenderable ->
            modelRenderable?.let { model ->
                createAndPlaceModel(model)
            }
        })

        if (checkARSupport()) {
            showARFragment()
        } else {
            handleARNotSupported()
        }
    }

    private fun createAndPlaceModel(model: ModelRenderable) {
        val anchorNode = AnchorNode(arViewModel.currentAnchor)
        anchorNode.setParent(arFragment!!.arSceneView.scene)

        val modelNode = TransformableNode(arFragment!!.transformationSystem)
        modelNode.setParent(anchorNode)
        modelNode.renderable = model

        val material = model.material
        material.setFloat4(MaterialFactory.MATERIAL_COLOR, Color(1.0f, 1.0f, 1.0f, 1.0f))

        modelNode.select()
    }

    private fun checkARSupport(): Boolean {
        return ArCoreApk.getInstance()
            .checkAvailability(this) == ArCoreApk.Availability.SUPPORTED_INSTALLED
    }

    private fun showARFragment() {
        if (arFragment!!.isAdded) {
            supportFragmentManager.beginTransaction()
                .show(arFragment!!)
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.arFragmentContainer, arFragment!!)
                .commit()
        }
    }

    private fun handleARNotSupported() {
        promptARCoreInstallation()
        showToast("AR not supported on this device")
    }

    private fun promptARCoreInstallation() {
        val arCoreApk = ArCoreApk.getInstance()
        val installStatus = arCoreApk.requestInstall(this, true)

        if (installStatus != ArCoreApk.InstallStatus.INSTALLED) {
            startARCoreInstallationActivity()
        }
    }

    private fun startARCoreInstallationActivity() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("market://details?id=com.google.ar.core")
        intent.resolveActivity(packageManager)?.let {
            startActivity(intent)
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }
    }
}
