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
 * ******************************************************************************
 */
public class LineData<T> {
  private T mValue;
  private boolean mUpdated;
  private boolean mFalselyDeleted;


  public LineData(LineData<T> ld) {
    mValue = ld.mValue;
    mUpdated = ld.mUpdated;
    mFalselyDeleted = ld.mFalselyDeleted;
  }

  public LineData(T value) {
    this(value, false);
  }

  public LineData(T value, boolean updated) {
    mValue = value;
    mUpdated = updated;
    mFalselyDeleted = false;
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

  /**
   * Tests if the line is falsely deleted or not.
   *
   * @return boolean
   */
  public boolean isFalselyDeleted() {
    return mFalselyDeleted;
  }

  /**
   * Sets if the line is falsely deleted or not.
   *
   * @param value The new value.
   */
  public void setFalselyDeleted(boolean value) {
    mFalselyDeleted = value;
  }
}
