
package com.android.android.storage.ui.images

import android.app.Application
import android.database.ContentObserver
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.android.storage.FileOperations
import com.android.android.storage.hasSdkHigherThan
import com.android.android.storage.model.Media
import com.android.android.storage.registerObserver
import com.android.android.storage.ui.MainAction
import com.android.android.storage.ui.ModificationType
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

  private val _actions = MutableLiveData<MainAction>()
  val actions: LiveData<MainAction> get() = _actions

  private var contentObserver: ContentObserver? = null

  fun loadImages() {
    viewModelScope.launch {
      val imageList = FileOperations.queryImagesOnDevice(getApplication<Application>())
      _actions.postValue(MainAction.ImagesChanged(imageList))

      if (contentObserver == null) {
        contentObserver = getApplication<Application>().contentResolver.registerObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI) {
          loadImages()
        }
      }
    }
  }

  fun loadVideos() {
    viewModelScope.launch {
      val videosList = FileOperations.queryVideosOnDevice(getApplication<Application>())
      _actions.postValue(MainAction.VideosChanged(videosList))
    }
  }

  @RequiresApi(Build.VERSION_CODES.R)
  fun loadFavorites() {
    viewModelScope.launch {
      val mediaList = FileOperations.queryFavoriteMedia(getApplication<Application>())
      _actions.postValue(MainAction.FavoriteChanged(mediaList))
    }
  }

  @RequiresApi(Build.VERSION_CODES.R)
  fun loadTrashed() {
    viewModelScope.launch {
      val mediaList = FileOperations.queryTrashedMedia(getApplication<Application>())
      _actions.postValue(MainAction.TrashedChanged(mediaList))
    }
  }

  fun requestStoragePermissions() {
    _actions.postValue(MainAction.StoragePermissionsRequested)
  }

  fun deleteMedia(media: List<Media>) {
    if (hasSdkHigherThan(Build.VERSION_CODES.Q) && media.size > 1) {
      val intentSender = FileOperations.deleteMediaBulk(getApplication<Application>(), media)
      _actions.postValue(MainAction.ScopedPermissionRequired(intentSender, ModificationType.DELETE))
    } else {
      viewModelScope.launch {
        for (item in media) {
          val intentSender = FileOperations.deleteMedia(getApplication<Application>(), item)
          if (intentSender != null) {
            _actions.postValue(
                MainAction.ScopedPermissionRequired(
                    intentSender,
                    ModificationType.DELETE
                )
            )
          }
        }
      }
    }
  }

  fun requestFavoriteMedia(media: List<Media>, state: Boolean) {
    val intentSender = FileOperations.addToFavorites(getApplication<Application>(), media, state)
    _actions.postValue(MainAction.ScopedPermissionRequired(intentSender, ModificationType.FAVORITE))
  }

  fun requestTrashMedia(media: List<Media>, state: Boolean) {
    val intentSender = FileOperations.addToTrash(getApplication<Application>(), media, state)
    _actions.postValue(MainAction.ScopedPermissionRequired(intentSender, ModificationType.TRASH))
  }
}