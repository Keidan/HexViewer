package fr.ralala.hexviewer.utils;

import android.os.Handler;

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

  public interface MemoryListener {
    void onLowAppMemory();
  }

  public MemoryMonitor(final float threshold, final int checkFrequencyMs) {
    mThreshold = threshold;
    mCheckFrequencyMs = checkFrequencyMs;
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
    mMemoryHandler = new Handler();
    run();
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
    long available = Runtime.getRuntime().maxMemory();
    long used = Runtime.getRuntime().totalMemory();

    // Check for & and handle low memory state
    float percentUsed = 100f * (1f - ((float) used / available));
    if (percentUsed <= mThreshold && mMemoryListener != null) {
      mMemoryListener.onLowAppMemory();
      if (mAutoStop)
        stop();
      /* force cleanup */
      System.runFinalization();
      System.gc();
    }

    // Repeat after a delay
    if (mMemoryHandler != null)
      mMemoryHandler.postDelayed(this, mCheckFrequencyMs);
  }
}
