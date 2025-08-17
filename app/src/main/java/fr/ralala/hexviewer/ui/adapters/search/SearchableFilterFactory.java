package fr.ralala.hexviewer.ui.adapters.search;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.util.BitSet;
import java.util.List;

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
  private final ByteArrayOutputStream mByteArrayStream = new ByteArrayOutputStream(1024);
  private int mLineWidth;

  private static class Integers {
    int nbPerLines;
    int index;
  }

  private static class MatchContext {
    BitSet indexes = new BitSet();
    char c;
    char[] query;
    int queryLen = 0;
    int matchHexSpaces = 0;     // for hex with spaces
    int matchHexNoSpaces = 0;   // for hex without spaces
    int matchText = 0;          // for plain text
    int posHexNoSpaces = 0;     // position counter for hex without spaces
    int length;
    boolean withSpaces = true;
    boolean hexPart = true;
  }

  public SearchableFilterFactory(final Context context,
                                 ISearchFrom searchFromHewView,
                                 UserConfig userConfigPortrait,
                                 UserConfig userConfigLandscape) {
    mContext = context;
    mApp = (ApplicationCtx) context.getApplicationContext();
    mSearchFromHewView = searchFromHewView;
    mUserConfigLandscape = userConfigLandscape;
    mUserConfigPortrait = userConfigPortrait;

    reloadLineWidth();
  }

  public void reloadLineWidth() {
    /* The word fits on 2 or more lines */
    mLineWidth = mSearchFromHewView.isSearchNotFromHexView()
      ? UIHelper.getMaxByLine(mContext, mUserConfigLandscape, mUserConfigPortrait) + 1
      : mApp.getNbBytesPerLine();
  }

  protected int estimateBlockSize(int queryLength) {
    int count = queryLength / mLineWidth;
    if (queryLength % mLineWidth != 0) count++;
    return count + 1;
  }

  /**
   * Performs a search.
   *
   * @param line      The current line.
   * @param query     The query.
   * @param hexLength The length of the hex part.
   * @param sr        The SearchResult to fill
   */
  private void performsSearch(final LineEntry line, char[] query, int hexLength, SearchResult sr) {
    final char[] plainChars = line.toString().toCharArray();
    final int plainLength = plainChars.length;


    int textStart = hexLength + 2;
    int textLength = plainLength - textStart;
    MatchContext ctx = new MatchContext();
    ctx.query = query;
    ctx.queryLen = query.length;
    ctx.length = hexLength;
    // Single main loop scanning the entire line
    boolean found = false;
    for (int i = 0; i < plainLength; i++) {
      ctx.c = plainChars[i];
      if (i < hexLength) {
        // ---- Case 1: hex with spaces ----
        // ---- Case 2: hex without spaces ----
        if (matchHexWithSpace(ctx, i) || matchHexWithoutSpace(ctx))
          found = true;
      }
      // ---- Case 3: plain text ----
      else if (i >= textStart && matchPlainText(ctx, i, textStart, textLength)) {
        found = true;
      }
      if (found)
        break;
    }

    // Save final result
    sr.setAll(ctx.length, ctx.indexes.isEmpty() ? null : ctx.indexes,
      ctx.hexPart, ctx.withSpaces, mSearchFromHewView.isSearchNotFromHexView());
  }

  private static boolean matchHexWithSpace(MatchContext ctx, int index) {
    if (ctx.c == ctx.query[ctx.matchHexSpaces]) {
      ctx.matchHexSpaces++;
      if (ctx.matchHexSpaces == ctx.queryLen) {
        // Found a match in hex with spaces
        ctx.indexes.set(index - ctx.queryLen + 1);
        ctx.matchHexSpaces = 0; // reset to allow overlapping matches
        return true;
      }
    } else {
      // Reset progress if mismatch, restart if current char matches query[0]
      ctx.matchHexSpaces = (ctx.c == ctx.query[0]) ? 1 : 0;
    }
    return false;
  }

  private static boolean matchHexWithoutSpace(MatchContext ctx) {
    if (ctx.c != ' ') {
      if (ctx.c == ctx.query[ctx.matchHexNoSpaces]) {
        ctx.matchHexNoSpaces++;
        if (ctx.matchHexNoSpaces == ctx.queryLen) {
          // Found a match in hex without spaces
          ctx.indexes.set(ctx.posHexNoSpaces - ctx.queryLen + 1);
          ctx.withSpaces = false;
          ctx.length = ctx.posHexNoSpaces + 1;
          ctx.matchHexNoSpaces = 0;
          return true;
        }
      } else {
        // Reset progress
        ctx.matchHexNoSpaces = (ctx.c == ctx.query[0]) ? 1 : 0;
      }
      ctx.posHexNoSpaces++;
    }
    return false;
  }

  private static boolean matchPlainText(MatchContext ctx, int index, int textStart, int textLength) {
    char lower = Character.toLowerCase(ctx.c); // normalize to lowercase
    if (lower == ctx.query[ctx.matchText]) {
      ctx.matchText++;
      if (ctx.matchText == ctx.queryLen) {
        // Found a match in plain text
        ctx.indexes.set(index - textStart - ctx.queryLen + 1);
        ctx.hexPart = false;
        ctx.length = textLength;
        ctx.matchText = 0;
        return true;
      }
    } else {
      // Reset progress
      ctx.matchText = (lower == ctx.query[0]) ? 1 : 0;
    }
    return false;
  }

  /**
   * Adds the current line to a byte stream.
   *
   * @param s LineEntry
   */
  private void insertByteList(LineEntry s) {
    byte[] bytes;
    if (s.getRaw() != null) {
      bytes = s.getRaw();
    } else {
      char[] chars = s.getPlain().toCharArray();
      bytes = new byte[chars.length];
      for (int i = 0; i < bytes.length; i++) bytes[i] = (byte) chars[i];
    }
    mByteArrayStream.write(bytes, 0, bytes.length);
  }

  /**
   * Performs a search for the query across a block of consecutive lines.
   *
   * @param items      list of LineEntry items
   * @param startIndex index of the first line in the block
   * @param endIndex   index after the last line in the block
   * @param query      the query to search for
   * @param tempList   BitSet to mark matching lines
   */
  public void multilineSearchBlock(List<LineEntry> items, int startIndex, int endIndex,
                                   final char[] query, final BitSet tempList) {
    // Create a contiguous byte buffer for the block
    mByteArrayStream.reset();
    int shiftOffset = 0;
    for (int i = startIndex; i < endIndex; i++) {
      LineEntry le = items.get(i);
      insertByteList(le);
      if (shiftOffset == 0 && le.getShiftOffset() != 0) {
        shiftOffset = le.getShiftOffset();
      }
    }

    byte[] block = mByteArrayStream.toByteArray();
    if (block.length == 0) return;

    // Format block once
    LineEntry leBlock = SysHelper.formatBufferFast(block, block.length);
    int hexLengthBlock = block.length * 3 - 1;
    // Perform search in the formatted block
    SearchResult sr = new SearchResult();
    sr.clear();
    performsSearch(leBlock, query, hexLengthBlock, sr);
    // Evaluate results and set matching lines
    evaluateResult(shiftOffset, startIndex, sr, query, tempList);
  }

  /**
   * Evaluates the result of the research.
   *
   * @param shiftOffset LineEntry shiftOffset
   * @param i           Current index.
   * @param sr          SearchResult
   * @param query       The query.
   * @param tempList    The output BitSet.
   */
  private void evaluateResult(final int shiftOffset, int i, SearchResult sr,
                              final char[] query, final BitSet tempList) {
    BitSet indexes = sr.getIndexes();
    if (indexes == null || (sr.isHexPart() && sr.isNotFromHexView())) return;
    Integers integers = new Integers();
    for (int idx = indexes.nextSetBit(0); idx >= 0; idx = indexes.nextSetBit(idx + 1)) {
      integers.nbPerLines = 0;
      integers.index = idx;
      if (sr.isHexPart())
        prepareEvaluateResultHexPart(shiftOffset, sr, integers);
      else
        prepareEvaluateResultTextPart(shiftOffset, sr, integers);

      finalizeEvaluateResult(i, integers, query, tempList);
    }
  }

  private void prepareEvaluateResultHexPart(final int shiftOffset, final SearchResult sr, final Integers integers) {
    final int cfgNbPerLine = mApp.getNbBytesPerLine();
    if (sr.isWithSpaces()) {
      integers.nbPerLines = cfgNbPerLine == SysHelper.MAX_BY_ROW_16 ? SysHelper.MAX_BYTES_ROW_16 : SysHelper.MAX_BYTES_ROW_8;
      integers.index += shiftOffset * 3;
    } else {
      integers.nbPerLines = cfgNbPerLine == SysHelper.MAX_BY_ROW_16 ? SysHelper.MAX_BY_ROW_16 * 2 : SysHelper.MAX_BY_ROW_8 * 2;
      integers.index += shiftOffset * 2;
    }
  }

  private void prepareEvaluateResultTextPart(final int shiftOffset, final SearchResult sr, final Integers integers) {
    if (sr.isNotFromHexView())
      integers.nbPerLines = UIHelper.getMaxByLine(mContext, mUserConfigLandscape, mUserConfigPortrait) + 1;
    else {
      final int cfgNbPerLine = mApp.getNbBytesPerLine();
      integers.nbPerLines = cfgNbPerLine == SysHelper.MAX_BY_ROW_16 ? SysHelper.MAX_BY_ROW_16 : SysHelper.MAX_BY_ROW_8;
      integers.index += shiftOffset;
    }
  }

  private void finalizeEvaluateResult(final int i, final Integers integers,
                                      final char[] query, final BitSet tempList) {
    final int full = integers.index + query.length;
    int start = i + (integers.index / integers.nbPerLines);
    int end = i + (full / integers.nbPerLines);
    if (full % integers.nbPerLines == 0) end--;
    if (start == end) {
      tempList.set((integers.index < integers.nbPerLines) ? i : start);
    } else {
      for (int j = start; j <= end; j++)
        tempList.set(j);
    }
  }
}
