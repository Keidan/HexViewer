package fr.ralala.hexviewer.ui.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Base activity
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class BaseActivity extends AppCompatActivity {
  private ApplicationCtx mApp;

  /**
   * Called when the activity is created.
   *
   * @param savedInstanceState Bundle
   */
  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    mApp = ((ApplicationCtx) getApplication());
    mApp.applyThemeFromSettings();
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_base);
    Toolbar toolbar = findViewById(R.id.base_toolbar);
    setSupportActionBar(toolbar);
  }

  public void setLayout(final @LayoutRes int layoutId) {
    LayoutInflater.from(this).inflate(layoutId, findViewById(R.id.content_frame), true);
  }

  /**
   * Dispatch onResume() to fragments. Note that for better inter-operation with older versions of the platform, at the point of this call the fragments attached to the activity are not resumed.
   */
  @Override
  protected void onResume() {
    mApp.applyThemeFromSettings();
    super.onResume();
  }

  /**
   * Called by the system when the device configuration changes while your activity is running.
   *
   * @param newConfig The new device configuration. This value cannot be null.
   */
  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    mApp.setConfiguration(newConfig);
    int mask = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;
    if(mask == Configuration.UI_MODE_NIGHT_YES)
      mApp.setApplicationTheme(getString(R.string.default_theme_dark));
    else if(mask == Configuration.UI_MODE_NIGHT_NO)
      mApp.setApplicationTheme(getString(R.string.default_theme_light));
  }
}
