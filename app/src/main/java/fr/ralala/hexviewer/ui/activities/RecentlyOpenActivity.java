package fr.ralala.hexviewer.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.FileData;
import fr.ralala.hexviewer.ui.adapters.RecentlyOpenRecyclerAdapter;

import static fr.ralala.hexviewer.ui.adapters.RecentlyOpenRecyclerAdapter.UriData;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Recently open activity
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class RecentlyOpenActivity extends AppCompatActivity implements RecentlyOpenRecyclerAdapter.OnEventListener {
  private ApplicationCtx mApp = null;
  public static final String RESULT_START_OFFSET = "startOffset";
  public static final String RESULT_END_OFFSET = "endOffset";
  public static final String RESULT_OLD_TO_STRING = "oldToString";

  /**
   * Starts an activity.
   *
   * @param c                      Android context.
   * @param activityResultLauncher Activity Result Launcher.
   */
  public static void startActivity(final Context c, final ActivityResultLauncher<Intent> activityResultLauncher) {
    Intent intent = new Intent(c, RecentlyOpenActivity.class);
    activityResultLauncher.launch(intent);
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
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_recently_open);
    mApp = ApplicationCtx.getInstance();

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
    mApp.getRecentlyOpened().reload();
    // Lookup the recyclerview in activity layout
    RecyclerView recyclerView = findViewById(R.id.recyclerView);
    List<UriData> list = new ArrayList<>();
    final List<FileData> li = mApp.getRecentlyOpened().list();
    int index = 0;
    int max = li.size();
    int m = String.valueOf(max).length();
    for (int i = max - 1; i >= 0; i--) {
      list.add(new UriData(this, ++index, m, li.get(i)));
    }
    RecentlyOpenRecyclerAdapter adapter = new RecentlyOpenRecyclerAdapter(this, list, this);
    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(adapter.getSwipeToDeleteCallback());
    itemTouchHelper.attachToRecyclerView(recyclerView);

    setTitle(getString(R.string.action_recently_open_title));
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
      setResult(RESULT_CANCELED);
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Called when a click is captured.
   *
   * @param ud The associated item.
   */
  @Override
  public void onClick(@NonNull UriData ud) {
    Intent i = new Intent();
    i.setData(ud.fd.getUri());
    i.putExtra(RESULT_START_OFFSET, ud.fd.getStartOffset());
    i.putExtra(RESULT_END_OFFSET, ud.fd.getEndOffset());
    if(ud.sizeChanged)
      i.putExtra(RESULT_OLD_TO_STRING, ud.fd.toString());
    setResult(RESULT_OK, i);
    finish();
  }

  /**
   * Called when a click is captured.
   *
   * @param ud The associated item.
   */
  @Override
  public void onDelete(@NonNull UriData ud) {
    mApp.getRecentlyOpened().remove(ud.fd);
    if (mApp.getRecentlyOpened().list().isEmpty()) {
      Intent i = new Intent();
      i.setData(null);
      setResult(RESULT_OK, i);
      finish();
    }
  }

}
