package fr.ralala.hexviewer.ui.tasks;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import java.util.List;

import fr.ralala.hexviewer.application.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.FileData;
import fr.ralala.hexviewer.models.lines.LineEntry;
import fr.ralala.hexviewer.models.RawBuffer;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.system.SysHelper;
import fr.ralala.hexviewer.utils.io.RandomAccessFileChannel;

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
public class TaskSave extends ProgressTask<ContentResolver, TaskSave.Request, TaskSave.Result> {
  private static final int MAX_LENGTH = SysHelper.MAX_BY_ROW_16 * 10000;
  private RandomAccessFileChannel mRandomAccessFileChannel = null;
  private final SaveResultListener mListener;
  private final ContentResolver mContentResolver;
  private final Context mContext;
  private final int mMaxByLine;

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
    mContentResolver = activity.getContentResolver();
    mContext = activity;
    mListener = listener;
    mMaxByLine = ((ApplicationCtx)mContext.getApplicationContext()).getNbBytesPerLine();
  }

  /**
   * Called before the execution of the task.
   *
   * @return The Config.
   */
  @Override
  public ContentResolver onPreExecute() {
    super.onPreExecute();
    return mContentResolver;
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
        if (docFile != null && docFile.exists() && !docFile.delete()) {
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
   * Closes the stream.
   */
  private void close() {
    if (mRandomAccessFileChannel != null) {
      mRandomAccessFileChannel.close();
      mRandomAccessFileChannel = null;
    }
  }

  /**
   * Called when the async task is cancelled.
   */
  @Override
  public void onCancelled() {
    super.onCancelled();
    close();
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
   * @param contentResolver ContentResolver.
   * @param request         Request.
   * @return The result.
   */
  @SuppressWarnings("squid:S2093")
  @Override
  public Result doInBackground(final ContentResolver contentResolver, final Request request) {
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
    publishProgress(0L);
    mTotalSize = getTotalSize(request.mEntries, mMaxByLine);
    try {
      // Open the file channel in write-only mode
      mRandomAccessFileChannel = RandomAccessFileChannel.openForWriteOnly(contentResolver, result.fd.getUri(), !result.fd.isSequential());

      // Set the maximum batch size
      int maxLength = MAX_LENGTH;

      // Set initial file position for writing
      mRandomAccessFileChannel.setPosition(result.fd.getStartOffset());

      // Adjust maxLength for sequential files if file size is smaller
      if (result.fd.isSequential()) {
        maxLength = (int) Math.min(result.fd.getSize(), MAX_LENGTH);
      }

      // Temporary storage for a batch of bytes
      RawBuffer batch = new RawBuffer(2048);

      // Iterate over all line entries
      for (LineEntry entry : request.mEntries) {
        // Add the raw bytes of the current entry to the batch
        batch.addAll(entry.getRaw());

        // If batch reaches maxLength, write it to the file
        if (batch.size() >= maxLength) {
          byte[] data = batch.array();
          mRandomAccessFileChannel.write(data, 0, data.length);

          // Update progress on UI thread
          publishProgress((long) data.length);

          // Clear batch for next iteration
          batch.clear();

          // Stop if the task was cancelled
          if (isCancelled()) break;
        }
      }

      // Write any remaining bytes in the last batch
      if (!isCancelled() && batch.size() != 0) {
        byte[] data = batch.array();
        mRandomAccessFileChannel.write(data, 0, data.length);
        // Update progress on UI thread
        publishProgress((long) data.length);
      }
    } catch (final Exception e) {
      // Capture any exception in the result
      result.exception = e.getMessage();
    } finally {
      // Ensure the file channel is closed at the end
      close();
    }
    // Return the final result object
    return result;
  }
}
