package fr.ralala.hexviewer.ui.adapters.search;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.models.LineEntry;
import fr.ralala.hexviewer.ui.adapters.config.UserConfig;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.SysHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Filter used with SearchableListArrayAdapter
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class SearchableFilterFactory {
  private final Context mContext;
  private final UserConfig mUserConfigPortrait;
  private final UserConfig mUserConfigLandscape;
  private final ISearchFrom mSearchFromHewView;
  private final ApplicationCtx mApp;

  public SearchableFilterFactory(final Context context,
                                 ISearchFrom searchFromHewView,
                                 UserConfig userConfigPortrait,
                                 UserConfig userConfigLandscape) {
    mContext = context;
    mApp = (ApplicationCtx) context.getApplicationContext();
    mSearchFromHewView = searchFromHewView;
    mUserConfigLandscape = userConfigLandscape;
    mUserConfigPortrait = userConfigPortrait;
  }


  private void allIndexes(Set<Integer> indexes, String input, String query) {
    for (int index = input.indexOf(query);
         index >= 0; index = input.indexOf(query, index + 1)) {
      indexes.add(index);
    }
  }

  /**
   * Performs a search.
   *
   * @param line      The current line.
   * @param query     The query.
   * @param loc       The locale.
   * @param hexLength The length of the hex part.
   * @return The index of the query
   */
  private SearchResult performsSearch(final LineEntry line, String query, Locale loc, int hexLength) {
    String plain = line.toString();
    Set<Integer> indexes = new HashSet<>();
    /* hex part */
    final String hex = plain.substring(0, hexLength);
    int length = hex.length();
    allIndexes(indexes, hex.toLowerCase(loc), query);
    boolean hexPart = true;
    boolean withSpaces = true;
    if (indexes.isEmpty()) {
      /* hex no space */
      final String hexNoSpaces = plain.substring(0, hexLength).replace(" ", "");
      allIndexes(indexes, hexNoSpaces.toLowerCase(loc), query);
      if (indexes.isEmpty()) {
        /* plain text */
        hexPart = false;
        plain = plain.substring(hexLength + 2);
        allIndexes(indexes, plain.toLowerCase(loc), query);
        length = plain.length();
      } else {
        length = hexNoSpaces.length();
        withSpaces = false;
      }
    }
    return new SearchResult(length, indexes.isEmpty() ? null : indexes, hexPart, withSpaces, mSearchFromHewView.isSearchNotFromHexView());
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

  /**
   * Performs a search in the ByteArrayOutputStream
   *
   * @param byteArrayStream ByteArrayOutputStream
   * @param query           The query.
   * @param loc             Locale
   * @return SearchResult
   */
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
    return new SearchResult(0, null, false, false, true);
  }

  /**
   * Search on several lines
   *
   * @param lineEntry LineEntry
   * @param items     List<LineEntry>
   * @param i         Current index.
   * @param query     The query.
   * @param loc       Locale
   * @param tempList  The output list.
   */
  public void multilineSearch(final LineEntry lineEntry,
                              final List<LineEntry> items,
                              final int i,
                              final String query,
                              final Locale loc,
                              final Set<Integer> tempList) {
    /* The word fits on 2 or more lines */
    final int lineWidth;
    if (mSearchFromHewView.isSearchNotFromHexView())
      lineWidth = UIHelper.getMaxByLine(mContext, mUserConfigLandscape, mUserConfigPortrait) + 1;
    else {
      lineWidth = mApp.getNbBytesPerLine();
    }

    int count = (query.length() / lineWidth);
    int rest = (query.length() % lineWidth);
    if (rest != 0)
      count++;
    int possibleLines = count + 1;
    ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
    insertByteList(byteArrayStream, lineEntry);
    if (findInByteArrayOutputStream(byteArrayStream, query, loc).getIndexes() != null) {
      tempList.add(i);
      /* If query contains only 1 character there is no need to go further. */
      if (query.length() == 1)
        return;
    }
    if (query.length() == 1)
      return;

    int idx = i + 1;
    int k = idx;
    for (int j = idx; j <= i + possibleLines && k < (i + possibleLines) && j < items.size(); j++, k++) {
      LineEntry le = items.get(j);
      insertByteList(byteArrayStream, le);
    }

    /* Preparation of an entry with all the lines concerned. */
    SearchResult sr = findInByteArrayOutputStream(byteArrayStream, query, loc);
    evaluateResult(lineEntry.getShiftOffset(), i, sr, query, tempList);
    try {
      byteArrayStream.close();
    } catch (IOException e) {
      Log.e(getClass().getSimpleName(), "Close exception: " + e.getMessage(), e);
    }
  }

  /**
   * Evaluates the result of the research.
   *
   * @param shiftOffset LineEntry shiftOffset
   * @param i           Current index.
   * @param sr          SearchResult
   * @param query       The query.
   * @param tempList    The output list.
   */
  private void evaluateResult(final int shiftOffset, int i, SearchResult sr,
                              final String query, final Set<Integer> tempList) {
    if (sr.getIndexes() == null || (sr.isHexPart() && sr.isNotFromHexView()))
      return;
    for (Integer idx : sr.getIndexes()) {
      AtomicInteger index = new AtomicInteger(idx);
      AtomicInteger nbPerLines = new AtomicInteger();
      if (sr.isHexPart())
        prepareEvaluateResultHexPart(shiftOffset, sr, nbPerLines, index);
      else
        prepareEvaluateResultTextPart(shiftOffset, sr, nbPerLines, index);

      finalizeEvaluateResult(i, index.get(), nbPerLines.get(), query, tempList);
    }
  }

  private void prepareEvaluateResultHexPart(final int shiftOffset, final SearchResult sr,
                                            final AtomicInteger nbPerLines, final AtomicInteger index) {
    final int cfgNbPerLine = mApp.getNbBytesPerLine();
    if (sr.isWithSpaces()) {
      nbPerLines.set(cfgNbPerLine == SysHelper.MAX_BY_ROW_16 ? SysHelper.MAX_BYTES_ROW_16 : SysHelper.MAX_BYTES_ROW_8);
      index.set(index.get() + (shiftOffset * 3));
    } else {
      nbPerLines.set(cfgNbPerLine == SysHelper.MAX_BY_ROW_16 ? SysHelper.MAX_BY_ROW_16 * 2 : SysHelper.MAX_BY_ROW_8 * 2);
      index.set(index.get() + (shiftOffset * 2));
    }
  }

  private void prepareEvaluateResultTextPart(final int shiftOffset, final SearchResult sr,
                                             final AtomicInteger nbPerLines, final AtomicInteger index) {
    if (sr.isNotFromHexView())
      nbPerLines.set(UIHelper.getMaxByLine(mContext, mUserConfigLandscape, mUserConfigPortrait) + 1);
    else {
      final int cfgNbPerLine = mApp.getNbBytesPerLine();
      nbPerLines.set(cfgNbPerLine == SysHelper.MAX_BY_ROW_16 ? SysHelper.MAX_BY_ROW_16 : SysHelper.MAX_BY_ROW_8);
      index.set(index.get() + shiftOffset);
    }
  }

  private void finalizeEvaluateResult(final int i, final int index, final int nbPerLines,
                                      final String query, final Set<Integer> tempList) {
    final int full = index + query.length();
    int start = i + (index / nbPerLines);
    int end = i + (full / nbPerLines);
    if (full % nbPerLines == 0)
      end--;
    if (start == end) {
      tempList.add((index < nbPerLines) ? i : start);
    } else {
      for (int j = start; j <= end; j++)
        tempList.add(j);
    }
  }
}
