package fr.ralala.hexviewer.ui.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.utils.FileHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Adapter used with the listview (recently open).
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class RecentlyOpenListArrayAdapter extends ArrayAdapter<RecentlyOpenListArrayAdapter.UriData> {
  private static final int ID = R.layout.listview_recently_open;
  private final List<UriData> mEntryList;


  public RecentlyOpenListArrayAdapter(final Context context, final List<UriData> objects) {
    super(context, ID, objects);
    mEntryList = objects;
  }

  /**
   * Get the data item associated with the specified position in the data set.
   *
   * @param position Position of the item whose data we want within the adapter's data set.
   * @return This value may be null.
   */
  @Override
  public UriData getItem(final int position) {
    if (mEntryList != null)
      return mEntryList.get(position);
    return null;
  }

  /**
   * Returns the position of the specified item in the array.
   *
   * @param item The item to retrieve the position of. This value may be null.
   * @return The position of the specified item.
   */
  @Override
  public int getPosition(UriData item) {
    return super.getPosition(item);
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
   * Adds a list of new items to the list.
   *
   * @param set The items to be added.
   */
  public void addAll(@NonNull List<String> set) {
    List<String> list = new ArrayList<>(set);
    int index = 1;
    for(int i = list.size() - 1; i >= 0; i--) {
      mEntryList.add(new UriData(index++, list.get(i)));
    }
    notifyDataSetChanged();
  }

  /**
   * Remove an item.
   *
   * @param position Position of the item.
   */
  public void removeItem(final int position) {
    mEntryList.remove(position);
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
      UriData ud = mEntryList.get(position);
      holder.setText((ud.index + " - " + ud.value));
    }
    return v == null ? new View(getContext()) : v;
  }


  public static class UriData {
    public final String value;
    public final Uri uri;
    public final int index;

    private UriData(int index, String uri) {
      this.index = index;
      this.uri = Uri.parse(uri);
      this.value = FileHelper.getFileName(this.uri);
    }
  }
}

