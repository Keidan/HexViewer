package fr.ralala.hexviewer.ui.payload;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.Line;
import fr.ralala.hexviewer.models.LineData;
import fr.ralala.hexviewer.models.LineFilter;
import fr.ralala.hexviewer.ui.activities.MainActivity;
import fr.ralala.hexviewer.ui.adapters.HexTextArrayAdapter;
import fr.ralala.hexviewer.ui.adapters.config.UserConfigLandscape;
import fr.ralala.hexviewer.ui.adapters.config.UserConfigPortrait;
import fr.ralala.hexviewer.ui.utils.MultiChoiceCallback;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Helper for the hex list view.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class PayloadHexHelper {
  private MainActivity mActivity;
  private ListView mPayloadHex = null;
  private HexTextArrayAdapter mAdapterHex = null;
  private RelativeLayout mPayloadViewContainer = null;
  private LinearLayout mTitle = null;
  private TextView mTitleLineNumbers = null;
  private TextView mTitleContent = null;

  /**
   * Called when the activity is created.
   *
   * @param activity The owner activity
   */
  public void onCreate(final MainActivity activity) {
    mActivity = activity;
    mPayloadViewContainer = activity.findViewById(R.id.payloadViewContainer);
    mTitle = activity.findViewById(R.id.title);
    mTitleLineNumbers = activity.findViewById(R.id.titleLineNumbers);
    mTitleContent = activity.findViewById(R.id.titleContent);
    mPayloadHex = activity.findViewById(R.id.payloadView);

    mPayloadHex.setVisibility(View.GONE);
    mPayloadViewContainer.setVisibility(View.GONE);
    mTitleLineNumbers.setVisibility(View.GONE);
    mTitleContent.setVisibility(View.GONE);
    mTitle.setVisibility(View.GONE);

    HexTextArrayAdapter.LineNumbersTitle title = new HexTextArrayAdapter.LineNumbersTitle();
    title.titleContent = mTitleContent;
    title.titleLineNumbers = mTitleLineNumbers;

    mAdapterHex = new HexTextArrayAdapter(activity,
        new ArrayList<>(),
        title,
        new UserConfigPortrait(true),
        new UserConfigLandscape(true));
    mPayloadHex.setAdapter(mAdapterHex);
    mPayloadHex.setOnItemClickListener(activity);
    mPayloadHex.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
    MultiChoiceCallback multiChoiceCallback = new MultiChoiceCallback(activity, mPayloadHex, mAdapterHex);
    mPayloadHex.setMultiChoiceModeListener(multiChoiceCallback);
  }

  /**
   * Resets the update status.
   */
  public void resetUpdateStatus() {
    String query = mActivity.getSearchQuery();
    if (!query.isEmpty())
      mAdapterHex.manualFilterUpdate(""); /* reset filter */

    List<LineData<Line>> items = mAdapterHex.getItems();
    for (LineFilter<Line> line : mAdapterHex.getFilteredList()) {
      line.getData().setUpdated(false);
      items.get(line.getOrigin()).setUpdated(false);
    }
    if (!query.isEmpty())
      mAdapterHex.manualFilterUpdate(query);
    mAdapterHex.notifyDataSetChanged();
  }

  /**
   * Called to refresh the adapter.
   */
  public void refreshAdapter() {
    mAdapterHex.refresh();
  }

  /**
   * Called to refresh the line numbers.
   */
  public void refreshLineNumbers() {
    refreshLineNumbersVisibility();
    mAdapterHex.notifyDataSetChanged();
  }

  /**
   * Returns the hex adapter.
   *
   * @return HexTextArrayAdapter
   */
  public HexTextArrayAdapter getAdapter() {
    return mAdapterHex;
  }

  /**
   * Tests if the list view is visible.
   *
   * @return boolean
   */
  public boolean isVisible() {
    return mPayloadHex.getVisibility() == View.VISIBLE;
  }

  /**
   * Changes the list view visibility.
   *
   * @param b The new value
   */
  public void setVisible(boolean b) {
    mPayloadHex.setVisibility(b ? View.VISIBLE : View.GONE);
    mPayloadViewContainer.setVisibility(b ? View.VISIBLE : View.GONE);
    if (!b) {
      mTitleLineNumbers.setVisibility(View.GONE);
      mTitleContent.setVisibility(View.GONE);
      mTitle.setVisibility(View.GONE);
    } else
      refreshLineNumbersVisibility();
  }

  /**
   * Refreshes line numbers visibility.
   */
  private void refreshLineNumbersVisibility() {
    final boolean checked = ApplicationCtx.getInstance().isLineNumber();
    mTitleLineNumbers.setVisibility(checked ? View.VISIBLE : View.GONE);
    mTitleContent.setVisibility(checked ? View.VISIBLE : View.GONE);
    mTitle.setVisibility(checked ? View.VISIBLE : View.GONE);
  }

  /**
   * Returns the ListView
   *
   * @return ListView
   */
  public ListView getListView() {
    return mPayloadHex;
  }
}
