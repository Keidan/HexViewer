package fr.ralala.hexviewer.models;

import androidx.annotation.NonNull;

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
public class LineData<T> {
  private T mValue;
  private boolean mUpdated;


  public LineData(LineData<T> ld) {
    mValue = ld.mValue;
    mUpdated = ld.mUpdated;
  }

  public LineData(T value) {
    this(value, false);
  }

  public LineData(T value, boolean updated) {
    mValue = value;
    mUpdated = updated;
  }

  @NonNull
  @Override
  public String toString() {
    return "" + mValue;
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
   * Gets the value.
   *
   * @return T
   */
  public T getValue() {
    return mValue;
  }

  /**
   * Sets the value.
   *
   * @param value The new value.
   */
  public void setValue(T value) {
    mValue = value;
  }

}
