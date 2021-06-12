package fr.ralala.hexviewer.ui.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import fr.ralala.hexviewer.R;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Adapter used with the listview.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class SearchableListArrayAdapter extends ArrayAdapter<String> {
  private static final int ID = R.layout.listview_simple_row;
  private final EntryFilter mEntryFilter;
  private final DisplayCharPolicy mPolicy;
  private final List<String> mEntryList;
  private final UserConfig mUserConfig;
  private List<FilterData> mFilteredList;
  private Map<Integer, FilterData> mRecentDeleteList;
  private SparseBooleanArray mSelectedItemsIds;

  public enum DisplayCharPolicy {
    DISPLAY_ALL,
    IGNORE_NON_DISPLAYED_CHAR, /* except space and NL */
  }

  public interface UserConfig {
    float getFontSize();
    int getRowHeight();
    boolean isRowHeightAuto();
  }

  public SearchableListArrayAdapter(final Context context, DisplayCharPolicy policy, final List<String> objects, UserConfig userConfig) {
    super(context, ID, objects);
    mEntryFilter = new EntryFilter();
    mEntryList = objects;
    mFilteredList = new ArrayList<>();
    mPolicy = policy;
    mUserConfig = userConfig;
    mSelectedItemsIds = new SparseBooleanArray();
    mRecentDeleteList = new HashMap<>();
  }

  /**
   * Toggles the item selection.
   * @param position Item position.
   */
  public void toggleSelection(int position) {
    selectView(position, !mSelectedItemsIds.get(position));
  }

  /**
   * Removes the item selection.
   */
  public void removeSelection() {
    mSelectedItemsIds = new SparseBooleanArray();
    notifyDataSetChanged();
  }

  /**
   * Select a view.
   * @param position Position.
   * @param value Selection value.
   */
  private void selectView(int position, boolean value) {
    if (value)
      mSelectedItemsIds.put(position, true);
    else
      mSelectedItemsIds.delete(position);
    notifyDataSetChanged();
  }

  /**
   * Returns the selection count.
   * @return int
   */
  public int getSelectedCount() {
    return mSelectedItemsIds.size();
  }

  /**
   * Returns the selected ids.
   * @return SparseBooleanArray
   */
  public SparseBooleanArray getSelectedIds() {
    return mSelectedItemsIds;
  }

  /**
   * Returns if the position is checked or not.
   * @param position The item position.
   * @return boolean
   */
  public boolean isPositionChecked(int position) {
    return mSelectedItemsIds.get(position);
  }

  /**
   * Get the data item associated with the specified position in the data set.
   *
   * @param position Position of the item whose data we want within the adapter's data set.
   * @return This value may be null.
   */
  @Override
  public String getItem(final int position) {
    if (mFilteredList != null)
      return mFilteredList.get(position).value;
    return null;
  }
  /**
   * Remove an item.
   *
   * @param position Position of the item.
   */
  public void removeItem(final int position) {
    FilterData fd = mFilteredList.get(position);
    mEntryList.remove(fd.origin);
    mFilteredList.remove(position);
    mRecentDeleteList.put(position, fd);
    super.notifyDataSetChanged();
  }

  /**
   * Undo the deleted items.
   */
  public void undoDelete() {
    for (Map.Entry<Integer, FilterData> entry : mRecentDeleteList.entrySet()) {
      FilterData fd = entry.getValue();
      mFilteredList.add(entry.getKey(), fd);
      mEntryList.add(fd.origin, fd.value);
    }
    clearRecentlyDeleted();
    super.notifyDataSetChanged();
  }

  /**
   * Clears the list of recently deleted items.
   */
  public void clearRecentlyDeleted() {
    mRecentDeleteList.clear();
  }

  /**
   * Set the data item with the specified position in the data set.
   *
   * @param position Position of the item.
   * @param t        The data.
   */
  public void setItem(final int position, final List<String> t) {
    int size = t.size();
    if(size == 1) {
      FilterData fd = mFilteredList.get(position);
      fd.value = t.get(0);
      fd.updated = true;
      mEntryList.set(fd.origin, t.get(0));
    } else {
      /* first we move the existing indexes - filtered */
      for(int i = position + 1; i < mFilteredList.size(); i++)
        mFilteredList.get(i).origin += size;

      /* Then we modify the existing element */
      int origin = mFilteredList.get(position).origin + 1;
      FilterData fd = mFilteredList.get(position);
      final String newVal = t.get(0);
      if(!fd.value.equals(newVal)) {
        fd.value = newVal;
        fd.updated = true;
        mEntryList.set(fd.origin, t.get(0));
      }

      /* finally we add the elements */
      for(int i = 1; i < size; i++) {
        String value = t.get(i);
        fd = new FilterData(value, origin + i);
        fd.updated = true;
        if(origin + i < mEntryList.size())
          mEntryList.add(origin + i, t.get(i));
        else
          mEntryList.add(t.get(i));
        if(position + i < mFilteredList.size())
          mFilteredList.add(position + i, fd);
        else
          mFilteredList.add(fd);
      }
    }
    super.notifyDataSetChanged();
  }

  /**
   * Returns the position of the specified item in the array.
   *
   * @param item The item to retrieve the position of. This value may be null.
   * @return The position of the specified item.
   */
  @Override
  public int getPosition(String item) {
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
      return mFilteredList.get(position).value.hashCode();
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
  public void addAll(@NonNull Collection<? extends String> collection) {
    /* Here the list is already empty */
    mEntryList.addAll(collection);
    for(int i = 0; i < mEntryList.size(); i++) {
      mFilteredList.add(new FilterData(mEntryList.get(i), i));
    }
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
    View v = convertView;
    if (v == null) {
      final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      if (inflater != null) {
        v = inflater.inflate(ID, null);
        final TextView label1 = v.findViewById(R.id.label1);
        v.setTag(label1);
      }
    }
    if (v != null && v.getTag() != null) {
      final TextView holder = (TextView) v.getTag();
      FilterData fd = mFilteredList.get(position);
      if (mPolicy == DisplayCharPolicy.IGNORE_NON_DISPLAYED_CHAR) {
        fd.value = ignoreNonDisplayedChar(fd.value);
      }

      applyUpdated(holder, fd);

      holder.setTextColor(ContextCompat.getColor(getContext(),
          fd.updated ? R.color.colorTextUpdated : R.color.textColor));

      applyUserConfig(holder);
      v.setBackgroundColor(ContextCompat.getColor(getContext(), mSelectedItemsIds.get(position) ? R.color.colorAccent : R.color.windowBackground));
    }
    return v == null ? new View(getContext()) : v;
  }

  /**
   * Ignore non displayed char
   * @param ref Ref string.
   * @return Patched string.
   */
  private String ignoreNonDisplayedChar(final String ref) {
    StringBuilder sb = new StringBuilder();
    for (char c : ref.toCharArray())
      sb.append((c == 0x09 || c == 0x0A || (c >= 0x20 && c < 0x7F)) ? c : '.');
    return sb.toString();
  }

  /**
   * Applies the necessary changes if the "updated" field is true.
   * @param tv TextView
   * @param fd FilterData
   */
  private void applyUpdated(final TextView tv, final FilterData fd) {
    if(fd.updated) {
      SpannableString spanString = new SpannableString(fd.value);
      spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
      tv.setText(spanString);
    } else
      tv.setText(fd.value);
  }

  /**
   * Applies the user config.
   * @param tv TextView
   */
  private void applyUserConfig(final TextView tv) {
    if(mUserConfig != null) {
      tv.setTextSize(mUserConfig.getFontSize());
      ViewGroup.LayoutParams lp = tv.getLayoutParams();
      lp.height = mUserConfig.isRowHeightAuto() ? ViewGroup.LayoutParams.WRAP_CONTENT : mUserConfig.getRowHeight();
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

  private static class FilterData {
    private String value;
    private int origin;
    private boolean updated = false;

    private FilterData(String value, int origin) {
      this.value = value;
      this.origin = origin;
    }
  }

  /**
   * Custom filter
   */
  private class EntryFilter extends Filter {

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
      final FilterResults filterResults = new FilterResults();
      final ArrayList<FilterData> tempList = new ArrayList<>();
      boolean clear = (constraint == null || constraint.length() == 0);
      for(int i = 0; i < mEntryList.size(); i++) {
        String s = mEntryList.get(i);
        if(clear)
          tempList.add(new FilterData(s, i));
        else if (s.toLowerCase(Locale.getDefault()).contains(constraint))
          tempList.add(new FilterData(s, i));
      }
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
      if (results.count == 0) {
        notifyDataSetInvalidated();
      } else {
        mFilteredList = (ArrayList<FilterData>) results.values;
        notifyDataSetChanged();
      }
    }
  }
}

