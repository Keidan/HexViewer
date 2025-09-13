package fr.ralala.hexviewer.ui.tasks;


import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import java.util.List;

import fr.ralala.hexviewer.application.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.FileData;
import fr.ralala.hexviewer.models.lines.LineEntry;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.io.file.FileSaveProcessor;
import fr.ralala.hexviewer.utils.io.file.IFileProgress;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Task used to save a file.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class TaskSave extends ProgressTask<FileSaveProcessor, TaskSave.Request, TaskSave.Result> implements IFileProgress {
  private final SaveResultListener mListener;
  private final Context mContext;
  private final int mMaxByLine;
  private final FileSaveProcessor mFileSaveProcessor;

  public static class Result {
    private Runnable runnable;
    private String exception = null;
    private FileData fd = null;
  }

  public static class Request {
    private final FileData mFd;
    private final List<LineEntry> mEntries;
    private final Runnable mRunnable;

    public Request(FileData fd, List<LineEntry> entries, final Runnable runnable) {
      mFd = fd;
      mEntries = entries;
      mRunnable = runnable;
    }

  }

  public interface SaveResultListener {
    void onSaveResult(FileData fd, boolean success, final Runnable userRunnable);
  }

  public TaskSave(final Activity activity, final SaveResultListener listener) {
    super(activity, false);
    mContext = activity;
    mListener = listener;
    mMaxByLine = ((ApplicationCtx)mContext.getApplicationContext()).getNbBytesPerLine();
    mFileSaveProcessor = new FileSaveProcessor(activity.getContentResolver(), mCancel, this);
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
   *
   * @return The Config.
   */
  @Override
  public FileSaveProcessor onPreExecute() {
    super.onPreExecute();
    return mFileSaveProcessor;
  }

  /**
   * Called after the execution of the task.
   *
   * @param result The result.
   */
  @Override
  public void onPostExecute(final Result result) {
    super.onPostExecute(result);
    if (isCancelled()) {
      if (result.fd != null) {
        final DocumentFile docFile = DocumentFile.fromSingleUri(mContext, result.fd.getUri());
        if (docFile.exists() && !docFile.delete()) {
          Log.e(this.getClass().getSimpleName(), "File delete error");
        }
      }
      UIHelper.showErrorDialog(mContext, R.string.error_title, mContext.getString(R.string.operation_canceled));
    } else if (result.exception == null)
      UIHelper.toast(mContext, mContext.getString(R.string.save_success));
    else
      UIHelper.showErrorDialog(mContext, R.string.error_title, mContext.getString(R.string.exception) + ": " + result.exception);
    if (mListener != null)
      mListener.onSaveResult(result.fd, result.exception == null && !isCancelled(), result.runnable);
  }

  /**
   * Called when the async task is cancelled.
   */
  @Override
  public void onCancelled() {
    super.onCancelled();
    mFileSaveProcessor.close();
    UIHelper.toast(mContext, mContext.getString(R.string.operation_canceled));
  }

  private static long getTotalSize(List<LineEntry> entries, long maxByLine) {
    if(entries.isEmpty())
      return 0L;
    else
      return (entries.size() * maxByLine) + entries.get(entries.size() - 1).getRaw().length;
  }

  /**
   * Performs a computation on a background thread.
   *
   * @param fileSaveProcessor FileSaveProcessor.
   * @param request         Request.
   * @return The result.
   */
  @SuppressWarnings("squid:S2093")
  @Override
  public Result doInBackground(final FileSaveProcessor fileSaveProcessor, final Request request) {
    // Create a result object to store the outcome of the task
    final Result result = new Result();

    // Validate input request
    if (request == null) {
      result.exception = "Invalid param!";
      return result;
    }

    // Store file data and optional runnable from the request
    result.fd = request.mFd;
    result.runnable = request.mRunnable;

    // Initialize progress
    mTotalSize = getTotalSize(request.mEntries, mMaxByLine);
    try {
      fileSaveProcessor.writeFile(request.mFd, request.mEntries);
    } catch (final Exception e) {
      // Capture any exception in the result
      result.exception = e.getMessage();
    } finally {
      // Ensure the file channel is closed at the end
      fileSaveProcessor.close();
    }
    // Return the final result object
    return result;
  }
}
