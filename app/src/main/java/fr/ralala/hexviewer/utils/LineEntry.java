package fr.ralala.hexviewer.utils;

import java.util.List;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Line entry.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */

public class LineEntry {
  private final String mPlain;
  private final List<Byte> mRaw;

  public LineEntry(final String plain, List<Byte> raw) {
    mPlain = plain;
    mRaw = raw;
  }

  @Override
  public String toString() {
    return mPlain;
  }

  /**
   * Returns the plain value.
   * @return String
   */
  public String getPlain() {
    return mPlain;
  }

  /**
   * Returns the raw value.
   * @return List<Byte>
   */
  public List<Byte> getRaw() {
    return mRaw;
  }
}
