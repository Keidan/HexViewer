package fr.ralala.hexviewer.models;

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
public class FilterData<T> {
  private T mValue;
  private int mOrigin;
  private boolean mUpdated = false;

  public FilterData(T value, int origin) {
    mValue = value;
    mOrigin = origin;
  }

  /**
   * Tests if the data is updated.
   * @return boolean
   */
  public boolean isUpdated() {
    return mUpdated;
  }

  /**
   * Sets the data updated state.
   * @param updated The new value.
   */
  public void setUpdated(boolean updated) {
    mUpdated = updated;
  }

  /**
   * Gets the origin index.
   * @return int
   */
  public int getOrigin() {
    return mOrigin;
  }

  /**
   * Sets origin index.
   * @param origin The new value.
   */
  public void setOrigin(int origin) {
    mOrigin = origin;
  }

  /**
   * Gets the value.
   * @return T
   */
  public T getValue() {
    return mValue;
  }

  /**
   * Sets the value.
   * @param value The new value.
   */
  public void setValue(T value) {
    mValue = value;
  }
}
