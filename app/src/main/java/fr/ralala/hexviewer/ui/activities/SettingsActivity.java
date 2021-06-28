package fr.ralala.hexviewer.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.fragments.SettingsFragment;

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
public class SettingsActivity extends AppCompatActivity {
  private static final String ACTIVITY_EXTRA_CHANGE = "ACTIVITY_EXTRA_CHANGE";
  private boolean mChange;

  /**
   * Starts an activity.
   *
   * @param c                      Android context.
   * @param change                 A change is detected?
   */
  public static void startActivity(final Context c, final boolean change) {
    Intent intent = new Intent(c, SettingsActivity.class);
    intent.putExtra(ACTIVITY_EXTRA_CHANGE, change);
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


    mChange = false;
    if (getIntent().getExtras() != null) {
      Bundle extras = getIntent().getExtras();
      mChange = extras.getBoolean(ACTIVITY_EXTRA_CHANGE);
    }

    //If you want to insert data in your settings
    SettingsFragment prefs = new SettingsFragment(this);

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

  /**
   * Tests if a change is detected.
   * @return boolean
   */
  public boolean isChanged() {
    return mChange;
  }
}