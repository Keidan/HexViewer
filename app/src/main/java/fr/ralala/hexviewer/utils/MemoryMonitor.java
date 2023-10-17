package fr.ralala.hexviewer.utils;

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

  public interface MemoryListener {
    void onLowAppMemory(boolean disabled, long available, long used, float percentUsed);
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
    if (mMemoryListener != null) {
      if (mThreshold == -1) {
        mMemoryListener.onLowAppMemory(true, 0, 0, 0);
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
    long available = Runtime.getRuntime().maxMemory();
    long used = Runtime.getRuntime().totalMemory();

    // Check for & and handle low memory state
    float percentUsed = 100f * (1f - ((float) used / available));
    if (percentUsed <= mThreshold) {
      mMemoryListener.onLowAppMemory(false, available, used, percentUsed);
      if (mAutoStop)
        stop();
    }

    // Repeat after a delay
    if (mMemoryHandler != null)
      mMemoryHandler.postDelayed(this, mCheckFrequencyMs);
  }
}
