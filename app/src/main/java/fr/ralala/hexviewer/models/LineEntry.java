package fr.ralala.hexviewer.models;

import androidx.annotation.Nullable;

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
  private byte[] mRaw;
  private int mIndex;
  private boolean mUpdated;
  private int mShiftOffset;

  public LineEntry(final String plain, @Nullable final RawBuffer buffer) {
    if (buffer == null)
      setValues(plain, null, 0);
    else
      setValues(plain, buffer.getBytes(), buffer.size());
    mShiftOffset = 0;
  }

  public LineEntry(LineEntry le) {
    setValues(le.mPlain, le.mRaw, le.mRaw == null ? 0 : le.mRaw.length);
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
   * @param plain  Plain text
   * @param raw    Raw data.
   * @param length Raw data length.
   */
  public void setValues(String plain, final byte[] raw, int length) {
    mPlain = plain;
    if (raw != null && length != 0) {
      mRaw = new byte[length];
      System.arraycopy(raw, 0, mRaw, 0, mRaw.length);
    } else
      mRaw = null;
  }

  /**
   * Sets the values.
   *
   * @param plain Plain text
   * @param raw   Raw data.
   */
  public void setValues(String plain, final byte[] raw) {
    setValues(plain, raw, raw == null ? 0 : raw.length);
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
   * @return byte[]
   */
  public byte[] getRaw() {
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
