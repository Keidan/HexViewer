package fr.ralala.hexviewer.ui.payload;

import android.content.res.Configuration;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.LineEntry;
import fr.ralala.hexviewer.ui.activities.MainActivity;
import fr.ralala.hexviewer.ui.adapters.PlainTextListArrayAdapter;
import fr.ralala.hexviewer.ui.adapters.config.UserConfigLandscape;
import fr.ralala.hexviewer.ui.adapters.config.UserConfigPortrait;

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
    mPayloadPlainSwipeRefreshLayout.setOnRefreshListener(this::refresh);
    mPayloadPlainSwipeRefreshLayout.setColorSchemeResources(
        android.R.color.holo_blue_light,
        android.R.color.holo_orange_light,
        android.R.color.holo_green_light,
        android.R.color.holo_red_light);
    mPayloadPlain.setVisibility(View.GONE);

    mAdapterPlain = new PlainTextListArrayAdapter(activity,
        new ArrayList<>(),
        mUserConfigPortrait = new UserConfigPortrait(false),
        mUserConfigLandscape = new UserConfigLandscape(false));
    mPayloadPlain.setAdapter(mAdapterPlain);
  }

  /**
   * Called to refresh the adapter.
   */
  public void refreshAdapter() {
    refresh();
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
      new Handler().postDelayed(() -> {
        mPayloadPlainSwipeRefreshLayout.setRefreshing(true);
        refresh();
      }, 100);
    }
  }


  /**
   * Functions called to refresh the list.
   */
  public void refresh() {
    mCancelPayloadPlainSwipeRefresh.set(true);
    new Handler().postDelayed(() -> {
      mCancelPayloadPlainSwipeRefresh.set(false);
      final List<LineEntry> list = refreshPlain(mCancelPayloadPlainSwipeRefresh);
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
   * @return List<ListData < String>>
   */
  private List<LineEntry> refreshPlain(final AtomicBoolean cancel) {
    int width = getTextWidth();
    int maxByLine = width == 0 ? 70 : (getScreenWidth() / width) - 2;
    final List<Byte> payload = new ArrayList<>();
    for (LineEntry le : mActivity.getPayloadHex().getAdapter().getEntries().getItems())
      payload.addAll(le.getRaw());
    final StringBuilder sb = new StringBuilder();
    int nbPerLine = 0;
    final List<LineEntry> list = new ArrayList<>();
    for (int i = 0; i < payload.size() && (cancel == null || !cancel.get()); i++) {
      sb.append((char) payload.get(i).byteValue());
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


  /**
   * Gets the width of the text according to the font size and family (monospace).
   *
   * @return The width.
   */
  private int getTextWidth() {
    final Typeface monospace = Typeface.MONOSPACE;
    final String text = "a";
    float fontSize = 12.0f;
    /* Solution 1: We get the width of the text. */
    TextView tv = new TextView(mActivity);
    tv.setText(text);
    tv.setTypeface(monospace);
    Configuration cfg = mActivity.getResources().getConfiguration();
    if (mUserConfigLandscape != null && cfg.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      fontSize = mUserConfigLandscape.getFontSize();
    } else if (mUserConfigPortrait != null) {
      fontSize = mUserConfigPortrait.getFontSize();
    }
    tv.setTextSize(fontSize);
    tv.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
    int width = tv.getMeasuredWidth();
    /* Solution 2: If we can't get the width, then we try another method (obviously less accurate) */
    if (width < 1) {
      Paint paint = new Paint();
      paint.setTypeface(monospace);
      float scaledSizeInPixels = getSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
      paint.setTextSize(scaledSizeInPixels);
      Rect bounds = new Rect();
      paint.getTextBounds(text, 0, text.length(), bounds);
      width = bounds.width();
    }
    return width;
  }

  /**
   * Gets the size according to the display metrics.
   *
   * @param unit  The unit to convert from.
   * @param value The value to apply the unit to.
   * @return The new value.
   */
  private float getSize(int unit, float value) {
    return TypedValue.applyDimension(unit, value, mActivity.getResources().getDisplayMetrics());
  }

  /**
   * Gets the width of the screen.
   *
   * @return The width.
   */
  private int getScreenWidth() {
    DisplayMetrics displayMetrics = mActivity.getResources().getDisplayMetrics();
    /* The current view has a padding of 1dp */
    return (int) ((float) displayMetrics.widthPixels - getSize(TypedValue.COMPLEX_UNIT_DIP, 1.0f));
  }
}
