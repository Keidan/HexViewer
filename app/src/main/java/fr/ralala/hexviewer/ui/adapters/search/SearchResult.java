package fr.ralala.hexviewer.ui.adapters.search;

import java.util.Set;
/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * class used by SearchableFilterFactory
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class SearchResult {
  private final int mLength;
  private final Set<Integer> mIndexes;
  private final boolean mHexPart;
  private final boolean mWithSpaces;
  private final boolean mFromHexView;

  protected SearchResult(int length, Set<Integer> indexes, boolean hexPart, boolean withSpaces, boolean fromHexView) {
    mLength = length;
    mIndexes = indexes;
    mHexPart = hexPart;
    mWithSpaces = withSpaces;
    mFromHexView = fromHexView;
  }

  /**
   * Returns the length.
   * @return int
   */
  public int getLength() {
    return mLength;
  }

  /**
   * Returns the indexes.
   * @return Set<Integer>
   */
  public Set<Integer> getIndexes() {
    return mIndexes;
  }

  /**
   * Tests if the indexes are in the hexadecimal part.
   * @return boolean
   */
  public boolean isHexPart() {
    return mHexPart;
  }

  /**
   * Tests if the indexes are in a search with spaces or not (only if isHexPart == true).
   * @return boolean
   */
  public boolean isWithSpaces() {
    return mWithSpaces;
  }

  /**
   * Tests if the indexes are in the hexadecimal part.
   * @return boolean
   */
  public boolean isFromHexView() {
    return mFromHexView;
  }
}
