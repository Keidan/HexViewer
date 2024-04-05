package fr.ralala.hexviewer.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.BuildConfig;
import fr.ralala.hexviewer.R;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Log activity
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class LogsActivity extends AppCompatActivity {
  private CircularFifoQueue<String> mCfq = null;
  private String mContent = null;
  private ListView mLogs = null;
  private ApplicationCtx mApp = null;

  /**
   * Starts an activity.
   *
   * @param c Android context.
   */
  public static void startActivity(final Context c) {
    Intent intent = new Intent(c, LogsActivity.class);
    c.startActivity(intent);
  }

  /**
   * User implementation (called in onCreate).
   */
  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_logs);
    mApp = (ApplicationCtx) getApplicationContext();

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    mLogs = findViewById(R.id.logs);

    mCfq = (CircularFifoQueue<String>) mApp.getLogBuffer();
    final String[] lines = mCfq.toArray(new String[]{});
    final StringBuilder sb = new StringBuilder();
    for (final String s : lines)
      sb.append(s).append("\n");
    mContent = sb.toString();
    mLogs.setAdapter(null);
    mLogs.setAdapter(new ArrayAdapter<>(this,
      R.layout.listview_simple_row, R.id.label1, lines));
  }

  /**
   * Detects the configuration changed.
   *
   * @param newConfig The new device configuration.
   */
  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    mApp.setConfiguration(newConfig);
  }

  /**
   * Called when the options menu is created.
   *
   * @param menu The selected menu.
   * @return boolean
   */
  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    getMenuInflater().inflate(R.menu.logs, menu);
    return super.onCreateOptionsMenu(menu);
  }

  /**
   * Returns a share intent
   */
  private Intent getDefaultShareIntent() {
    final String name = getResources().getString(R.string.app_name);
    final String date = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss a", Locale.US)
      .format(new Date());
    final Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("text/plain");
    intent.putExtra(Intent.EXTRA_SUBJECT, name + " v" + BuildConfig.VERSION_NAME + " logs "
      + date);
    intent.putExtra(Intent.EXTRA_TEXT, mContent);
    return intent;
  }

  /**
   * Called when the options item is clicked.
   *
   * @param item The selected menu.
   * @return boolean
   */
  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    } else if (item.getItemId() == R.id.action_clear) {
      mContent = "";
      mCfq.clear();
      mLogs.setAdapter(null);
      finish();
      return true;
    } else if (item.getItemId() == R.id.action_share) {
      /* reload the contents */
      startActivity(Intent.createChooser(getDefaultShareIntent(), null));
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
