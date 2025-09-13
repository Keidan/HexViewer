package fr.ralala.hexviewer.models.lines;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
    mFilteredList = filteredList == null ? new ArrayList<>() : filteredList;
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
   * Reloads all indexes located after the start index
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
   * Removes items.
   *
   * @param positions The items position.
   */
  public void removeItems(List<Integer> positions) {
    // 1. Mark the positions to be deleted
    boolean[] toRemove = new boolean[mFilteredList.size()];
    for (int pos : positions) {
      if (pos >= 0 && pos < toRemove.length) {
        toRemove[pos] = true;
      }
    }

    // 2. Rebuild mFilteredList and mEntryList in a single pass
    List<Integer> newFilteredList = new ArrayList<>(mFilteredList.size() - positions.size());
    List<LineEntry> newEntryList = new ArrayList<>(mEntryList.size());

    for (int i = 0; i < mFilteredList.size(); i++) {
      if (!toRemove[i]) {
        int entryIndex = mFilteredList.get(i);
        newFilteredList.add(entryIndex);
        newEntryList.add(mEntryList.get(entryIndex));
      }
    }

    // 3. Replace old lists
    mFilteredList.clear();
    mFilteredList.addAll(newFilteredList);

    mEntryList.clear();
    mEntryList.addAll(newEntryList);
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
    return mFilteredList.isEmpty() ? 0 : mFilteredList.get(position);
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
      if (mEntryList.size() > pos)
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
