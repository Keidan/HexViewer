package fr.ralala.hexviewer.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
  private final List<String> mItemList;
  private final List<String> mArraylist;
  private final Context mContext;

  public SearchableListArrayAdapter(final Context context, final List<String> objects) {
    super(context, ID, objects);
    mContext = context;
    mItemList = objects;
    mArraylist = new ArrayList<>();
    mArraylist.addAll(mItemList);
  }

  /**
   * Get the data item associated with the specified position in the data set.
   *
   * @param position Position of the item whose data we want within the adapter's data set.
   * @return This value may be null.
   */
  @Override
  public String getItem(final int position) {
    if (mItemList != null)
      return mItemList.get(position);
    return null;
  }

  /**
   * Set the data item with the specified position in the data set.
   *
   * @param position Position of the item.
   * @param t The data.
   */
  public void setItem(final int position, final String t) {
    mItemList.set(position, t);
    mArraylist.set(position, t);
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
    if (mItemList != null)
      return mItemList.size();
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
    if (mItemList != null)
      return mItemList.get(position).hashCode();
    return 0;
  }

  /**
   * Remove all elements from the list.
   */
  @Override
  public void clear() {
    mItemList.clear();
    mArraylist.clear();
    notifyDataSetChanged();
  }


  /**
   * Adds a list of new items to the list.
   * @param collection The items to be added.
   */
  public void addAll(@NonNull Collection<? extends String> collection) {
    mItemList.addAll(collection);
    mArraylist.addAll(collection);
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
      final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      if (inflater != null) {
        v = inflater.inflate(ID, null);
        final TextView label1 = v.findViewById(R.id.label1);
        v.setTag(label1);
      }
    }
    if (v != null && v.getTag() != null) {
      final TextView holder = (TextView) v.getTag();
      holder.setText(mItemList.get(position));
    }
    return v == null ? new View(mContext) : v;
  }


  // Filter part

  /**
   * Filters the list of applications to display.
   * @param charText Search word
   */
  public void filter(String charText) {
    charText = charText.toLowerCase(Locale.getDefault());
    mItemList.clear();
    if (charText.length() == 0) {
      mItemList.addAll(mArraylist);
    } else {
      for (final String s : mArraylist) {
        if (s.toLowerCase(Locale.getDefault()).contains(charText)) {
          mItemList.add(s);
        }
      }
    }
    notifyDataSetChanged();
  }

  /**
   * Clears the filter.
   */
  public void clearFilter() {
    mItemList.clear();
    mItemList.addAll(mArraylist);
    notifyDataSetChanged();
  }
}

