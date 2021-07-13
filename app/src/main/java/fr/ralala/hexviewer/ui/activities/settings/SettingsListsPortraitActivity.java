package fr.ralala.hexviewer.ui.activities.settings;

import android.content.Context;
import android.content.Intent;

import fr.ralala.hexviewer.ui.fragments.AbstractSettingsFragment;
import fr.ralala.hexviewer.ui.fragments.SettingsFragmentListsPortrait;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Settings activity for lists in portrait mode.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class SettingsListsPortraitActivity extends AbstractSettingsActivity {

  /**
   * Starts an activity.
   *
   * @param c Android context.
   */
  public static void startActivity(final Context c) {
    Intent intent = new Intent(c, SettingsListsPortraitActivity.class);
    c.startActivity(intent);
  }

  /**
   * User implementation (called in onCreate).
   *
   * @return AbstractSettingsFragment
   */
  public AbstractSettingsFragment onUserCreate() {
    return new SettingsFragmentListsPortrait(this);
  }

}