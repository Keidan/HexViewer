package fr.ralala.hexviewer.models;

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
public class LineFilter<T> {
  private LineData<T> mData;
  private int mOrigin;

  public LineFilter(LineData<T> data, int origin) {
    mData = data;
    mOrigin = origin;
  }

  public LineFilter(LineFilter<T> fd) {
    mData = new LineData<>(fd.mData);
    mOrigin = fd.mOrigin;
  }


  /**
   * Gets the data.
   *
   * @return ListData<T>
   */
  public LineData<T> getData() {
    return mData;
  }

  /**
   * Sets the data.
   *
   * @param data The new value.
   */
  public void setData(LineData<T> data) {
    mData = data;
  }

  /**
   * Gets the origin index.
   *
   * @return int
   */
  public int getOrigin() {
    return mOrigin;
  }

  /**
   * Sets origin index.
   *
   * @param origin The new value.
   */
  public void setOrigin(int origin) {
    mOrigin = origin;
  }
}
