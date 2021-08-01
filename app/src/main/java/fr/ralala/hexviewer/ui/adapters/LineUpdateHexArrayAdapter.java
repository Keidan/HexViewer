package fr.ralala.hexviewer.ui.adapters;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import fr.ralala.hexviewer.R;
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
  private final HexTextArrayAdapter.LineNumbersTitle mTitle;
  private final ListView mListView;

  public LineUpdateHexArrayAdapter(final Context context,
                                   ListView listView,
                                   HexTextArrayAdapter.LineNumbersTitle title,
                                   final List<String> objects) {
    super(context, ID, objects);
    mListView = listView;
    mEntryList = objects;
    mTitle = title;
  }

  /**
   * Returns the list view.
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
      final HexTextArrayAdapter.Holder holder = (HexTextArrayAdapter.Holder) v.getTag();
      String string = mEntryList.get(position);

      final int maxLength = String.format("%X", mEntryList.size() * SysHelper.MAX_BY_ROW_8).length();
      final String s = String.format("%0" + maxLength + "X", position * SysHelper.MAX_BY_ROW_8);
      final @ColorInt int color = ContextCompat.getColor(getContext(),
          R.color.colorLineNumbers);
      holder.lineNumbers.setText(s);
      holder.lineNumbers.setTextColor(color);
      holder.lineNumbers.setVisibility(View.VISIBLE);

      if (position == 0) {
        mTitle.titleLineNumbers.setText(String.format("%" + maxLength + "s", " "));
      }
      holder.content.setText(string);
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
        HexTextArrayAdapter.Holder holder = new HexTextArrayAdapter.Holder();
        holder.content = v.findViewById(R.id.content);
        holder.lineNumbers = v.findViewById(R.id.lineNumbers);
        holder.content.setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources().getDimension(R.dimen.activity_line_update_lv_font_size));
        holder.lineNumbers.setTextSize(TypedValue.COMPLEX_UNIT_PX, getContext().getResources().getDimension(R.dimen.activity_line_update_lv_font_size));
        v.setTag(holder);
      }
    }
    return v == null ? new View(getContext()) : v;
  }
}
