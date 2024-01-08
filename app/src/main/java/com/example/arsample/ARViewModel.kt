package com.example.arsample
// ARViewModel.kt
// ARViewModel.kt

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.ar.core.Anchor
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ARViewModel(application: Application) : AndroidViewModel(application) {

    private val _modelRenderable = MutableLiveData<ModelRenderable>()
    val modelRenderable: LiveData<ModelRenderable> = _modelRenderable
    var currentAnchor: Anchor? = null
    fun placeModel(anchor: Anchor) {
        currentAnchor = anchor
        viewModelScope.launch {
            try {
                val rendererSource = withContext(Dispatchers.IO) {
                    RenderableSource.builder()
                        .setSource(
                            getApplication(),
                            Uri.parse("tom_dancing.glb"),
                            RenderableSource.SourceType.GLB
                        )
                        .setScale(0.3f)
                        .build()
                }

                val model = withContext(Dispatchers.Main) {
                    ModelRenderable.builder()
                        .setSource(getApplication(), rendererSource)
                        .build()
                        .await()
                }

                _modelRenderable.postValue(model)
            } catch (e: Exception) {
                // Handle model loading error
            }
        }
    }
}
