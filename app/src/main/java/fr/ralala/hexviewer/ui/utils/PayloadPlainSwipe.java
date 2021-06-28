package fr.ralala.hexviewer.ui.utils;

import android.os.Handler;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.activities.MainActivity;
import fr.ralala.hexviewer.ui.adapters.PlainTextListArrayAdapter;
import fr.ralala.hexviewer.ui.adapters.SearchableListArrayAdapter;
import fr.ralala.hexviewer.models.LineEntry;
import fr.ralala.hexviewer.utils.SysHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Swipe management for the plain text display list view
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class PayloadPlainSwipe {
  private MainActivity mActivity;
  private ApplicationCtx mApp;
  private ListView mPayloadPlain = null;
  private PlainTextListArrayAdapter mAdapterPlain = null;
  private SwipeRefreshLayout mPayloadPlainSwipeRefreshLayout;
  private final AtomicBoolean mCancelPayloadPlainSwipeRefresh = new AtomicBoolean(false);

  /**
   * Called when the activity is created.
   *
   * @param activity The owner activity
   */
  public void onCreate(final MainActivity activity) {
    mActivity = activity;
    mApp = ApplicationCtx.getInstance();
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

    mAdapterPlain = new PlainTextListArrayAdapter(activity,
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
  public PlainTextListArrayAdapter getAdapterPlain() {
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
      new Handler().postDelayed(() -> {
        mPayloadPlainSwipeRefreshLayout.setRefreshing(true);
        refresh();
      }, 100);
    }
  }


  /**
   * Functions called to refresh the list.
   */
  private void refresh() {
    mCancelPayloadPlainSwipeRefresh.set(true);
    new Handler().postDelayed(() -> {
      mCancelPayloadPlainSwipeRefresh.set(false);
      final List<String> list = refreshPlain(mCancelPayloadPlainSwipeRefresh);
      if (!mCancelPayloadPlainSwipeRefresh.get()) {
        mActivity.runOnUiThread(() -> {
          mAdapterPlain.clear();
          mAdapterPlain.addAll(list);
        });
      }
      mPayloadPlainSwipeRefreshLayout.setRefreshing(false);
      mCancelPayloadPlainSwipeRefresh.set(false);
    }, 100);
  }


  /**
   * Refreshes the plain text list according to the list of payload data.
   *
   * @param cancel Used to cancel this method.
   * @return List<String>
   */
  private List<String> refreshPlain(final AtomicBoolean cancel) {
    final List<Byte> payload = new ArrayList<>();
    for (LineEntry le : mActivity.getAdapterHex().getItems())
      payload.addAll(le.getRaw());
    final StringBuilder sb = new StringBuilder();
    int nbPerLine = 0;
    final List<String> list = new ArrayList<>();
    for (int i = 0; i < payload.size() && (cancel == null || !cancel.get()); i++) {
      if (nbPerLine != 0 && (nbPerLine % SysHelper.MAX_BY_LINE) == 0) {
        sb.append((char) payload.get(i).byteValue());
        list.add(sb.toString());
        nbPerLine = 0;
        sb.setLength(0);
      } else {
        sb.append((char) (char) payload.get(i).byteValue());
        nbPerLine++;
      }
    }
    if ((cancel == null || !cancel.get()) && nbPerLine != 0) {
      list.add(sb.toString());
    }
    return list;
  }
}
