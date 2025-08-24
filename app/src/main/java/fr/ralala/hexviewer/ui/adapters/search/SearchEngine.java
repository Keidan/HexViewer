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
 * Engine for performing searches over a list of lines in either hex or plain text view.
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
public class SearchEngine {
  private final ISearchFrom mSearchFromHewView;
  private final ApplicationCtx mApp;
  private final RawBuffer mRawBuffer = new RawBuffer(1024);
  private boolean mIsSearchFromHexView;
  private int mLineWidth;

  /**
   * Represents the result of a search within a byte block.
   * <p>
   * Contains the starting offset of the match and its length in bytes.
   * Used internally by {@link SearchEngine#performsSearch(byte[], int, char[], MatchResult)}
   * to return match information without creating additional objects.
   * </p>
   */
  private static class MatchResult {
    /**
     * The offset within the block where the match starts.
     */
    int startOffset;

    /**
     * The length of the matched sequence in bytes.
     */
    int length;
  }

  public SearchEngine(final Context context,
                      ISearchFrom searchFromHewView) {
    mApp = (ApplicationCtx) context.getApplicationContext();
    mSearchFromHewView = searchFromHewView;

    reloadContext();
  }

  public SearchEngine(ISearchFrom searchFromHewView, int lineWidth) {
    mApp = null; /* Not used here */
    mSearchFromHewView = searchFromHewView;
    mIsSearchFromHexView = mSearchFromHewView.isSearchFromHexView();
    mLineWidth = lineWidth;
  }

  /**
   * Reloads the current search context (hex/plain) and line width.
   */
  public void reloadContext() {
    mIsSearchFromHexView = mSearchFromHewView.isSearchFromHexView();
    if (mApp != null)
      mLineWidth = mApp.getNbBytesPerLine();
    else
      mLineWidth = SysHelper.MAX_BY_ROW_16;
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
   * Performs a search over a byte block for a given query, in either hex or plain text mode.
   *
   * <p>This method iterates through the block and attempts to find a sequence matching
   * the query. It supports:
   * <ul>
   *     <li>Hex mode: compares each nibble against the query characters, skipping spaces.</li>
   *     <li>Plain mode: compares printable characters case-insensitively, converts non-printable bytes to '.'.</li>
   * </ul>
   *
   * <p><b>Design decision:</b>
   * This method is intentionally monolithic ("brain method") to maximize performance:
   * <ul>
   *     <li>Handles both hex and plain searches in a single pass.</li>
   *     <li>Minimizes object allocations and extra method calls inside tight loops.</li>
   *     <li>Necessary for efficiently processing bursts of 100k+ items.</li>
   * </ul>
   *
   * <p>Sonar warnings are suppressed intentionally with {@link SuppressWarnings}:
   * <ul>
   *     <li>S3776: Cognitive Complexity of methods should not be too high</li>
   *     <li>S135: Loops should not contain more than a single "break" or "continue" statement</li>
   *     <li>S6541: Methods should not perform too many tasks (aka Brain method)</li>
   * </ul>
   *
   * @param block       the byte array to search
   * @param blockLength the number of valid bytes in the block
   * @param query       the query characters to search for
   * @param result      a MatchResult object to store the starting offset and length of the match
   */
  @SuppressWarnings({"squid:S135", "squid:S3776", "squid:S6541"})
  private void performsSearch(byte[] block, int blockLength, char[] query, MatchResult result) {
    // Initialize result to indicate no match found
    result.startOffset = -1;
    result.length = 0;

    // Iterate over each possible starting position in the block
    for (int startBlockIndex = 0; startBlockIndex < blockLength; startBlockIndex++) {
      int queryIndex = 0;        // Position in the query
      int blockIndex = startBlockIndex;  // Position in the byte block
      int matchStart = -1;       // Tracks start of a potential match
      if (mIsSearchFromHexView) {
        // --- HEX MODE SEARCH ---
        int nibblePos = 0;         // 0 for high nibble, 1 for low nibble

        while (queryIndex < query.length && blockIndex < blockLength) {
          char qc = Character.toLowerCase(query[queryIndex]);

          // Skip spaces in the query
          if (qc == ' ') {
            queryIndex++;
            continue;
          }

          byte b = block[blockIndex];

          // Get expected character for current nibble of the byte
          char expectedNibble = (nibblePos == 0)
            ? SysHelper.HEX_LOWERCASE[(b >>> 4) & 0x0F]
            : SysHelper.HEX_LOWERCASE[b & 0x0F];

          // Compare query character to expected hex character (case-insensitive)
          if (qc == expectedNibble) {
            if (matchStart == -1) matchStart = startBlockIndex; // mark match start
            nibblePos++;
            queryIndex++;
            // Move to next byte after both nibbles matched
            if (nibblePos == 2) {
              nibblePos = 0;
              blockIndex++;
              result.length++;
            }
          } else {
            // Mismatch → reset and break
            matchStart = -1;
            result.length = 0;
            break;
          }
        }

        // Full match found in hex mode
        if (queryIndex == query.length) {
          result.startOffset = matchStart;
          if (nibblePos == 1) {
            // The query stopped after a single nibble:
            // still count the entire byte
            result.length++;
          }
          return;
        }
      }

      // --- PLAIN TEXT MODE SEARCH ---
      queryIndex = 0;
      blockIndex = startBlockIndex;
      matchStart = -1;

      while (queryIndex < query.length && blockIndex < blockLength) {
        char qc = Character.toLowerCase(query[queryIndex]);
        byte b = block[blockIndex];

        // Convert non-printable characters to '.'
        char plainChar = (b == 0x09 || b == 0x0A || (b >= 0x20 && b < 0x7F)) ? (char) b : '.';

        // Compare query character to plain character (case-insensitive)
        if (Character.toLowerCase(plainChar) == qc) {
          if (matchStart == -1) matchStart = startBlockIndex;
          queryIndex++;
          blockIndex++;
          result.length++; // increment matched byte count
        } else {
          // Mismatch → reset and break
          matchStart = -1;
          result.length = 0;
          break;
        }
      }

      // Full match found in plain text mode
      if (queryIndex == query.length) {
        result.startOffset = matchStart;
        return;
      }
    }
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
      if (startIndex < items.size()) {
        int firstLen = getLineByteLength(items.get(startIndex)); // longueur en octets
        int remaining = queryLength + Math.max(0, firstLen - 1); // marge
        while (endIndex < items.size() && remaining > 0) {
          remaining -= getLineByteLength(items.get(endIndex));
          endIndex++;
        }
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
    // Clear previous contents of the contiguous buffer
    mRawBuffer.clear();

    // Determine the end index of the block to search
    // If query length is 1, we only need the current line
    int endIndex = (query.length == 1) ? startIndex : getEndIndex(items, startIndex, query.length);

    // Copy bytes of each line into the raw buffer
    if (startIndex == endIndex)
      // Single line case
      insertByteList(items.get(startIndex));
    else
      // Multiple lines case
      for (int i = startIndex; i < endIndex; i++) {
        insertByteList(items.get(i));
      }

    // Retrieve the concatenated byte block
    byte[] block = mRawBuffer.getBytes();
    if (block == null || mRawBuffer.size() == 0) return;

    // Perform the actual search on the byte block
    MatchResult mr = new MatchResult();
    performsSearch(block, mRawBuffer.size(), query, mr);

    // If a match was found, resolve which lines contain the matched bytes
    if (mr.startOffset >= 0) {
      // Evaluate results and set matching lines
      int[] lines;
      if (startIndex == endIndex) {
        // Fixed-width single line: compute start/end lines using fixed line width
        lines = resolveLinesFixed(mLineWidth, startIndex, mr.startOffset, query.length);
      } else {
        // Dynamic multi-line case: compute start/end lines based on actual line lengths
        lines = resolveLinesDynamic(items, startIndex, mr.startOffset, mr.length);
      }
      // Mark all matching lines in the BitSet
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

    int firstLine = globalStart / lineSize;

    // Compute the global end offset of the match
    int globalEnd = globalStart + matchLength - 1;

    // The last line is obtained by integer division
    int lastLine = globalEnd / lineSize;

    // Return both line indices
    return new int[]{firstLine, lastLine};
  }

  /**
   * Returns the byte length of a line entry.
   * If raw bytes are available, returns their length; otherwise, returns
   * the length of the plain string.
   *
   * @param s the LineEntry to measure
   * @return the number of bytes or characters in the line
   */
  private static int getLineByteLength(LineEntry s) {
    if (s.getRaw() != null) {
      return s.getRaw().length;
    } else {
      return s.getPlain().length();
    }
  }

  /**
   * Resolves the start and end line indices of a match in a list of variable-length lines.
   *
   * @param items       the list of LineEntry items
   * @param startLine   index of the first line in the block
   * @param startOffset offset within the block where the match starts
   * @param matchLength length of the match in bytes
   * @return an array {firstLineIndex, lastLineIndex} containing the start and end lines of the match
   */
  public static int[] resolveLinesDynamic(List<LineEntry> items, int startLine, int startOffset, int matchLength) {
    int accumulated = 0;
    int firstLine = -1;
    int lastLine = -1;

    for (int i = startLine; i < items.size(); i++) {
      LineEntry line = items.get(i);
      int len = getLineByteLength(line);
      int lineEnd = accumulated + len - 1;

      // Set firstLine if match overlaps this line
      if (firstLine == -1 && startOffset <= lineEnd) {
        firstLine = i;
      }

      // Set lastLine if match overlaps this line
      if (firstLine != -1) {
        if (startOffset + matchLength - 1 <= lineEnd) {
          lastLine = i;
          break; // match fully covered
        } else {
          lastLine = i; // match continues in next lines
        }
      }

      accumulated += len;
    }

    if (firstLine == -1) firstLine = startLine;
    if (lastLine == -1) lastLine = startLine;

    return new int[]{firstLine, lastLine};
  }
}
