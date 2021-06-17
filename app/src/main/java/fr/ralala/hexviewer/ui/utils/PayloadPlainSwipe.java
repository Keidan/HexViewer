package fr.ralala.hexviewer.ui.utils;

import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.adapters.SearchableListArrayAdapter;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Swipe management for the plain text display listview
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class PayloadPlainSwipe {
  private AppCompatActivity mActivity;
  private ApplicationCtx mApp;
  private ListView mPayloadPlain = null;
  private SearchableListArrayAdapter mAdapterPlain = null;
  private SwipeRefreshLayout mPayloadPlainSwipeRefreshLayout;
  private final AtomicBoolean mCancelPayloadPlainSwipeRefresh = new AtomicBoolean(false);

  /**
   * Called when the activity is created.
   *
   * @param activity The owner activity
   */
  public void onCreate(final AppCompatActivity activity) {
    mActivity = activity;
    mApp = (ApplicationCtx) activity.getApplication();
    mPayloadPlain = activity.findViewById(R.id.payloadPlain);
    mPayloadPlainSwipeRefreshLayout = activity.findViewById(R.id.payloadPlainSwipeRefreshLayout);

    // Configure SwipeRefreshLayout
    mPayloadPlainSwipeRefreshLayout.setOnRefreshListener(this::refresh);
    mPayloadPlainSwipeRefreshLayout.setColorSchemeResources(
        android.R.color.holo_blue_light,
        android.R.color.holo_orange_light,
        android.R.color.holo_green_light,
        android.R.color.holo_red_light);
    mPayloadPlain.setVisibility(View.GONE);

    mAdapterPlain = new SearchableListArrayAdapter(activity,
        SearchableListArrayAdapter.DisplayCharPolicy.IGNORE_NON_DISPLAYED_CHAR,
        new ArrayList<>(), new SearchableListArrayAdapter.UserConfig() {
      @Override
      public float getFontSize() {
        return mApp.getPlainFontSize();
      }

      @Override
      public int getRowHeight() {
        return mApp.getPlainRowHeight();
      }

      @Override
      public boolean isRowHeightAuto() {
        return mApp.isPlainRowHeightAuto();
      }
    });
    mPayloadPlain.setAdapter(mAdapterPlain);
  }

  /**
   * Called when the activity is resumed.
   */
  public void refreshAdapter() {
    mAdapterPlain.refresh();
  }

  /**
   * Gets the list view adapter.
   *
   * @return SearchableListArrayAdapter
   */
  public SearchableListArrayAdapter getAdapterPlain() {
    return mAdapterPlain;
  }

  /**
   * Tests if the list view is visible.
   *
   * @return boolean
   */
  public boolean isVisible() {
    return mPayloadPlain.getVisibility() == View.VISIBLE;
  }

  /**
   * Changes the list view visibility.
   *
   * @param b The new value
   */
  public void setVisible(boolean b) {
    mPayloadPlain.setVisibility(b ? View.VISIBLE : View.GONE);
  }


  /**
   * Functions called to refresh the list.
   */
  private void refresh() {
    mCancelPayloadPlainSwipeRefresh.set(false);
    new Thread(() -> {
      mApp.getPayload().refreshPlain(mCancelPayloadPlainSwipeRefresh);
      if (!mCancelPayloadPlainSwipeRefresh.get()) {
        mActivity.runOnUiThread(() -> {
          mAdapterPlain.clear();
          mAdapterPlain.addAll(mApp.getPayload().getPlain());
        });
      }
      mPayloadPlainSwipeRefreshLayout.setRefreshing(false);
      mCancelPayloadPlainSwipeRefresh.set(false);
    }).start();
  }
}
