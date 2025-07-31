package fr.ralala.hexviewer.ui.activities.settings;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.fragments.AbstractSettingsFragment;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Abstract settings activity
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public abstract class AbstractSettingsActivity extends AppCompatActivity {

  private ApplicationCtx mApp;

  /**
   * Set the base context for this ContextWrapper.
   * All calls will then be delegated to the base context.
   * Throws IllegalStateException if a base context has already been set.
   *
   * @param base The new base context for this wrapper.
   */
  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(((ApplicationCtx) base.getApplicationContext()).onAttach(base));
  }

  /**
   * User implementation (called in onCreate).
   *
   * @return AbstractSettingsFragment
   */
  public abstract AbstractSettingsFragment onUserCreate();

  /**
   * Called when the activity is created.
   *
   * @param savedInstanceState Bundle
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    mApp = ((ApplicationCtx) getApplication());
    mApp.applyThemeFromSettings();
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_base);
    Toolbar toolbar = findViewById(R.id.base_toolbar);
    setSupportActionBar(toolbar);

    setLayout(R.layout.activity_settings);

    //If you want to insert data in your settings
    AbstractSettingsFragment prefs = onUserCreate();

    getSupportFragmentManager()
      .beginTransaction()
      .replace(R.id.settings_container, prefs)
      .commit();

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
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
  }

  /**
   * Called when the options item is clicked (home).
   *
   * @param item The selected menu.
   * @return boolean
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

}