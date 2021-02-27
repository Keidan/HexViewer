package fr.ralala.hexviewer.ui.adapters;

import android.content.Context;
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
  private List<FilterData> mFilteredList;

  public enum DisplayCharPolicy {
    DISPLAY_ALL,
    IGNORE_NON_DISPLAYED_CHAR, /* except space and NL */
  }

  public SearchableListArrayAdapter(final Context context, DisplayCharPolicy policy, final List<String> objects) {
    super(context, ID, objects);
    mEntryFilter = new EntryFilter();
    mEntryList = objects;
    mFilteredList = new ArrayList<>();
    mPolicy = policy;
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
    super.notifyDataSetChanged();
  }

  /**
   * Set the data item with the specified position in the data set.
   *
   * @param position Position of the item.
   * @param t        The data.
   */
  public void setItem(final int position, final String t) {
    FilterData fd = mFilteredList.get(position);
    fd.value = t;
    mEntryList.set(fd.origin, t);
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
      String text = getItem(position);
      if (mPolicy == DisplayCharPolicy.IGNORE_NON_DISPLAYED_CHAR) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray())
          sb.append((c == 0x09 || c == 0x0A || (c >= 0x20 && c < 0x7F)) ? c : '.');
        text = sb.toString();
      }
      holder.setText(text);
    }
    return v == null ? new View(getContext()) : v;
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
    private FilterData(String value, int origin) {
      this.value = value;
      this.origin = origin;
    }
    private String value;
    private final int origin;
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

