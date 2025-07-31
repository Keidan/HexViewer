package fr.ralala.hexviewer.ui.activities.settings;

import android.content.Context;
import android.content.Intent;

import fr.ralala.hexviewer.ui.fragments.AbstractSettingsFragment;
import fr.ralala.hexviewer.ui.fragments.SettingsFragmentListsLandscape;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Settings activity for lists in landscape mode.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
@SuppressWarnings("java:S110")
public class SettingsListsLandscapeActivity extends AbstractSettingsActivity {

  /**
   * Starts an activity.
   *
   * @param c Android context.
   */
  public static void startActivity(final Context c) {
    Intent intent = new Intent(c, SettingsListsLandscapeActivity.class);
    c.startActivity(intent);
  }

  /**
   * User implementation (called in onCreate).
   *
   * @return AbstractSettingsFragment
   */
  public AbstractSettingsFragment onUserCreate() {
    return new SettingsFragmentListsLandscape(this);
  }

}