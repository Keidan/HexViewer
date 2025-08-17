package fr.ralala.hexviewer.ui.adapters.search;

import java.util.BitSet;

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
  private int mLength = 0;
  private BitSet mIndexes = null;
  private boolean mHexPart = false;
  private boolean mWithSpaces = false;
  private boolean mNotFromHexView = false;

  protected SearchResult() {
  }

  public void setAll(int length, BitSet indexes, boolean hexPart, boolean withSpaces, boolean notFromHexView) {
    mLength = length;
    mIndexes = indexes;
    mHexPart = hexPart;
    mWithSpaces = withSpaces;
    mNotFromHexView = notFromHexView;
  }

  public void clear() {
    setAll(0, null, false, false, true);
  }

  /**
   * Returns the length.
   *
   * @return int
   */
  public int getLength() {
    return mLength;
  }

  /**
   * Returns the indexes.
   *
   * @return BitSet
   */
  public BitSet getIndexes() {
    return mIndexes;
  }

  /**
   * Tests if the indexes are in the hexadecimal part.
   *
   * @return boolean
   */
  public boolean isHexPart() {
    return mHexPart;
  }

  /**
   * Tests if the indexes are in a search with spaces or not (only if isHexPart == true).
   *
   * @return boolean
   */
  public boolean isWithSpaces() {
    return mWithSpaces;
  }

  /**
   * Tests if the indexes aren't in the hexadecimal part.
   *
   * @return boolean
   */
  public boolean isNotFromHexView() {
    return mNotFromHexView;
  }
}
