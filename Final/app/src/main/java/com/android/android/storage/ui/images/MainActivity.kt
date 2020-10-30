

package com.android.android.storage.ui.images

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.android.android.storage.R

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    // Switch to AppTheme for displaying the activity
    setTheme(R.style.AppTheme)

    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
  }

  override fun onSupportNavigateUp(): Boolean {
    return findNavController(R.id.navHostFragment).navigateUp()
  }
}
