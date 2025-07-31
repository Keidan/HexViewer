package fr.ralala.hexviewer.ui.activities.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.ui.fragments.AbstractSettingsFragment;
import fr.ralala.hexviewer.ui.fragments.SettingsFragment;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Settings activity
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class SettingsActivity extends AppCompatActivity implements SettingsFactory.Create {
  private static final String ACTIVITY_EXTRA_CHANGE = "ACTIVITY_EXTRA_CHANGE";
  private static final String ACTIVITY_EXTRA_OPEN = "ACTIVITY_EXTRA_OPEN";
  private final SettingsFactory mFactory = new SettingsFactory(this, this);
  private boolean mChange;
  private boolean mOpen;

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
   * Starts an activity.
   *
   * @param c      Android context.
   * @param open   A file is open?
   * @param change A change is detected?
   */
  public static void startActivity(final Context c, final boolean open, final boolean change) {
    Intent intent = new Intent(c, SettingsActivity.class);
    intent.putExtra(ACTIVITY_EXTRA_CHANGE, change);
    intent.putExtra(ACTIVITY_EXTRA_OPEN, open);
    c.startActivity(intent);
  }

  /**
   * Called when the activity is created.
   *
   * @param savedInstanceState Bundle
   */
  @SuppressLint("MissingSuperCall")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    mFactory.onCreate(savedInstanceState);
  }

  /**
   * Called when the activity is created.
   *
   * @param savedInstanceState Bundle
   */
  @Override
  public void onInitCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  /**
   * Dispatch onResume() to fragments. Note that for better inter-operation with older versions of the platform, at the point of this call the fragments attached to the activity are not resumed.
   */
  @Override
  protected void onResume() {
    mFactory.onResume();
    super.onResume();
  }

  /**
   * Called when the options item is clicked (home).
   *
   * @param item The selected menu.
   * @return boolean
   */
  @Override
  public boolean onOptsItemSelected(MenuItem item) {
    return super.onOptionsItemSelected(item);
  }

  /**
   * Called when the options item is clicked (home).
   *
   * @param item The selected menu.
   * @return boolean
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    return mFactory.onOptionsItemSelected(item);
  }


  /**
   * User implementation (called in onCreate).
   *
   * @return AbstractSettingsFragment
   */
  public AbstractSettingsFragment onUserCreate() {
    mChange = false;
    mOpen = false;
    if (getIntent().getExtras() != null) {
      Bundle extras = getIntent().getExtras();
      mChange = extras.getBoolean(ACTIVITY_EXTRA_CHANGE);
      mOpen = extras.getBoolean(ACTIVITY_EXTRA_OPEN);
    }

    //If you want to insert data in your settings
    return new SettingsFragment(this);
  }

  /**
   * Tests if a change is detected.
   *
   * @return boolean
   */
  public boolean isNotChanged() {
    return !mChange;
  }

  /**
   * Tests if a file is open.
   *
   * @return boolean
   */
  public boolean isOpen() {
    return mOpen;
  }
}