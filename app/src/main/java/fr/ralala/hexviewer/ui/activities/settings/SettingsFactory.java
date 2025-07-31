package fr.ralala.hexviewer.ui.activities.settings;

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
 * Settings factory
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class SettingsFactory {
  protected interface Create {
    /**
     * User implementation (called in onCreate).
     *
     * @return AbstractSettingsFragment
     */
    AbstractSettingsFragment onUserCreate();

    /**
     * Called when the activity is created.
     *
     * @param savedInstanceState Bundle
     */
    void onInitCreate(Bundle savedInstanceState);

    /**
     * Called when the options item is clicked (home).
     *
     * @param item The selected menu.
     * @return boolean
     */
    boolean onOptsItemSelected(MenuItem item);
  }

  private ApplicationCtx mApp;
  private final AppCompatActivity mActivity;
  private final Create mCreate;

  protected SettingsFactory(AppCompatActivity activity, Create create) {
    mActivity = activity;
    mCreate = create;
  }

  /**
   * Called when the activity is created.
   *
   * @param savedInstanceState Bundle
   */
  protected void onCreate(Bundle savedInstanceState) {
    mApp = ((ApplicationCtx) mActivity.getApplication());
    mApp.applyThemeFromSettings();
    mCreate.onInitCreate(savedInstanceState);
    mActivity.setContentView(R.layout.activity_base);
    Toolbar toolbar = mActivity.findViewById(R.id.base_toolbar);
    mActivity.setSupportActionBar(toolbar);

    setLayout(R.layout.activity_settings);

    //If you want to insert data in your settings
    AbstractSettingsFragment prefs = mCreate.onUserCreate();

    mActivity.getSupportFragmentManager()
      .beginTransaction()
      .replace(R.id.settings_container, prefs)
      .commit();

    ActionBar actionBar = mActivity.getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  public void setLayout(final @LayoutRes int layoutId) {
    LayoutInflater.from(mActivity).inflate(layoutId, mActivity.findViewById(R.id.content_frame), true);
  }

  /**
   * Dispatch onResume() to fragments. Note that for better inter-operation with older versions of the platform, at the point of this call the fragments attached to the activity are not resumed.
   */
  protected void onResume() {
    mApp.applyThemeFromSettings();
  }

  /**
   * Called by the system when the device configuration changes while your activity is running.
   *
   * @param newConfig The new device configuration. This value cannot be null.
   */
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    mApp.setConfiguration(newConfig);
  }

  /**
   * Called when the options item is clicked (home).
   *
   * @param item The selected menu.
   * @return boolean
   */
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      mActivity.finish();
      return true;
    }
    return mCreate.onOptsItemSelected(item);
  }
}
