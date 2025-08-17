package fr.ralala.hexviewer.ui.payload;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ListView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.LineEntry;
import fr.ralala.hexviewer.models.RawBuffer;
import fr.ralala.hexviewer.ui.activities.MainActivity;
import fr.ralala.hexviewer.ui.adapters.PlainTextListArrayAdapter;
import fr.ralala.hexviewer.ui.adapters.config.UserConfigLandscape;
import fr.ralala.hexviewer.ui.adapters.config.UserConfigPortrait;
import fr.ralala.hexviewer.ui.multichoice.PlainMultiChoiceCallback;
import fr.ralala.hexviewer.ui.utils.UIHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Swipe management for the plain text display list view
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class PayloadPlainSwipe {
  private MainActivity mActivity;
  private ListView mPayloadPlain = null;
  private PlainTextListArrayAdapter mAdapterPlain = null;
  private SwipeRefreshLayout mPayloadPlainSwipeRefreshLayout;
  private final AtomicBoolean mCancelPayloadPlainSwipeRefresh = new AtomicBoolean(false);
  private UserConfigPortrait mUserConfigPortrait;
  private UserConfigLandscape mUserConfigLandscape;
  private PlainMultiChoiceCallback mPlainMultiChoiceCallback;

  /**
   * Called when the activity is created.
   *
   * @param activity The owner activity
   */
  public void onCreate(final MainActivity activity) {
    mActivity = activity;
    mPayloadPlain = activity.findViewById(R.id.payloadPlain);
    mPayloadPlainSwipeRefreshLayout = activity.findViewById(R.id.payloadPlainSwipeRefreshLayout);
    // Configure SwipeRefreshLayout
    mPayloadPlainSwipeRefreshLayout.setOnRefreshListener(() -> refresh(false));
    mPayloadPlainSwipeRefreshLayout.setColorSchemeResources(
      android.R.color.holo_blue_light,
      android.R.color.holo_orange_light,
      android.R.color.holo_green_light,
      android.R.color.holo_red_light);
    mPayloadPlain.setVisibility(View.GONE);

    mUserConfigPortrait = new UserConfigPortrait(activity, false);
    mUserConfigLandscape = new UserConfigLandscape(activity, false);
    mAdapterPlain = new PlainTextListArrayAdapter(activity,
      new ArrayList<>(),
      mUserConfigPortrait,
      mUserConfigLandscape);
    mPayloadPlain.setAdapter(mAdapterPlain);
    mPlainMultiChoiceCallback = new PlainMultiChoiceCallback(activity, mPayloadPlain, mAdapterPlain);
  }

  /**
   * Called to refresh the adapter.
   */
  public void refreshAdapter() {
    refresh(false);
    mAdapterPlain.refresh();
  }

  /**
   * Gets the list view adapter.
   *
   * @return SearchableListArrayAdapter
   */
  public PlainTextListArrayAdapter getAdapter() {
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
    if (b) {
      new Handler(Looper.getMainLooper()).postDelayed(() -> {
        mPayloadPlainSwipeRefreshLayout.setRefreshing(true);
        refresh(false);
      }, 100);
    }
  }


  /**
   * Functions called to refresh the list.
   */
  public void refresh(boolean fromOrientation) {
    mCancelPayloadPlainSwipeRefresh.set(true);
    new Handler(Looper.getMainLooper()).postDelayed(() -> {
      mCancelPayloadPlainSwipeRefresh.set(false);
      final List<LineEntry> list = refreshPlain(mCancelPayloadPlainSwipeRefresh);
      if (!mCancelPayloadPlainSwipeRefresh.get()) {
        mActivity.runOnUiThread(() -> {
          mAdapterPlain.setLockRefresh(fromOrientation && mPlainMultiChoiceCallback.isActionMode());
          mAdapterPlain.clear();
          mAdapterPlain.addAll(list);
          if (!mActivity.getSearchQuery().isEmpty())
            mAdapterPlain.manualFilterUpdate(mActivity.getSearchQuery());
        });
      }
      mPayloadPlainSwipeRefreshLayout.setRefreshing(false);
      mCancelPayloadPlainSwipeRefresh.set(false);
      mPlainMultiChoiceCallback.refresh();
    }, 100);
  }


  /**
   * Refreshes the plain text list according to the list of payload data.
   *
   * @param cancel Used to cancel this method.
   * @return List<ListData < String>>
   */
  private List<LineEntry> refreshPlain(final AtomicBoolean cancel) {
    int maxByLine = UIHelper.getMaxByLine(mActivity, mUserConfigLandscape, mUserConfigPortrait);

    RawBuffer payload = new RawBuffer(4096);
    for (LineEntry le : mActivity.getPayloadHex().getAdapter().getEntries().getItems()) {
      payload.addAll(le.getRaw());
    }
    final StringBuilder sb = new StringBuilder();
    int nbPerLine = 0;
    final List<LineEntry> list = new ArrayList<>();
    for (int i = 0; i < payload.size() && (cancel == null || !cancel.get()); i++) {
      sb.append((char) payload.get(i));
      if (nbPerLine != 0 && (nbPerLine % maxByLine) == 0) {
        list.add(new LineEntry(sb.toString(), null));
        nbPerLine = 0;
        sb.setLength(0);
      } else {
        nbPerLine++;
      }
    }
    if ((cancel == null || !cancel.get()) && nbPerLine != 0) {
      list.add(new LineEntry(sb.toString(), null));
    }
    return list;
  }

  /**
   * Returns the ListView
   *
   * @return ListView
   */
  public ListView getListView() {
    return mPayloadPlain;
  }

}
