package fr.ralala.hexviewer.ui.adapters.search;

import android.content.Context;

import java.util.BitSet;
import java.util.List;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.models.LineEntry;
import fr.ralala.hexviewer.models.RawBuffer;
import fr.ralala.hexviewer.utils.SysHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Factory for performing searches over a list of lines in either hex or plain text view.
 * <p>
 * Supports multi-line searches with overlap handling. Uses a contiguous byte buffer
 * to perform efficient matching and maps matches back to line indices.
 * </p>
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class SearchableFilterFactory {
  private final ISearchFrom mSearchFromHewView;
  private final ApplicationCtx mApp;
  private final RawBuffer mRawBuffer = new RawBuffer(1024);
  private boolean mIsSearchFromHexView;
  private int mLineWidth;

  /**
   * Holds the state of a search within a byte block.
   * Tracks the current position in the query, the byte block,
   * and the nibble position for hex view.
   */
  private static class SearchContext {
    /** Current index in the query array */
    int indexInQuery = 0;
    /** Current index in the byte block */
    int indexInBlock = 0;
    /** Nibble position for hex view: 0 = high nibble, 1 = low nibble */
    int nibblePos = 0;
    /** Actual start index of the match in the block */
    int startIndex = -1;
  }

  /**
   * Represents the different types of matches that can occur when searching
   * within a byte block, either in hex view or plain text.
   */
  private enum MatchType {
    /** No match found. */
    NONE,
    /** Match found in the hex representation. */
    HEX,
    /** Space character ignored in hex search. */
    HEX_SPACE,
    /** Match found in plain text while searching from hex view. */
    PLAIN_FROM_HEX,
    /** Match found in plain text view. */
    PLAIN
  }

  public SearchableFilterFactory(final Context context,
                                 ISearchFrom searchFromHewView) {
    mApp = (ApplicationCtx) context.getApplicationContext();
    mSearchFromHewView = searchFromHewView;

    reloadContext();
  }
  /**
   * Reloads the current search context (hex/plain) and line width.
   */
  public void reloadContext() {
    mIsSearchFromHexView = mSearchFromHewView.isSearchFromHexView();
    mLineWidth = mApp.getNbBytesPerLine();
  }

  /**
   * Estimates how many lines are needed to hold the query in a fixed-width block.
   */
  protected int estimateBlockSize(int queryLength) {
    int count = queryLength / mLineWidth;
    if (queryLength % mLineWidth != 0) count++;
    return count + 1;
  }

  /**
   * Determines the type of match for a given character against a target character.
   *
   * @param ch  the character extracted from the byte (hex nibble or plain)
   * @param c   the character from the query to match
   * @param hex true if this comparison is for a hex nibble, false for plain text
   * @return the corresponding {@link MatchType} for this comparison
   */
  private MatchType getMatchType( char ch, char c, boolean hex) {
    if(ch == c) {
      if(hex)
        return MatchType.HEX;
      return mIsSearchFromHexView ? MatchType.PLAIN_FROM_HEX : MatchType.PLAIN;
    }
    return MatchType.NONE;
  }

  /**
   * Checks whether a given byte matches a query character according to the current
   * search mode (hex or plain). Advances the search context indices as appropriate.
   *
   * @param sc the search context tracking indices in the query and block
   * @param b  the byte to test
   * @param c  the query character to match
   * @return the {@link MatchType} indicating the result of the match
   */
  private MatchType isMatch(SearchContext sc, byte b, char c) {
    MatchType matched = MatchType.NONE;
    if (mIsSearchFromHexView) {
      // ignore space only for hex search
      if(c == ' ')
        return MatchType.HEX_SPACE;
      // pick current nibble
      char ch = (sc.nibblePos == 0)
        ? SysHelper.HEX_LOWERCASE[(b >>> 4) & 0x0F]
        : SysHelper.HEX_LOWERCASE[b & 0x0F];
      matched = getMatchType(ch, c, true);
      sc.nibblePos++;
      if (sc.nibblePos == 2) { // after low nibble, move to next byte
        sc.nibblePos = 0;
        sc.indexInBlock++;
      }
    }
    // Attempt to match as plain text if no match has been found yet
    if(matched == MatchType.NONE) {
      // plain text, map non-printable to '.'
      char ch = (b == 0x09 || b == 0x0A || (b >= 0x20 && b < 0x7F)) ? Character.toLowerCase((char) b) : '.';
      matched = getMatchType(ch, c, false);
      sc.indexInBlock++; // always advance one byte in plain view
    }
    return matched;
  }

  /**
   * Updates the indices in the search context based on the result of a match.
   *
   * @param sc    the search context to update
   * @param match the type of match returned by {@link #isMatch}
   */
  private void updateIndexes(SearchContext sc, MatchType match) {
    boolean matched = match == MatchType.HEX || match == MatchType.PLAIN || match == MatchType.PLAIN_FROM_HEX;
    // set startIndex at first non-space
    if (sc.startIndex == -1 && matched)
      sc.startIndex = sc.indexInBlock;

    // advance query index only if space or matched
    if (match == MatchType.HEX_SPACE || matched)
      sc.indexInQuery++;
  }

  /**
   * Performs a search for a query within a contiguous byte block.
   *
   * @param block       the byte array to search
   * @param blockLength the number of valid bytes in the block
   * @param query       the query characters to search for
   * @return the index of the first byte of the match in the block, or -1 if no match
   */
  private int performsSearch(byte[] block, int blockLength, char[] query) {
    SearchContext sc = new SearchContext();
    for (int i = 0; i < blockLength; i++) {
      sc.indexInQuery = 0;
      sc.indexInBlock = i;
      sc.nibblePos = 0;// 0 = high nibble, 1 = low nibble, used only in hex view
      sc.startIndex = -1;

      while (sc.indexInQuery < query.length && sc.indexInBlock < blockLength) {
        char qc = query[sc.indexInQuery];

        MatchType match = isMatch(sc, block[sc.indexInBlock], qc);

        updateIndexes(sc, match);

        // advance block index only if matched in plain/hex view
        if (match == MatchType.NONE) {
          // mismatch → stop scanning this start position
          break;
        }
      }

      if (sc.indexInQuery == query.length) {
        return sc.startIndex; // exact start of match
      }
    }
    return -1;
  }

  /**
   * Adds the current line to a byte stream.
   *
   * @param s LineEntry to insert
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
    mRawBuffer.addAll(bytes);
  }

  /**
   * Computes the end index of a block of lines to search based on query length
   * and current search mode (hex or plaintext).
   *
   * @param items       list of LineEntry items
   * @param startIndex  index of the first line in the block
   * @param queryLength length of the query
   * @return exclusive end index for the block
   */
  public int getEndIndex(List<LineEntry> items, int startIndex, int queryLength) {
    int endIndex = startIndex;

    if (mIsSearchFromHexView) {
      // Hex mode → estimate block size based on fixed-width lines
      endIndex = Math.min(startIndex + estimateBlockSize(queryLength), items.size());
    } else {
      // Plaintext mode → accumulate lines until we cover queryLength bytes
      int remaining = queryLength;
      while (endIndex < items.size() && remaining > 0) {
        remaining -= items.get(endIndex).getPlain().length();
        endIndex++;
      }
    }

    return endIndex;
  }

  /**
   * Searches for the query across a block of consecutive lines and marks matches.
   *
   * @param items      list of LineEntry items
   * @param startIndex index of the first line in the block
   * @param query      query to search for
   * @param tempList   BitSet to mark matching lines
   */
  public void multiLinesSearchBlock(List<LineEntry> items, int startIndex,
                                    final char[] query, final BitSet tempList) {
    // Create a contiguous byte buffer for the block
    mRawBuffer.clear();

    int endIndex = getEndIndex(items, startIndex, query.length);

    if (startIndex == endIndex)
      insertByteList(items.get(startIndex));
    else
      for (int i = startIndex; i < endIndex; i++) {
        insertByteList(items.get(i));
      }

    byte[] block = mRawBuffer.getBytes();
    if (block == null || mRawBuffer.size() == 0) return;

    // Perform search in the formatted block
    int startOffset = performsSearch(block, mRawBuffer.size(), query);
    if (startOffset >= 0) {
      // Evaluate results and set matching lines
      int[] lines;
      if (mIsSearchFromHexView) {
        lines = resolveLinesFixed(mLineWidth, startIndex, startOffset, query.length);
      } else {
        lines = resolveLinesDynamic(items, startIndex, startOffset, query.length);
      }
      for (int j = lines[0]; j <= lines[1]; j++)
        tempList.set(j);
    }
  }

  /**
   * Resolves the start and end line numbers of a match in a fixed-size block structure.
   * <p>
   * Assumes that all lines have the same fixed size except the last one,
   * which may be shorter. The method converts the match start (given by
   * line index and offset within that line) and the match length into
   * absolute positions, then computes the corresponding line indices.
   * </p>
   * <p>
   * Example:
   * <pre>
   *   lineSize = 16
   *   startLine = 2
   *   startOffset = 14
   *   matchLength = 6
   *
   *   globalStart = 2*16 + 14 = 46
   *   globalEnd   = 46 + 6 - 1 = 51
   *
   *   firstLine = 2
   *   lastLine  = 51 / 16 = 3
   *
   *   Result: {2, 3}
   * </pre>
   *
   * @param lineSize    the fixed size of each line (except the last line)
   * @param startLine   the index of the line where the match starts (0-based)
   * @param startOffset the offset within {@code startLine} where the match starts
   * @param matchLength the length of the match in bytes
   * @return an array of two integers: { firstLine, lastLine }
   * where {@code firstLine} is the starting line index and
   * {@code lastLine} is the ending line index of the match
   */
  public static int[] resolveLinesFixed(
    int lineSize,
    int startLine,
    int startOffset,
    int matchLength) {

    // Convert the start position into a global offset
    int globalStart = startLine * lineSize + startOffset;

    // Compute the global end offset of the match
    int globalEnd = globalStart + matchLength - 1;

    // The last line is obtained by integer division
    int lastLine = globalEnd / lineSize;

    // Return both line indices
    return new int[]{startLine, lastLine};
  }

  /**
   * Resolves the start and end line indices of a match in a list of lines
   * with variable lengths (plaintext mode).
   * <p>
   * This method computes which lines contain the matched sequence by
   * accumulating the lengths of each line until the match start and end
   * positions in the concatenated block are located.
   * </p>
   *
   * @param items       the list of LineEntry items
   * @param startLine   the index of the first line in the block (0-based)
   * @param startOffset the offset in bytes where the match starts relative to the block
   * @param matchLength the length of the match in bytes
   * @return an array of two integers: {firstLineIndex, lastLineIndex}
   *         where firstLineIndex is the line containing the start of the match,
   *         and lastLineIndex is the line containing the end of the match
   */
  public static int[] resolveLinesDynamic(List<LineEntry> items, int startLine, int startOffset, int matchLength) {
    int accumulated = 0;
    int firstLine = startLine;
    int lastLine = startLine;

    // Find the line where the match actually starts
    for (int i = startLine; i < items.size(); i++) {
      int lineLength = items.get(i).getPlain().length();
      if (accumulated + lineLength > startOffset) {
        firstLine = i;
        break;
      }
      accumulated += lineLength;
    }

    // Compute the absolute end offset of the match
    int globalEnd = startOffset + matchLength - 1;

    // Find the line where the match ends
    accumulated = 0;
    for (int i = startLine; i < items.size(); i++) {
      int lineLength = items.get(i).getPlain().length();
      accumulated += lineLength;
      if (accumulated > globalEnd) {
        lastLine = i;
        break;
      }
    }

    return new int[]{firstLine, lastLine};
  }
}
