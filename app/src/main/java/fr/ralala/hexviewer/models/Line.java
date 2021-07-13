package fr.ralala.hexviewer.models;

import java.util.List;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Line entry.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class Line {
  private final String mPlain;
  private final List<Byte> mRaw;

  public Line(final String plain, List<Byte> raw) {
    mPlain = plain;
    mRaw = raw;
  }

  @SuppressWarnings("NullableProblems")
  @Override
  public String toString() {
    return mPlain;
  }

  /**
   * Returns the plain value.
   *
   * @return String
   */
  public String getPlain() {
    return mPlain;
  }

  /**
   * Returns the raw value.
   *
   * @return List<Byte>
   */
  public List<Byte> getRaw() {
    return mRaw;
  }
}
