package com.example.arsample

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer


class ArFragment : ArFragment() {

    private var modelPlaced = false
    val name =""

    override fun getSessionConfiguration(session: Session): Config {
        val config = super.getSessionConfiguration(session)
        config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
        return config
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadModel()
    }

    private fun loadModel() {
        val modelUri = Uri.parse("models/scene.gltf")
        val fileName="scene"
        val path=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val newDir=File(path,fileName)
        Log.e("path", "path:$path" )

        try {
            if (!newDir.exists()){
                newDir.mkdir()
            }
        }catch (e:Exception){

        }
        val assetManager = requireContext().assets

// Load SFB model synchronously
        val sfbModelUri = Uri.parse("models/scene.sfb")
        val sfbModelRenderable = ModelRenderable.builder()
            .setSource(requireContext(), sfbModelUri)
            .build()

        Log.e("sfbModelRenderable", "loadModel: $sfbModelRenderable")
// Check if the SFB model is placed, and if not, load the GLTF model asynchronously
        if (!modelPlaced) {
            val gltfModelUri = Uri.parse("models/scene.gltf")
            ModelRenderable.builder()
                .setSource(requireContext(), gltfModelUri)
                .build()
                .thenAccept { gltfRenderable ->
                    // This block runs asynchronously when the GLTF model is loaded.
                    // Ensure that the GLTF model is placed on the AR scene within this block.
                    placeRenderable(gltfRenderable)
                }
        }

    }

    private fun placeRenderable(renderable: ModelRenderable) {
        val anchor = arSceneView.session?.createAnchor(arSceneView.arFrame?.hitTest(0.5f, 0.5f)?.get(0)?.hitPose)
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(arSceneView.scene)

        val modelNode = TransformableNode(transformationSystem)
        modelNode.setParent(anchorNode)
        modelNode.renderable = renderable
        modelNode.select()

        modelPlaced = true
    }

    fun placeModel() {
        loadModel()
    }
}


