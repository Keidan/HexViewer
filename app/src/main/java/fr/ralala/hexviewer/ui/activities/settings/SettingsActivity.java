package fr.ralala.hexviewer.ui.activities.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

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
public class SettingsActivity extends AbstractSettingsActivity {
  private static final String ACTIVITY_EXTRA_CHANGE = "ACTIVITY_EXTRA_CHANGE";
  private boolean mChange;

  /**
   * Starts an activity.
   *
   * @param c      Android context.
   * @param change A change is detected?
   */
  public static void startActivity(final Context c, final boolean change) {
    Intent intent = new Intent(c, SettingsActivity.class);
    intent.putExtra(ACTIVITY_EXTRA_CHANGE, change);
    c.startActivity(intent);
  }

  /**
   * User implementation (called in onCreate).
   *
   * @return AbstractSettingsFragment
   */
  public AbstractSettingsFragment onUserCreate() {
    mChange = false;
    if (getIntent().getExtras() != null) {
      Bundle extras = getIntent().getExtras();
      mChange = extras.getBoolean(ACTIVITY_EXTRA_CHANGE);
    }

    //If you want to insert data in your settings
    return new SettingsFragment(this);
  }

  /**
   * Tests if a change is detected.
   *
   * @return boolean
   */
  public boolean isChanged() {
    return mChange;
  }
}