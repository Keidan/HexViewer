package fr.ralala.hexviewer.ui.adapters.search;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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

  public void apply(CharSequence constraint, final Set<Integer> tempList) {
    boolean clear = (constraint == null || constraint.length() == 0);
    String query = "";
    final Locale loc = Locale.getDefault();
    if (!clear)
      query = constraint.toString().toLowerCase(loc);
    List<LineEntry> items = mLineEntries.getItems();
    final int length = items.size();
    for (int i = 0; i < length; i++) {
      LineEntry lineEntry = items.get(i);
      if (clear)
        tempList.add(i);
      else {
        mFilterFactory.multilineSearch(lineEntry, items, i, query, loc, tempList);
      }
    }
  }

  @Override
  protected FilterResults performFiltering(CharSequence constraint) {
    final FilterResults filterResults = new FilterResults();
    final Set<Integer> tempList = new HashSet<>();
    apply(constraint, tempList);
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
    List<Integer> li = new ArrayList<>((Set<Integer>) results.values);
    Collections.sort(li);
    mLineEntries.setFilteredList(li);
    mAdapter.notifyDataSetChanged();
  }
}
