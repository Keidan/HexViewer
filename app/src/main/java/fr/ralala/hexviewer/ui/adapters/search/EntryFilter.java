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

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Filter implementation that performs searches on a list of {@link LineEntry} items.
 * <p>
 * Supports searching in both hex view and plain text view, using a {@link SearchableFilterFactory}
 * to handle multi-line and multi-format queries efficiently. Results are tracked in a {@link BitSet}
 * and mapped to line indices for the adapter.
 * </p>
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

  /**
   * Constructs an EntryFilter.
   *
   * @param context     the context
   * @param adapter     the adapter to notify when the filtered results change
   * @param searchFrom  object indicating whether the search is from hex view or plain text
   * @param lineEntries the source of LineEntry items to search
   */
  public EntryFilter(final Context context,
                     ArrayAdapter<LineEntry> adapter,
                     ISearchFrom searchFrom,
                     LineEntries lineEntries) {
    mAdapter = adapter;
    mLineEntries = lineEntries;
    mFilterFactory = new SearchableFilterFactory(context, searchFrom);
  }

  /**
   * Reloads the search context (hex/plain) and line width for the underlying filter factory.
   */
  public void reloadContext() {
    mFilterFactory.reloadContext();
  }

  /**
   * Applies a search query to all lines and marks matching lines in the provided BitSet.
   * <p>
   * Supports multi-line matches. If the query is empty, all lines are marked as matching.
   * </p>
   *
   * @param constraint the search query
   * @param tempList   BitSet to mark the indices of matching lines
   */
  public void apply(CharSequence constraint, final BitSet tempList) {
    List<LineEntry> items = mLineEntries.getItems();
    final int length = items.size();

    // If query is empty, mark all lines as matching
    if (TextUtils.isEmpty(constraint)) {
      tempList.set(0, length);
      return;
    }
    // Reset abort flag at the start of the search
    abortFlag.set(false);

    // Prepare query as lowercase char array for comparison
    final Locale loc = Locale.getDefault();
    char[] cQuery = constraint.toString().toLowerCase(loc).toCharArray();
    int lineIndex = 0;
    while (lineIndex < length) {
      // Check for external abort request
      if (abortFlag.get()) return;

      // Perform a search starting from the current line, potentially covering multiple lines
      mFilterFactory.multiLinesSearchBlock(items, lineIndex, cQuery, tempList);
      // Move to the next unmarked line, to handle queries starting at any position
      lineIndex = tempList.nextClearBit(lineIndex + 1);
    }
  }

  /**
   * Performs the filtering operation for the given query.
   * <p>
   * This method is called by the framework when a filter request is submitted.
   * It computes the set of matching line indices.
   * </p>
   *
   * @param constraint the search query
   * @return a {@link FilterResults} object containing the count and the list of matching indices
   */
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

  /**
   * Converts a {@link BitSet} to a sorted {@link List} of integers.
   *
   * @param bs the BitSet to convert
   * @return a sorted List of indices corresponding to set bits in the BitSet
   */
  public List<Integer> toSet(BitSet bs) {
    List<Integer> li = new ArrayList<>();
    for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) li.add(i);
    Collections.sort(li);
    return li;
  }

  /**
   * Publishes the filtered results to the adapter and updates the UI.
   *
   * @param constraint the query that produced these results
   * @param results    the filtered results, typically a List<Integer> of matching line indices
   */
  @SuppressWarnings("unchecked")
  @Override
  protected void publishResults(CharSequence constraint, FilterResults results) {
    mLineEntries.setFilteredList((List<Integer>) results.values);
    if (mAdapter != null)
      mAdapter.notifyDataSetChanged();
  }
}
