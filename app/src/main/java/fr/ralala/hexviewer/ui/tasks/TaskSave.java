package fr.ralala.hexviewer.ui.tasks;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import androidx.documentfile.provider.DocumentFile;

import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.FileData;
import fr.ralala.hexviewer.models.LineEntry;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.io.RandomAccessFileChannel;
import fr.ralala.hexviewer.utils.SysHelper;

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

  /**
   * Performs a computation on a background thread.
   *
   * @param contentResolver ContentResolver.
   * @param request         Request.
   * @return The result.
   */
  @Override
  public Result doInBackground(final ContentResolver contentResolver, final Request request) {
    final Result result = new Result();
    if (request == null) {
      result.exception = "Invalid param!";
      return result;
    }
    result.fd = request.mFd;
    result.runnable = request.mRunnable;
    publishProgress(0L);
    try {
      mRandomAccessFileChannel = RandomAccessFileChannel.openForWriteOnly(contentResolver, result.fd.getUri(), !result.fd.isSequential());
      List<Byte> bytes = new ArrayList<>();
      for (LineEntry entry : request.mEntries)
        bytes.addAll(entry.getRaw());
      final byte[] data = SysHelper.toByteArray(bytes, mCancel);
      if (!isCancelled()) {
        mTotalSize = data.length;
        int maxLength = MAX_LENGTH;
        AtomicLong position = new AtomicLong(result.fd.getStartOffset());
        mRandomAccessFileChannel.setPosition(position.get());
        if (result.fd.isSequential()) {
          maxLength = result.fd.getSize() < MAX_LENGTH ? (int) result.fd.getSize() : MAX_LENGTH;
        }
        final long count = mTotalSize / maxLength;
        final long remain = mTotalSize - (count * maxLength);

        long offset = 0;
        for (long i = 0; i < count && !isCancelled(); i++) {
          mRandomAccessFileChannel.write(data, (int) offset, maxLength);
          publishProgress((long) maxLength);
          offset += maxLength;
        }
        if (!isCancelled() && remain > 0) {
          mRandomAccessFileChannel.write(data, (int) offset, (int) remain);
          publishProgress(remain);
        }
      }
    } catch (final Exception e) {
      result.exception = e.getMessage();
    } finally {
      close();
    }
    return result;
  }
}
