package fr.ralala.hexviewer.ui.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Vector;

import fr.ralala.hexviewer.application.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.utils.SystemBarUtils;

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
  private static final String TAG = "Base";
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
    SystemBarUtils.setNavAndStatusBarColor(this,
      mApp.getCurrentTheme().equals(getString(R.string.default_theme_light)));
  }

  public void setLayout(final @LayoutRes int layoutId) {
    LayoutInflater.from(this).inflate(layoutId, findViewById(R.id.content_frame), true);
  }

  /**
   * Dispatch onResume() to fragments. Note that for better inter-operation with older versions of the platform, at the point of this call the fragments attached to the activity are not resumed.
   */
  @Override
  protected void onResume() {
    if (mApp.applyThemeFromSettings()) {
      recreate();
    }
    super.onResume();
  }

  private String getConfigurationChangedTrigger(@NonNull Configuration newConfig)
  {
    Configuration oldConfig = getResources().getConfiguration();
    Vector<String> vec = new Vector<>();
    if (oldConfig.orientation != newConfig.orientation)
      vec.add("Orientation");
    if (oldConfig.screenLayout != newConfig.screenLayout)
      vec.add("ScreenLayout");
    if (oldConfig.smallestScreenWidthDp != newConfig.smallestScreenWidthDp)
      vec.add("SmallestScreenWidthDp");
    if (oldConfig.densityDpi != newConfig.densityDpi)
      vec.add("Density");
    if (oldConfig.fontScale != newConfig.fontScale)
      vec.add("FontScale");
    if (oldConfig.uiMode != newConfig.uiMode)
      vec.add("UiMode");
    return vec.isEmpty() ? "Unknown!!!" : String.join(", ", vec);
  }

  /**
   * Called by the system when the device configuration changes while your activity is running.
   *
   * @param newConfig The new device configuration. This value cannot be null.
   */
  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    ApplicationCtx.addLog(this, TAG, "Application configuration changed: " + getConfigurationChangedTrigger(newConfig));
    mApp.setConfiguration(newConfig);
    int mask = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;
    if (mask == Configuration.UI_MODE_NIGHT_YES)
      mApp.setApplicationTheme(getString(R.string.default_theme_dark), false);
    else if (mask == Configuration.UI_MODE_NIGHT_NO)
      mApp.setApplicationTheme(getString(R.string.default_theme_light), false);
  }
}
