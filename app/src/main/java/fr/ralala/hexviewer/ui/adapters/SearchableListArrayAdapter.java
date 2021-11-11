package fr.ralala.hexviewer.ui.adapters;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import androidx.annotation.NonNull;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.models.LineEntries;
import fr.ralala.hexviewer.models.LineEntry;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.SysHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Adapter used by HexTextArrayAdapter and PlainTextArrayAdapter
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public abstract class SearchableListArrayAdapter extends ArrayAdapter<LineEntry> {
  private final EntryFilter mEntryFilter;
  protected final UserConfig mUserConfigPortrait;
  protected final UserConfig mUserConfigLandscape;
  private final LineEntries mLineEntries;

  protected static class SearchResult {
    protected final int length;
    protected final int index;
    protected final boolean hexPart;
    protected final boolean withSpaces;
    protected final boolean fromHexView;

    protected SearchResult(int length, int index, boolean hexPart, boolean withSpaces, boolean fromHexView) {
      this.length = length;
      this.index = index;
      this.hexPart = hexPart;
      this.withSpaces = withSpaces;
      this.fromHexView = fromHexView;
    }
  }

  public interface UserConfig {
    float getFontSize();

    int getRowHeight();

    boolean isRowHeightAuto();

    boolean isDataColumnNotDisplayed();
  }

  public SearchableListArrayAdapter(final Context context,
                                    final int layoutId,
                                    final List<LineEntry> objects,
                                    UserConfig userConfigPortrait,
                                    UserConfig userConfigLandscape) {
    super(context, layoutId, objects);
    mEntryFilter = new EntryFilter();
    mLineEntries = new LineEntries(objects);
    mUserConfigPortrait = userConfigPortrait;
    mUserConfigLandscape = userConfigLandscape;
  }

  /**
   * Returns the line entries.
   *
   * @return LineEntries
   */
  public LineEntries getEntries() {
    return mLineEntries;
  }

  /**
   * Get the data item associated with the specified position in the data set.
   *
   * @param position Position of the item whose data we want within the adapter's data set.
   * @return This value may be null.
   */
  @Override
  public LineEntry getItem(final int position) {
    return mLineEntries.getItem(position);
  }

  /**
   * Returns the position of the specified item in the array.
   *
   * @param item The item to retrieve the position of. This value may be null.
   * @return The position of the specified item.
   */
  @Override
  public int getPosition(LineEntry item) {
    return super.getPosition(item);
  }

  /**
   * How many items are in the data set represented by this Adapter.
   *
   * @return Count of items.
   */
  @Override
  public int getCount() {
    return mLineEntries.getCount();
  }

  /**
   * Get the row id associated with the specified position in the list.
   *
   * @param position The position of the item within the adapter's data set whose row id we want.
   * @return The id of the item at the specified position.
   */
  @Override
  public long getItemId(int position) {
    return mLineEntries.getItemId(position);
  }

  /**
   * Remove all elements from the list.
   */
  @Override
  public void clear() {
    mLineEntries.clear();
    notifyDataSetChanged();
  }

  /**
   * Refreshes this adapter.
   */
  public void refresh() {
    notifyDataSetChanged();
  }

  /**
   * Adds a list of new items to the list.
   *
   * @param collection The items to be added.
   */
  public void addAll(@NonNull Collection<? extends LineEntry> collection) {
    mLineEntries.addAll(collection);
    notifyDataSetChanged();
  }

  /**
   * Inflate the view.
   *
   * @param convertView This value may be null.
   * @return The view.
   */
  protected abstract @NonNull
  View inflateView(final View convertView);

  /**
   * Fills the view.
   *
   * @param v        This can't be null.
   * @param position The position of the item within the adapter's data set of the item whose view we want.
   */
  protected abstract void fillView(final @NonNull View v, final int position);

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
   * Applies the user config.
   *
   * @param tv TextView
   */
  protected void applyUserConfig(final TextView tv) {
    Configuration cfg = getContext().getResources().getConfiguration();
    if (mUserConfigLandscape != null && cfg.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      tv.setTextSize(mUserConfigLandscape.getFontSize());
      ViewGroup.LayoutParams lp = tv.getLayoutParams();
      lp.height = mUserConfigLandscape.isRowHeightAuto() ? ViewGroup.LayoutParams.WRAP_CONTENT : mUserConfigLandscape.getRowHeight();
      tv.setLayoutParams(lp);
    } else if (mUserConfigPortrait != null) {
      tv.setTextSize(mUserConfigPortrait.getFontSize());
      ViewGroup.LayoutParams lp = tv.getLayoutParams();
      lp.height = mUserConfigPortrait.isRowHeightAuto() ? ViewGroup.LayoutParams.WRAP_CONTENT : mUserConfigPortrait.getRowHeight();
      tv.setLayoutParams(lp);
    }
  }

  /**
   * Returns true if the item is selected.
   *
   * @param position The position
   * @return boolean
   */
  protected abstract boolean isSelected(int position);

  /**
   * Test if we are from the hex view or the plain view.
   *
   * @return boolean
   */
  protected abstract boolean isSearchFromHewView();

  /**
   * Performs a search.
   *
   * @param line      The current line.
   * @param query     The query.
   * @param loc       The locale.
   * @param hexLength The length of the hex part.
   * @return The index of the query
   */
  protected SearchResult performsSearch(final LineEntry line, String query, Locale loc, int hexLength) {
    String plain = line.toString();
    /* hex part */
    final String hex = plain.substring(0, hexLength);
    int length = hex.length();
    int index = hex.toLowerCase(loc).lastIndexOf(query);
    boolean hexPart = true;
    boolean withSpaces = true;
    if (index == -1) {
      /* hex no space */
      final String hexNoSpaces = plain.substring(0, hexLength).replaceAll(" ", "");
      index = hexNoSpaces.toLowerCase(loc).lastIndexOf(query);
      if (index == -1) {
        /* plain text */
        hexPart = false;
        plain = plain.substring(hexLength + 2);
        index = plain.toLowerCase(loc).lastIndexOf(query);
        length = plain.length();
      } else {
        length = hexNoSpaces.length();
        withSpaces = false;
      }
    }
    return new SearchResult(length, index, hexPart, withSpaces, isSearchFromHewView());
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

  /**
   * Manual filter update.
   *
   * @param constraint The constraint.
   */
  public void manualFilterUpdate(CharSequence constraint) {
    final Set<Integer> tempList = new HashSet<>();
    mEntryFilter.apply(constraint, tempList);
    List<Integer> li = new ArrayList<>(tempList);
    Collections.sort(li);
    mLineEntries.setFilteredList(li);
  }

  /**
   * Adds the current line to a byte stream.
   *
   * @param byteArrayStream ByteArrayOutputStream
   * @param s               LineEntry
   */
  private void insertByteList(ByteArrayOutputStream byteArrayStream, LineEntry s) {
    if (s.getRaw() != null) {
      final List<Byte> bytes = s.getRaw();
      for (Byte b : bytes)
        byteArrayStream.write(b);
    } else {
      final char[] plain = s.getPlain().toCharArray();
      for (char c : plain)
        byteArrayStream.write((byte) c);
    }
  }

  private SearchResult findInByteArrayOutputStream(ByteArrayOutputStream byteArrayStream,
                                                   final String query,
                                                   final Locale loc) {
    byte[] bytes = byteArrayStream.toByteArray();
    List<LineEntry> lle = SysHelper.formatBuffer(bytes, null, bytes.length);
    if (!lle.isEmpty()) {
      LineEntry le = lle.get(0);
      int hexLength = bytes.length * 3 - 1;
      return performsSearch(new LineEntry(le.toString(), le.getRaw()), query, loc, hexLength);
    }
    return new SearchResult(0, -1, false, false, false);
  }

  /**
   * Search on several lines
   *
   * @param lineEntry LineEntry
   * @param items     List<LineEntry>
   * @param length    Items length.
   * @param i         Current index.
   * @param query     The query.
   * @param loc       Locale
   * @param tempList  The output list.
   */
  private void multilineSearch(final LineEntry lineEntry,
                               final List<LineEntry> items,
                               final int length,
                               final int i,
                               final String query,
                               final Locale loc,
                               final Set<Integer> tempList) {
    /* The word fits on 2 or more lines */
    final long count = query.length() / lineEntry.getPlain().length();
    final long remain = query.length() - (count * lineEntry.getPlain().length());
    ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
    insertByteList(byteArrayStream, lineEntry);
    if (findInByteArrayOutputStream(byteArrayStream, query, loc).index != -1) {
      tempList.add(i);
      /* If query contains only 1 character there is no need to go further. */
      if(query.length() == 1)
        return;
    }
    int k = i;
    /* The lines are complete? */
    if (count != 0) {
      for (int j = i; j < count && k < (i + count) && j < items.size(); j++, k++) {
        LineEntry lineEntry2 = items.get(j);
        insertByteList(byteArrayStream, lineEntry2);
      }
    } else k++;
    /* We overflow on another line not complete. */
    if (remain != 0 && k < length) {
      LineEntry lineEntry2 = items.get(k++);
      insertByteList(byteArrayStream, lineEntry2);
    }

    /* Preparation of an entry with all the lines concerned. */
    SearchResult sr = findInByteArrayOutputStream(byteArrayStream, query, loc);
    evaluateResult(lineEntry, i, k, sr, query, tempList);
    try {
      byteArrayStream.close();
    } catch (IOException e) {
      Log.e(getClass().getSimpleName(), "Close exception: " + e.getMessage(), e);
    }
  }

  private void evaluateResult(final LineEntry lineEntry, int i, int k, SearchResult sr,
                               final String query, final Set<Integer> tempList) {
    int index = sr.index;
    if (index != -1) {
      int start = 0;
      int end = k;
      int nbPerLines;
      if (sr.hexPart) {
        final int cfgNbPerLine = ApplicationCtx.getInstance().getNbBytesPerLine();
        if (sr.withSpaces) {
          nbPerLines = cfgNbPerLine == SysHelper.MAX_BY_ROW_16 ? SysHelper.MAX_BYTES_ROW_16 : SysHelper.MAX_BYTES_ROW_8;
          index += lineEntry.getShiftOffset() * 3;
        } else {
          nbPerLines = cfgNbPerLine == SysHelper.MAX_BY_ROW_16 ? SysHelper.MAX_BY_ROW_16 * 2 : SysHelper.MAX_BY_ROW_8 * 2;
          index += lineEntry.getShiftOffset() * 2;
        }
      } else {
        if (!sr.fromHexView)
          nbPerLines = UIHelper.getMaxByLine(getContext(), mUserConfigLandscape, mUserConfigPortrait);
        else {
          final int cfgNbPerLine = ApplicationCtx.getInstance().getNbBytesPerLine();
          nbPerLines = cfgNbPerLine == SysHelper.MAX_BY_ROW_16 ? SysHelper.MAX_BY_ROW_16 : SysHelper.MAX_BY_ROW_8;
          index += lineEntry.getShiftOffset();
        }
      }
      if (index > nbPerLines)
        start++;
      else if ((index + query.length()) <= nbPerLines)
        end--;
      /* We add all occurrences. */
      for (int j = i + start; j < end; j++)
        tempList.add(j);
    }
  }

  /**
   * Custom filter
   */
  private class EntryFilter extends Filter {

    protected void apply(CharSequence constraint, final Set<Integer> tempList) {
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
          multilineSearch(lineEntry, items, length, i, query, loc, tempList);
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
      notifyDataSetChanged();
    }
  }
}

