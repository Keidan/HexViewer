package fr.ralala.hexviewer.models;

import java.util.ArrayList;
import java.util.List;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Used with the adapters.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class LineEntry {
  private String mPlain;
  private List<Byte> mRaw;
  private int mIndex;
  private boolean mUpdated;
  private int mShiftOffset;

  public LineEntry(final String plain, final List<Byte> raw) {
    mPlain = plain;
    mRaw = raw == null ? null : new ArrayList<>(raw);
    mShiftOffset = 0;
  }

  public LineEntry(LineEntry le) {
    mPlain = le.mPlain;
    mRaw = new ArrayList<>(le.mRaw);
    mIndex = le.mIndex;
    mUpdated = le.mUpdated;
    mShiftOffset = le.mShiftOffset;
  }

  /**
   * Sets the offset used to shift the text to the end of the line.
   *
   * @param shiftOffset The new value.
   */
  public void setShiftOffset(int shiftOffset) {
    mShiftOffset = shiftOffset;
  }

  /**
   * Returns the offset used to shift the text to the end of the line.
   *
   * @return int
   */
  public int getShiftOffset() {
    return mShiftOffset;
  }

  /**
   * Sets the values.
   *
   * @param plain Plain text
   * @param raw   Raw data.
   */
  public void setValues(String plain, List<Byte> raw) {
    mPlain = plain;
    mRaw = new ArrayList<>(raw);
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


  /**
   * Tests if the data is updated.
   *
   * @return boolean
   */
  public boolean isUpdated() {
    return mUpdated;
  }

  /**
   * Sets the data updated state.
   *
   * @param updated The new value.
   */
  public void setUpdated(boolean updated) {
    mUpdated = updated;
  }

  /**
   * Gets the origin index.
   *
   * @return int
   */
  public int getIndex() {
    return mIndex;
  }

  /**
   * Sets the origin index.
   *
   * @param index The new value.
   */
  public void setIndex(int index) {
    mIndex = index;
  }
}
