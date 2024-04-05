package fr.ralala.hexviewer.ui.adapters;

import android.content.Context;
import android.content.res.Configuration;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.List;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.ListSettings;
import fr.ralala.hexviewer.ui.adapters.holders.HolderHex;
import fr.ralala.hexviewer.ui.adapters.holders.LineNumbersTitle;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.SysHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Adapter used with the hex text list view (line update activity).
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class LineUpdateHexArrayAdapter extends ArrayAdapter<String> {
  private static final int ID = R.layout.listview_hex_row;
  private final List<String> mEntryList;
  private final LineNumbersTitle mTitle;
  private final ListView mListView;
  private long mStartOffset;
  private int mMaxLength;
  private int mPreviousSize = -1;
  private final ApplicationCtx mApp;

  public LineUpdateHexArrayAdapter(final Context context,
                                   ListView listView,
                                   LineNumbersTitle title,
                                   final List<String> objects) {
    super(context, ID, objects);
    mStartOffset = 0;
    mListView = listView;
    mEntryList = objects;
    mTitle = title;
    mApp = (ApplicationCtx) context.getApplicationContext();
  }

  public void setStartOffset(final long startOffset) {
    mPreviousSize = -1;
    mStartOffset = startOffset;
  }

  /**
   * Returns the list view.
   *
   * @return ListView
   */
  public ListView getListView() {
    return mListView;
  }

  /**
   * Get the data item associated with the specified position in the data set.
   *
   * @param position Position of the item whose data we want within the adapter's data set.
   * @return This value may be null.
   */
  @Override
  public String getItem(final int position) {
    if (mEntryList != null)
      return mEntryList.get(position);
    return null;
  }

  /**
   * How many items are in the data set represented by this Adapter.
   *
   * @return Count of items.
   */
  @Override
  public int getCount() {
    if (mEntryList != null)
      return mEntryList.size();
    return 0;
  }

  /**
   * Get the row id associated with the specified position in the list.
   *
   * @param position The position of the item within the adapter's data set whose row id we want.
   * @return The id of the item at the specified position.
   */
  @Override
  public long getItemId(int position) {
    if (mEntryList != null)
      return mEntryList.get(position).hashCode();
    return 0;
  }

  /**
   * Remove all elements from the list.
   */
  @Override
  public void clear() {
    mEntryList.clear();
    notifyDataSetChanged();
  }

  /**
   * Get a View that displays the data at the specified position in the data set.
   *
   * @param position    The position of the item within the adapter's data set of the item whose view we want.
   * @param convertView This value may be null.
   * @param parent      This value cannot be null.
   * @return This value cannot be null.
   */
  @Override
  public @NonNull
  View getView(final int position, final View convertView,
               @NonNull final ViewGroup parent) {
    View v = inflateView(convertView);
    fillView(v, position);
    return v;
  }

  /**
   * Fills the view.
   *
   * @param v        This can't be null.
   * @param position The position of the item within the adapter's data set of the item whose view we want.
   */
  protected void fillView(final @NonNull View v, final int position) {
    if (v.getTag() != null) {
      final HolderHex holder = (HolderHex) v.getTag();
      String string = mEntryList.get(position);
      if (mPreviousSize != mEntryList.size()) {
        mPreviousSize = mEntryList.size();
        mMaxLength = Long.toHexString(UIHelper.getCurrentLine(mPreviousSize, mStartOffset, SysHelper.MAX_BY_ROW_8)).length();
        final String fmt = "%" + mMaxLength + "s";
        mTitle.getTitleLineNumbers().setText(String.format(fmt, " "));
      }
      final String fmt = "%0" + mMaxLength + "X";
      final String s = String.format(fmt, UIHelper.getCurrentLine(position, mStartOffset, SysHelper.MAX_BY_ROW_8));

      final @ColorInt int color = ContextCompat.getColor(getContext(),
        R.color.colorLineNumbers);
      holder.getLineNumbers().setText(s);
      holder.getLineNumbers().setTextColor(color);
      holder.getLineNumbers().setVisibility(View.VISIBLE);

      holder.getContent().setText(string);
      applyUserConfig(mTitle.getTitleContent());
      applyUserConfig(mTitle.getTitleLineNumbers());
      applyUserConfig(holder.getLineNumbers());
      applyUserConfig(holder.getContent());
    }
  }

  /**
   * Inflate the view.
   *
   * @param convertView This value may be null.
   * @return The view.
   */
  protected @NonNull
  View inflateView(final View convertView) {
    View v = convertView;
    if (v == null) {
      final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      if (inflater != null) {
        v = inflater.inflate(ID, null);
        HolderHex holder = new HolderHex();
        holder.setContent(v.findViewById(R.id.content));
        holder.setLineNumbers(v.findViewById(R.id.lineNumbers));
        holder.getContent().setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources().getDimension(R.dimen.activity_line_update_lv_textSize));
        holder.getLineNumbers().setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources().getDimension(R.dimen.activity_line_update_lv_textSize));
        v.setTag(holder);
      }
    }
    return v == null ? new View(getContext()) : v;
  }


  /**
   * Applies the user config.
   *
   * @param tv TextView
   */
  private void applyUserConfig(final TextView tv) {
    Configuration cfg = mApp.getConfiguration();
    ListSettings landscape = mApp.getListSettingsLineEditLandscape();
    ListSettings portrait = mApp.getListSettingsLineEditPortrait();
    if (landscape != null && cfg.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      tv.setTextSize(landscape.getFontSize());
      ViewGroup.LayoutParams lp = tv.getLayoutParams();
      lp.height = landscape.isRowHeightAuto() ? ViewGroup.LayoutParams.WRAP_CONTENT : landscape.getRowHeight();
      tv.setLayoutParams(lp);
    } else if (portrait != null) {
      tv.setTextSize(portrait.getFontSize());
      ViewGroup.LayoutParams lp = tv.getLayoutParams();
      lp.height = portrait.isRowHeightAuto() ? ViewGroup.LayoutParams.WRAP_CONTENT : portrait.getRowHeight();
      tv.setLayoutParams(lp);
    }
  }
}
