package com.example.arsample

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.TransformableNode

class NewScreen : AppCompatActivity() {

    private lateinit var arFragment: ArFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_screen)

        // Find the ARFragment directly and assign it to arFragment

        val arFragmentScreen = findViewById<FrameLayout>(R.id.arFragmentContainerNew)
        arFragment = supportFragmentManager.findFragmentById(arFragmentScreen.id) as? ArFragment
            ?: ArFragment()

        if (arFragment.isAdded) {
            supportFragmentManager.beginTransaction()
                .show(arFragment)
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(arFragmentScreen.id, arFragment)
                .commit()
        }


        // Set the tap listener for AR planes
        arFragment?.setOnTapArPlaneListener { hitResult, _, _ ->
            placeObject(hitResult.createAnchor())
        }
    }

    private fun placeObject(anchor: Anchor?) {
        ModelRenderable.builder()
            .setSource(this, Uri.parse("animal_ninja.glb"))
            .build()
            .thenAccept { renderable -> addNodeToScene(anchor, renderable) }
//            .exceptionally { throwable ->
//                // Handle renderable load failure
//                return@exceptionally null
//            }
        .exceptionally { throwable ->
                Log.e("ModelLoad", "Error loading model", throwable)
                // Handle renderable load failure
                return@exceptionally null
            }
    }

    private fun addNodeToScene(anchor: Anchor?, renderable: ModelRenderable?) {
        val anchorNode = AnchorNode(anchor)
        val transformableNode = TransformableNode(arFragment?.transformationSystem)
        transformableNode.renderable = renderable
        transformableNode.setParent(anchorNode)
        arFragment?.arSceneView?.scene?.addChild(anchorNode)
        transformableNode.select()
    }
}
