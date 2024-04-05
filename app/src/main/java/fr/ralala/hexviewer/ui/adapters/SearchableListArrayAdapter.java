package fr.ralala.hexviewer.ui.adapters;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.models.LineEntries;
import fr.ralala.hexviewer.models.LineEntry;
import fr.ralala.hexviewer.ui.adapters.config.UserConfig;
import fr.ralala.hexviewer.ui.adapters.search.EntryFilter;
import fr.ralala.hexviewer.ui.adapters.search.ISearchFrom;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Adapter used by HexTextArrayAdapter and PlainTextArrayAdapter
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public abstract class SearchableListArrayAdapter extends ArrayAdapter<LineEntry> implements ISearchFrom {
  private final EntryFilter mEntryFilter;
  protected final UserConfig mUserConfigPortrait;
  protected final UserConfig mUserConfigLandscape;
  private final LineEntries mLineEntries;
  private Set<Integer> mSelectedItemsIds;

  protected SearchableListArrayAdapter(final Context context, final int layoutId, final List<LineEntry> objects, UserConfig userConfigPortrait, UserConfig userConfigLandscape) {
    super(context, layoutId, objects);
    mLineEntries = new LineEntries(objects);
    mUserConfigPortrait = userConfigPortrait;
    mUserConfigLandscape = userConfigLandscape;
    mEntryFilter = new EntryFilter(context, this, this, mLineEntries, userConfigPortrait, userConfigLandscape);
    mSelectedItemsIds = new HashSet<>();
  }

  /**
   * Returns true if the item is selected.
   *
   * @param position The position
   * @return boolean
   */
  protected boolean isSelected(int position) {
    return mSelectedItemsIds.contains(position);
  }

  /**
   * Toggles the item selection.
   *
   * @param position Item position.
   */
  public void toggleSelection(int position, boolean checked) {
    if (checked) {
      mSelectedItemsIds.add(position);
    } else {
      mSelectedItemsIds.remove(position);
    }
    notifyDataSetChanged();
  }

  /**
   * Removes the item selection.
   */
  public void removeSelection() {
    mSelectedItemsIds = new HashSet<>();
    notifyDataSetChanged();
  }

  /**
   * Returns the selected ids.
   *
   * @return SparseBooleanArray
   */
  public List<Integer> getSelectedIds() {
    List<Integer> li = new ArrayList<>(mSelectedItemsIds);
    Collections.sort(li);
    return li;
  }

  /**
   * Returns the selected count.
   *
   * @return count
   */
  public int getSelectedCount() {
    return mSelectedItemsIds.size();
  }

  /**
   * Returns the line entries.
   *
   * @return LineEntries
   */
  public LineEntries getEntries() {
    return mLineEntries;
  }

  /**
   * Get the data item associated with the specified position in the data set.
   *
   * @param position Position of the item whose data we want within the adapter's data set.
   * @return This value may be null.
   */
  @Override
  public LineEntry getItem(final int position) {
    return mLineEntries.getItem(position);
  }

  /**
   * How many items are in the data set represented by this Adapter.
   *
   * @return Count of items.
   */
  @Override
  public int getCount() {
    return mLineEntries.getCount();
  }

  /**
   * Get the row id associated with the specified position in the list.
   *
   * @param position The position of the item within the adapter's data set whose row id we want.
   * @return The id of the item at the specified position.
   */
  @Override
  public long getItemId(int position) {
    return mLineEntries.getItemId(position);
  }

  /**
   * Remove all elements from the list.
   */
  @Override
  public void clear() {
    mLineEntries.clear();
    notifyDataSetChanged();
  }

  /**
   * Refreshes this adapter.
   */
  public void refresh() {
    notifyDataSetChanged();
  }

  /**
   * Adds a list of new items to the list.
   *
   * @param collection The items to be added.
   */
  @Override
  public void addAll(@NonNull Collection<? extends LineEntry> collection) {
    mLineEntries.addAll(collection);
    notifyDataSetChanged();
  }

  /**
   * Inflate the view.
   *
   * @param convertView This value may be null.
   * @return The view.
   */
  protected abstract @NonNull View inflateView(final View convertView);

  /**
   * Fills the view.
   *
   * @param v        This can't be null.
   * @param position The position of the item within the adapter's data set of the item whose view we want.
   */
  protected abstract void fillView(final @NonNull View v, final int position);

  /**
   * Get a View that displays the data at the specified position in the data set.
   *
   * @param position    The position of the item within the adapter's data set of the item whose view we want.
   * @param convertView This value may be null.
   * @param parent      This value cannot be null.
   * @return This value cannot be null.
   */
  @Override
  public @NonNull View getView(final int position, final View convertView, @NonNull final ViewGroup parent) {
    View v = inflateView(convertView);
    fillView(v, position);
    return v;
  }

  /**
   * Applies the user config.
   *
   * @param tv TextView
   */
  protected void applyUserConfig(final TextView tv) {
    Configuration cfg = ((ApplicationCtx) getContext().getApplicationContext()).getConfiguration();
    if (mUserConfigLandscape != null && cfg.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      tv.setTextSize(mUserConfigLandscape.getFontSize());
      ViewGroup.LayoutParams lp = tv.getLayoutParams();
      lp.height = mUserConfigLandscape.isRowHeightAuto() ? ViewGroup.LayoutParams.WRAP_CONTENT : mUserConfigLandscape.getRowHeight();
      tv.setLayoutParams(lp);
    } else if (mUserConfigPortrait != null) {
      tv.setTextSize(mUserConfigPortrait.getFontSize());
      ViewGroup.LayoutParams lp = tv.getLayoutParams();
      lp.height = mUserConfigPortrait.isRowHeightAuto() ? ViewGroup.LayoutParams.WRAP_CONTENT : mUserConfigPortrait.getRowHeight();
      tv.setLayoutParams(lp);
    }
  }

  // Filter part

  /**
   * Get custom filter
   *
   * @return filter
   */
  @Override
  public Filter getFilter() {
    return mEntryFilter;
  }

  /**
   * Manual filter update.
   *
   * @param constraint The constraint.
   */
  public void manualFilterUpdate(CharSequence constraint) {
    final Set<Integer> tempList = new HashSet<>();
    mEntryFilter.apply(constraint, tempList);
    List<Integer> li = new ArrayList<>(tempList);
    Collections.sort(li);
    mLineEntries.setFilteredList(li);
  }
}

