package fr.ralala.hexviewer.ui.adapters;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Adapter used with the hex text list view.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class HexTextArrayAdapter extends SearchableListArrayAdapter {
  private final Map<Integer, FilterData> mRecentDeleteList;
  private SparseBooleanArray mSelectedItemsIds;


  public HexTextArrayAdapter(final Context context, final List<String> objects, UserConfig userConfig) {
    super(context, objects, userConfig);
    mSelectedItemsIds = new SparseBooleanArray();
    mRecentDeleteList = new HashMap<>();
  }


  /**
   * Sets the entry text (if updated = false)
   *
   * @param view The text view.
   * @param text The text.
   */
  @Override
  protected void setEntryText(final TextView view, final String text) {
    view.setText(text);
  }

  /**
   * Returns true if the item is selected.
   *
   * @param position The position
   * @return boolean
   */
  @Override
  protected boolean isSelected(int position) {
    return mSelectedItemsIds.get(position);
  }

  /**
   * Performs a hexadecimal search in a plain text string.
   *
   * @param line     The current line.
   * @param index    The line index.
   * @param query    The query.
   * @param tempList The output list.
   * @param loc      The locale.
   */
  @Override
  protected void extraFilter(final String line, int index, String query, final ArrayList<SearchableListArrayAdapter.FilterData> tempList, Locale loc) {
    /* nothing */
  }

  /**
   * Toggles the item selection.
   *
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
   *
   * @param position Position.
   * @param value    Selection value.
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
   *
   * @return int
   */
  public int getSelectedCount() {
    return mSelectedItemsIds.size();
  }

  /**
   * Returns the selected ids.
   *
   * @return SparseBooleanArray
   */
  public SparseBooleanArray getSelectedIds() {
    return mSelectedItemsIds;
  }

  /**
   * Returns if the position is checked or not.
   *
   * @param position The item position.
   * @return boolean
   */
  public boolean isPositionChecked(int position) {
    return mSelectedItemsIds.get(position);
  }

  /**
   * Remove an item.
   *
   * @param position Position of the item.
   */
  public void removeItem(final int position) {
    FilterData fd = mFilteredList.get(position);
    mFilteredList.remove(position);
    mRecentDeleteList.put(position, fd);
    super.removeItem(position);
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
   * Remove all elements from the list.
   */
  @Override
  public void clear() {
    mRecentDeleteList.clear();
    super.clear();
  }

}

