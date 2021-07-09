package fr.ralala.hexviewer.ui.activities.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.fragments.SettingsFragmentListsLandscape;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Settings activity
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class SettingsListsLandscapeActivity extends AppCompatActivity {

  /**
   * Starts an activity.
   *
   * @param c      Android context.
   */
  public static void startActivity(final Context c) {
    Intent intent = new Intent(c, SettingsListsLandscapeActivity.class);
    c.startActivity(intent);
  }

  /**
   * Set the base context for this ContextWrapper.
   * All calls will then be delegated to the base context.
   * Throws IllegalStateException if a base context has already been set.
   *
   * @param base The new base context for this wrapper.
   */
  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(ApplicationCtx.getInstance().onAttach(base));
  }

  /**
   * Called when the activity is created.
   *
   * @param savedInstanceState Bundle
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_settings);

    //If you want to insert data in your settings
    SettingsFragmentListsLandscape prefs = new SettingsFragmentListsLandscape(this);

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