package fr.ralala.hexviewer.ui.adapters.search;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.ralala.hexviewer.models.LineEntries;
import fr.ralala.hexviewer.models.LineEntry;
import fr.ralala.hexviewer.ui.adapters.config.UserConfig;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Custom filter
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class EntryFilter extends Filter {
  private final SearchableFilterFactory mFilterFactory;
  private final ArrayAdapter<LineEntry> mAdapter;
  private final LineEntries mLineEntries;
  private final AtomicBoolean abortFlag = new AtomicBoolean(false);

  public EntryFilter(final Context context,
                     ArrayAdapter<LineEntry> adapter,
                     ISearchFrom searchFrom,
                     LineEntries lineEntries,
                     UserConfig userConfigPortrait,
                     UserConfig userConfigLandscape) {
    mAdapter = adapter;
    mLineEntries = lineEntries;
    mFilterFactory = new SearchableFilterFactory(context, searchFrom, userConfigPortrait, userConfigLandscape);
  }

  public void reloadLineWidth() {
    mFilterFactory.reloadLineWidth();
  }

  public void apply(CharSequence constraint, final BitSet tempList) {
    List<LineEntry> items = mLineEntries.getItems();
    final int length = items.size();

    if (TextUtils.isEmpty(constraint)) {
      tempList.set(0, length);
      return;
    }
    // reset abort flag at the start
    abortFlag.set(false);

    final Locale loc = Locale.getDefault();
    char[] cQuery = constraint.toString().toLowerCase(loc).toCharArray();
    int lineIndex = 0;
    while (lineIndex < length) {
      // check abort
      if (abortFlag.get()) return;

      // Determine max block size for current position
      int blockSize = Math.min(mFilterFactory.estimateBlockSize(cQuery.length), length - lineIndex);

      // Search in the block: current line + following lines as needed
      mFilterFactory.multilineSearchBlock(items, lineIndex, lineIndex + blockSize, cQuery, tempList);

      // Move to next line not yet marked, to handle queries starting at any offset
      lineIndex = tempList.nextClearBit(lineIndex + 1);
    }
  }

  @Override
  protected FilterResults performFiltering(CharSequence constraint) {
    // abort previous search if any
    abortFlag.set(true);

    final FilterResults filterResults = new FilterResults();
    final BitSet tempList = new BitSet();
    apply(constraint, tempList);

    List<Integer> resultSet = toSet(tempList);
    filterResults.count = resultSet.size();
    filterResults.values = resultSet;
    return filterResults;
  }

  public List<Integer> toSet(BitSet bs) {
    List<Integer> li = new ArrayList<>();
    for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) li.add(i);
    Collections.sort(li);
    return li;
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
    if (mAdapter != null)
      mAdapter.notifyDataSetChanged();
  }
}
