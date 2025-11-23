package fr.ralala.hexviewer.utils.memory;

import android.app.Application;
import android.content.ComponentCallbacks2;
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
  private final Application mApp;
  public MemoryMonitor(Application app, final float threshold, final int checkFrequencyMs) {
    mApp = app;
    mThreshold = threshold;
    mCheckFrequencyMs = checkFrequencyMs;
    mMemoryInfo = new MemoryInfo();
  }

  /**
   * Loads memory information.
   */
  private void loadMemoryInfo() {
    Runtime r = Runtime.getRuntime();
    mMemoryInfo.setTotalMemory(r.totalMemory());
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
   * Force memory cleanup.
   */
  @SuppressWarnings("squid:S1215")
  private void forceCleanup() {
    mApp.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_BACKGROUND);
    System.gc();
    Runtime.getRuntime().gc();
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
    forceCleanup();
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
