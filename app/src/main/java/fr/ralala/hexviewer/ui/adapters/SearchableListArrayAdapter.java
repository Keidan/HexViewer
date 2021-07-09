package fr.ralala.hexviewer.ui.adapters;

import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
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
import androidx.core.content.ContextCompat;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.LineData;
import fr.ralala.hexviewer.models.LineFilter;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Adapter used by HexTextArrayAdapter and PlainTextArrayAdapter
 * </p>
 *
 * @author Keidan
 *
 * License: GPLv3
 * <p>
 * ******************************************************************************
 */
public abstract class SearchableListArrayAdapter<T> extends ArrayAdapter<LineData<T>> {
  private static final int ID = R.layout.listview_simple_row;
  private final EntryFilter mEntryFilter;
  private final List<LineData<T>> mEntryList;
  private final UserConfig mUserConfigPortrait;
  private final UserConfig mUserConfigLandscape;
  private List<LineFilter<T>> mFilteredList;

  public interface UserConfig {
    float getFontSize();

    int getRowHeight();

    boolean isRowHeightAuto();
  }

  public SearchableListArrayAdapter(final Context context, final List<LineData<T>> objects, UserConfig userConfigPortrait, UserConfig userConfigLandscape) {
    super(context, ID, objects);
    mEntryFilter = new EntryFilter();
    mEntryList = objects;
    mFilteredList = new ArrayList<>();
    mUserConfigPortrait = userConfigPortrait;
    mUserConfigLandscape = userConfigLandscape;
  }

  /**
   * Returns the list of items.
   *
   * @return List<ListData < T>>
   */
  public List<LineData<T>> getItems() {
    return mEntryList;
  }

  /**
   * Returns the list of filtered items.
   *
   * @return List<FilterData < T>>
   */
  public List<LineFilter<T>> getFilteredList() {
    return mFilteredList;
  }

  /**
   * Get the data item associated with the specified position in the data set.
   *
   * @param position Position of the item whose data we want within the adapter's data set.
   * @return This value may be null.
   */
  @Override
  public LineData<T> getItem(final int position) {
    if (mFilteredList != null)
      return mFilteredList.get(position).getData();
    return null;
  }

  /**
   * Returns the position of the specified item in the array.
   *
   * @param item The item to retrieve the position of. This value may be null.
   * @return The position of the specified item.
   */
  @Override
  public int getPosition(LineData<T> item) {
    return super.getPosition(item);
  }

  /**
   * How many items are in the data set represented by this Adapter.
   *
   * @return Count of items.
   */
  @Override
  public int getCount() {
    if (mFilteredList != null)
      return mFilteredList.size();
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
    if (mFilteredList != null)
      return mFilteredList.get(position).getData().getValue().hashCode();
    return 0;
  }

  /**
   * Remove all elements from the list.
   */
  @Override
  public void clear() {
    mFilteredList.clear();
    mEntryList.clear();
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
  public void addAll(@NonNull Collection<? extends LineData<T>> collection) {
    /* Here the list is already empty */
    int i = 0;
    for (LineData<T> t : collection) {
      mEntryList.add(t);
      mFilteredList.add(new LineFilter<>(t, i++));
    }
    notifyDataSetChanged();
  }

  /**
   * Inflate the view.
   *
   * @param convertView This value may be null.
   * @return The view.
   */
  private @NonNull
  View inflateView(final View convertView) {
    View v = convertView;
    if (v == null) {
      final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      if (inflater != null) {
        v = inflater.inflate(ID, null);
        final TextView label1 = v.findViewById(R.id.label1);
        v.setTag(label1);
      }
    }
    return v == null ? new View(getContext()) : v;
  }

  /**
   * Fills the view.
   *
   * @param v        This can't be null.
   * @param position The position of the item within the adapter's data set of the item whose view we want.
   */
  private void fillView(final @NonNull View v, final int position) {
    if (v.getTag() != null) {
      final TextView holder = (TextView) v.getTag();
      LineFilter<T> fd = mFilteredList.get(position);

      if(fd.getData().isFalselyDeleted())
        return;

      applyUpdated(holder, fd);

      holder.setTextColor(ContextCompat.getColor(getContext(),
          fd.getData().isUpdated() ? R.color.colorTextUpdated : R.color.textColor));

      applyUserConfig(holder);
      v.setBackgroundColor(ContextCompat.getColor(getContext(), isSelected(position) ? R.color.colorAccent : R.color.windowBackground));
    }
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
   * Applies the necessary changes if the "updated" field is true.
   *
   * @param tv TextView
   * @param fd FilterData
   */
  private void applyUpdated(final TextView tv, final LineFilter<T> fd) {
    setEntryText(tv, fd.getData().getValue(), fd.getData().isUpdated());
  }

  /**
   * Applies the user config.
   *
   * @param tv TextView
   */
  private void applyUserConfig(final TextView tv) {
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
   * Sets the entry text (if updated = false)
   *
   * @param view    The text view.
   * @param text    The text.
   * @param updated The updated flag.
   */
  protected abstract void setEntryText(final TextView view, final T text, final boolean updated);

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
  protected abstract void extraFilter(final LineData<T> line, int index, String query, final ArrayList<LineFilter<T>> tempList, Locale loc);

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
    final ArrayList<LineFilter<T>> tempList = new ArrayList<>();
    mEntryFilter.apply(constraint, tempList);
    mFilteredList = tempList;
  }

  /**
   * Custom filter
   */
  private class EntryFilter extends Filter {

    protected void apply(CharSequence constraint, final ArrayList<LineFilter<T>> tempList) {
      boolean clear = (constraint == null || constraint.length() == 0);
      String query = "";
      final Locale loc = Locale.getDefault();
      if (!clear)
        query = constraint.toString().toLowerCase(loc);
      for (int i = 0; i < mEntryList.size(); i++) {
        LineData<T> s = mEntryList.get(i);
        if(s.isFalselyDeleted())
          continue;
        if (clear)
          tempList.add(new LineFilter<>(s, i));
        else if (s.toString().toLowerCase(loc).contains(query))
          tempList.add(new LineFilter<>(s, i));
        else
          extraFilter(s, i, query, tempList, loc);
      }
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
      final FilterResults filterResults = new FilterResults();
      final ArrayList<LineFilter<T>> tempList = new ArrayList<>();
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
      mFilteredList = (ArrayList<LineFilter<T>>) results.values;
      notifyDataSetChanged();
    }
  }
}

