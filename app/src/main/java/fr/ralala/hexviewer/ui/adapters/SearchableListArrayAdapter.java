package fr.ralala.hexviewer.ui.adapters;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import fr.ralala.hexviewer.models.LineEntries;
import fr.ralala.hexviewer.models.LineEntry;

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
public abstract class SearchableListArrayAdapter extends ArrayAdapter<LineEntry> {
  private final EntryFilter mEntryFilter;
  protected final UserConfig mUserConfigPortrait;
  protected final UserConfig mUserConfigLandscape;
  private LineEntries mLineEntries;

  public interface UserConfig {
    float getFontSize();

    int getRowHeight();

    boolean isRowHeightAuto();

    boolean isDataColumnNotDisplayed();
  }

  public SearchableListArrayAdapter(final Context context,
                                    final int layoutId,
                                    final List<LineEntry> objects,
                                    UserConfig userConfigPortrait,
                                    UserConfig userConfigLandscape) {
    super(context, layoutId, objects);
    mEntryFilter = new EntryFilter();
    mLineEntries = new LineEntries(objects);
    mUserConfigPortrait = userConfigPortrait;
    mUserConfigLandscape = userConfigLandscape;
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
   * Returns the position of the specified item in the array.
   *
   * @param item The item to retrieve the position of. This value may be null.
   * @return The position of the specified item.
   */
  @Override
  public int getPosition(LineEntry item) {
    return super.getPosition(item);
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
  protected abstract @NonNull
  View inflateView(final View convertView);

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
  public @NonNull
  View getView(final int position, final View convertView,
               @NonNull final ViewGroup parent) {
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
    Configuration cfg = getContext().getResources().getConfiguration();
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

  /**
   * Returns true if the item is selected.
   *
   * @param position The position
   * @return boolean
   */
  protected abstract boolean isSelected(int position);

  /**
   * Performs a hexadecimal search in a plain text string.
   *
   * @param line     The current line.
   * @param index    The line index.
   * @param query    The query.
   * @param tempList The output list.
   * @param loc      The locale.
   */
  protected abstract void extraFilter(final LineEntry line, int index, String query, final List<Integer> tempList, Locale loc);

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
    final List<Integer> tempList = new ArrayList<>();
    mEntryFilter.apply(constraint, tempList);
    mLineEntries.setFilteredList(tempList);
  }

  /**
   * Custom filter
   */
  private class EntryFilter extends Filter {

    protected void apply(CharSequence constraint, final List<Integer> tempList) {
      boolean clear = (constraint == null || constraint.length() == 0);
      String query = "";
      final Locale loc = Locale.getDefault();
      if (!clear)
        query = constraint.toString().toLowerCase(loc);
      List<LineEntry> items = mLineEntries.getItems();
      for (int i = 0; i < items.size(); i++) {
        LineEntry s = items.get(i);
        if (clear)
          tempList.add(i);
        else if (s.toString().toLowerCase(loc).contains(query))
          tempList.add(i);
        else
          extraFilter(s, i, query, tempList, loc);
      }
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
      final FilterResults filterResults = new FilterResults();
      final List<Integer> tempList = new ArrayList<>();
      apply(constraint, tempList);
      filterResults.count = tempList.size();
      filterResults.values = tempList;
      return filterResults;
    }

    /**
     * Notify about filtered list to ui
     *
     * @param constraint text
     * @param results    filtered result
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
      mLineEntries.setFilteredList((List<Integer>) results.values);
      notifyDataSetChanged();
    }
  }
}

