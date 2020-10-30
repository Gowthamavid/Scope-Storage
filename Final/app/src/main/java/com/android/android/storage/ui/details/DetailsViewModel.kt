

package com.android.android.storage.ui.details

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.android.storage.FileOperations
import com.android.android.storage.getImageFormat
import com.android.android.storage.model.Media
import com.android.android.storage.ui.ImageDetailAction
import com.android.android.storage.ui.ModificationType
import kotlinx.coroutines.launch

class DetailsViewModel(application: Application) : AndroidViewModel(application) {

  private val _actions = MutableLiveData<ImageDetailAction>()
  val actions: LiveData<ImageDetailAction> get() = _actions

  fun saveImage(image: Media, uri: Uri?, bitmap: Bitmap) {
    viewModelScope.launch {
      val type = getApplication<Application>().contentResolver.getType(image.uri)
      val format = getImageFormat(type!!)

      if (uri == null) {
        FileOperations.saveImage(getApplication(), bitmap, format)
      } else {
        FileOperations.saveImage(getApplication(), uri, bitmap, format)
      }

      _actions.postValue(ImageDetailAction.ImageSaved)
    }
  }

  fun updateImage(image: Media, bitmap: Bitmap) {
    viewModelScope.launch {
      val type = getApplication<Application>().contentResolver.getType(image.uri)
      val format = getImageFormat(type!!)

      val intentSender = FileOperations.updateImage(
          getApplication(), image.uri, bitmap, format)

      if (intentSender == null) {
        _actions.postValue(ImageDetailAction.ImageUpdated)
      } else {
        _actions.postValue(
            ImageDetailAction.ScopedPermissionRequired(
                intentSender,
                ModificationType.UPDATE
            )
        )
      }
    }
  }

  fun deleteImage(image: Media) {
    viewModelScope.launch {
      val intentSender = FileOperations.deleteMedia(getApplication(), image)
      if (intentSender == null) {
        _actions.postValue(ImageDetailAction.ImageDeleted)
      } else {
        _actions.postValue(
            ImageDetailAction.ScopedPermissionRequired(
                intentSender,
                ModificationType.DELETE
            )
        )
      }
    }
  }
}