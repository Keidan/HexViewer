package fr.ralala.hexviewer.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Used with the adapters.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class LineEntries {
  private List<Integer> mFilteredList;
  private final List<LineEntry> mEntryList;

  public LineEntries(final List<LineEntry> objects) {
    mEntryList = objects;
    mFilteredList = new ArrayList<>();
  }

  public void setFilteredList(List<Integer> filteredList) {
    mFilteredList = filteredList;
  }

  /**
   * Returns the list of items.
   *
   * @return List<ListData < T>>
   */
  public List<LineEntry> getItems() {
    return mEntryList;
  }

  /**
   * Returns the number of items.
   *
   * @return int
   */
  public int getItemsCount() {
    return mEntryList.size();
  }

  /**
   * Reloads all indexes located after the start inde
   *
   * @param start Start index.
   */
  public void reloadAllIndexes(int start) {
    Collections.sort(mFilteredList);
    for (int i = start; i < mEntryList.size(); i++) {
      mEntryList.get(i).setIndex(i);
      if (i < mFilteredList.size())
        mFilteredList.set(i, i);
      else
        mFilteredList.add(i);
    }
  }

  /**
   * Moves indexes located after the start index
   *
   * @param start  Start index.
   * @param offset Offset.
   * @param plus   True=plus, False=minus
   */
  public void moveIndexes(int start, int offset, boolean plus) {
    Collections.sort(mFilteredList);
    for (int i = start; i < mEntryList.size(); i++) {
      LineEntry le = mEntryList.get(i);
      int idx = plus ? (le.getIndex() + offset) : (le.getIndex() - offset);
      le.setIndex(idx);
      mFilteredList.set(i, idx);
    }
  }

  /**
   * Removes an item.
   *
   * @param position The item position.
   */
  public void removeItem(final int position) {
    mEntryList.remove((int) mFilteredList.get(position));
    mFilteredList.remove((Integer) position);
  }

  /**
   * Adds an item.
   *
   * @param position The item position.
   * @param le       The item.
   */
  public void addItem(final int position, LineEntry le) {
    mFilteredList.add(position, le.getIndex());
    mEntryList.add(le.getIndex(), le);
  }

  /**
   * Adds an item.
   *
   * @param le The item.
   */
  public void addItem(LineEntry le) {
    mFilteredList.add(le.getIndex());
    mEntryList.add(le);
  }

  /**
   * Gets the actual index of an item.
   *
   * @param position The item.
   */
  public int getItemIndex(final int position) {
    return mFilteredList.get(position);
  }

  /**
   * Clears the update flag.
   */
  public void clearFilteredUpdated() {
    for (Integer index : mFilteredList) {
      mEntryList.get(index).setUpdated(false);
    }
  }

  /**
   * Adds a list of new items to the list.
   *
   * @param collection The items to be added.
   */
  public void addAll(@NonNull Collection<? extends LineEntry> collection) {
    /* Here the list is already empty */
    int i = 0;
    for (LineEntry t : collection) {
      t.setIndex(i);
      mEntryList.add(t);
      mFilteredList.add(i++);
    }
  }

  /**
   * Get the data item associated with the specified position in the data set.
   *
   * @param position Position of the item whose data we want within the adapter's data set.
   * @return This value may be null.
   */
  public LineEntry getItem(final int position) {
    if (mFilteredList != null && mFilteredList.size() > position) {
      final int pos = mFilteredList.get(position);
      if(mEntryList.size() > pos)
        return mEntryList.get(pos);
    }
    return null;
  }

  /**
   * How many items are in the data set represented by this Adapter.
   *
   * @return Count of items.
   */
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
  public long getItemId(int position) {
    if (mFilteredList != null)
      return mFilteredList.get(position).hashCode();
    return 0;
  }

  /**
   * Remove all elements from the list.
   */
  public void clear() {
    mFilteredList.clear();
    mEntryList.clear();
  }
}
