package fr.ralala.hexviewer.ui.tasks;

import android.app.Activity;
import android.content.Context;
import android.text.format.Formatter;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.ralala.hexviewer.application.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.FileData;
import fr.ralala.hexviewer.models.lines.LineEntry;
import fr.ralala.hexviewer.ui.adapters.HexTextArrayAdapter;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.io.file.FileOpenProcessor;
import fr.ralala.hexviewer.utils.io.file.IFileProgress;
import fr.ralala.hexviewer.utils.memory.MemoryInfo;
import fr.ralala.hexviewer.utils.memory.MemoryListener;
import fr.ralala.hexviewer.utils.memory.MemoryMonitor;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Task used to open a file.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class TaskOpen extends ProgressTask<FileOpenProcessor, FileData, TaskOpen.Result> implements MemoryListener, IFileProgress {
  private final Context mContext;
  private final HexTextArrayAdapter mAdapter;
  private final OpenResultListener mListener;
  private final boolean mAddRecent;
  private final MemoryMonitor mMemoryMonitor;
  private final AtomicBoolean mLowMemory = new AtomicBoolean(false);
  private final String mOldToString;
  private final FileOpenProcessor mFileOpenProcessor;
  private final ApplicationCtx mApp;

  public static class Result {
    private List<LineEntry> listHex = null;
    private String exception = null;
    private long startOffset = 0;
  }

  public interface OpenResultListener {
    void onOpenResult(boolean success, boolean fromOpen);
  }

  public TaskOpen(final Activity activity,
                  final HexTextArrayAdapter adapter,
                  final OpenResultListener listener, final String oldToString, final boolean addRecent) {
    super(activity, true);
    mApp = (ApplicationCtx) activity.getApplicationContext();
    mMemoryMonitor = new MemoryMonitor(mApp.getMemoryThreshold(), 2000);
    mContext = activity;
    mAdapter = adapter;
    mListener = listener;
    mAddRecent = addRecent;
    mOldToString = oldToString;
    mFileOpenProcessor = new FileOpenProcessor(mApp, mContext.getContentResolver(), mCancel, this);
  }

  /**
   * Notifies the progress of a file operation.
   * <p>
   * This method is called to report the number of bytes processed
   * during a file read or write operation. Implementations can use
   * this information to update progress indicators, logs, or other
   * feedback mechanisms.
   *
   * @param bytes The number of bytes processed in the most recent batch.
   */
  @Override
  public void onFileProgress(long bytes) {
    publishProgress(bytes);
  }

  /**
   * Called before the execution of the task.
   * Initialize memory monitoring and clear the adapter.
   *
   * @return The Config.
   */
  @Override
  public FileOpenProcessor onPreExecute() {
    super.onPreExecute();
    mLowMemory.set(false);
    mMemoryMonitor.start(this, true);
    mAdapter.clear();
    return mFileOpenProcessor;
  }

  /**
   * Called after the execution of the task.
   * Handle result, memory status, and UI feedback.
   *
   * @param result The result.
   */
  @Override
  public void onPostExecute(final Result result) {
    super.onPostExecute(result);
    mMemoryMonitor.stop();
    // Show low memory error if detected
    if (mLowMemory.get())
      UIHelper.showErrorDialog(mContext, R.string.error_title, mContext.getString(R.string.not_enough_memory));
      // Show cancellation toast
    else if (isCancelled())
      UIHelper.toast(mContext, mContext.getString(R.string.operation_canceled));
      // Show exception if any
    else if (result.exception != null)
      UIHelper.showErrorDialog(mContext, R.string.error_title, mContext.getString(R.string.exception) + ": " + result.exception);
      // Update adapter with loaded hex lines
    else {
      if (result.listHex != null) {
        mAdapter.setStartOffset(result.startOffset);
        mAdapter.addAll(result.listHex);
      }
    }
    // Log memory status if task completed normally
    if (!mLowMemory.get()) {
      MemoryInfo mi = mMemoryMonitor.getLastMemoryInfo();
      ApplicationCtx.addLog(mContext, "Open",
        String.format(Locale.US, "Memory status, used: %s (%.02f%%), free: %s, max: %s",
          Formatter.formatFileSize(mContext, mi.getUsedMemory()), mi.getPercentUsed(),
          Formatter.formatFileSize(mContext, mi.getTotalFreeMemory()),
          Formatter.formatFileSize(mContext, mi.getTotalMemory())));
    }
    // Notify listener
    if (mListener != null)
      mListener.onOpenResult(result.exception == null && !isCancelled() && !mLowMemory.get(), true);
    super.onPostExecute(result);
  }

  /**
   * Called when the async task is cancelled.
   */
  @Override
  public void onCancelled() {
    mFileOpenProcessor.close();
    if (mListener != null)
      mListener.onOpenResult(false, true);
  }

  /**
   * Performs a computation on a background thread.
   *
   * @param fileOpenProcessor FileOpenProcessor.
   * @param fd              FileData.
   * @return The result.
   */
  @Override
  public Result doInBackground(FileOpenProcessor fileOpenProcessor, FileData fd) {
    final Result result = new Result();
    try {
      result.startOffset = fd.getStartOffset();
      /* Size + stream */
      mTotalSize = fd.getSize();
      // Prepare the result
      result.listHex = fileOpenProcessor.readFile(fd);
      if (!mCancel.get()) {
        if (mOldToString != null)
          mApp.getRecentlyOpened().remove(mOldToString);
        if (mAddRecent)
          mApp.getRecentlyOpened().add(fd);
      }
    } catch (final Exception e) {
      // Capture any exception in the result
      result.exception = e.getMessage();
    } finally {
      // Ensure the file channel is closed at the end
      fileOpenProcessor.close();
    }
    // Return the final result object
    return result;
  }

  public void onLowAppMemory(boolean disabled, MemoryInfo mi) {
    ApplicationCtx.addLog(mContext, "Open",
      String.format(Locale.US, "Low memory %s, used: %s (%.02f%%), free: %s, max: %s",
        disabled ? "disabled" : "detected",
        Formatter.formatFileSize(mContext, mi.getUsedMemory()), mi.getPercentUsed(),
        Formatter.formatFileSize(mContext, mi.getTotalFreeMemory()),
        Formatter.formatFileSize(mContext, mi.getTotalMemory())));
    if (!disabled) {
      mLowMemory.set(true);
      mCancel.set(true);
    }
  }
}
