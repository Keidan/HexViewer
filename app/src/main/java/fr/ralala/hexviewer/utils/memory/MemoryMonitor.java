package fr.ralala.hexviewer.utils.memory;

import android.os.Handler;
import android.os.Looper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Memory monitor.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class MemoryMonitor implements Runnable {
  private Handler mMemoryHandler = null;
  private final float mThreshold;
  private final int mCheckFrequencyMs;
  private boolean mAutoStop = false;
  private MemoryListener mMemoryListener;
  private final MemoryInfo mMemoryInfo;

  public MemoryMonitor(final float threshold, final int checkFrequencyMs) {
    mThreshold = threshold;
    mCheckFrequencyMs = checkFrequencyMs;
    mMemoryInfo = new MemoryInfo();
  }

  /**
   * Loads memory information.
   */
  private void loadMemoryInfo() {
    Runtime r = Runtime.getRuntime();
    mMemoryInfo.setTotalMemory(r.maxMemory());
    mMemoryInfo.setUsedMemory(r.totalMemory() - r.freeMemory());
    mMemoryInfo.setTotalFreeMemory(mMemoryInfo.getTotalMemory() - mMemoryInfo.getUsedMemory());
    mMemoryInfo.setPercentUsed(mMemoryInfo.getUsedMemory() * 100.f / mMemoryInfo.getTotalMemory());
  }

  /**
   * Returns the last known memory information.
   *
   * @return MemoryInfo
   */
  public MemoryInfo getLastMemoryInfo() {
    return mMemoryInfo;
  }

  /**
   * Starts the memory monitor.
   *
   * @param memoryListener Lister called on low memory.
   * @param autoStop       Used to automatically shut down the monitor if low memory is detected.
   */
  public void start(final MemoryListener memoryListener, final boolean autoStop) {
    stop();
    mMemoryListener = memoryListener;
    mAutoStop = autoStop;
    loadMemoryInfo();
    if (mMemoryListener != null) {
      if (mThreshold == -1) {
        mMemoryListener.onLowAppMemory(true, mMemoryInfo);
      } else {
        mMemoryHandler = new Handler(Looper.getMainLooper());
        run();
      }
    }
  }

  /**
   * Stops the memory monitor.
   */
  public void stop() {
    if (mMemoryHandler != null) {
      mMemoryHandler.removeCallbacks(this);
      mMemoryHandler = null;
    }
  }


  public void run() {
    // Get app memory info
    loadMemoryInfo();
    if ((100.f - mMemoryInfo.getPercentUsed()) <= mThreshold) {
      mMemoryListener.onLowAppMemory(false, mMemoryInfo);
      if (mAutoStop)
        stop();
    }

    // Repeat after a delay
    if (mMemoryHandler != null)
      mMemoryHandler.postDelayed(this, mCheckFrequencyMs);
  }
}
