package fr.ralala.hexviewer.utils.memory;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Memory info.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class MemoryInfo {
  private long mTotalMemory = 0L;
  private long mUsedMemory = 0L;
  private long mTotalFreeMemory = 0L;
  private double mPercentUsed = 0.0;

  public long getTotalMemory() {
    return mTotalMemory;
  }

  public void setTotalMemory(long totalMemory) {
    mTotalMemory = totalMemory;
  }

  public long getUsedMemory() {
    return mUsedMemory;
  }

  public void setUsedMemory(long usedMemory) {
    mUsedMemory = usedMemory;
  }

  public long getTotalFreeMemory() {
    return mTotalFreeMemory;
  }

  public void setTotalFreeMemory(long totalFreeMemory) {
    mTotalFreeMemory = totalFreeMemory;
  }

  public double getPercentUsed() {
    return mPercentUsed;
  }

  public void setPercentUsed(double percentUsed) {
    mPercentUsed = percentUsed;
  }
}
