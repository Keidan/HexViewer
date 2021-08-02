package fr.ralala.hexviewer.ui.tasks;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.documentfile.provider.DocumentFile;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.Line;
import fr.ralala.hexviewer.models.LineData;
import fr.ralala.hexviewer.ui.utils.UIHelper;
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
  private OutputStream mOutputStream = null;
  private ParcelFileDescriptor mParcelFileDescriptor = null;
  private final SaveResultListener mListener;
  private final ContentResolver mContentResolver;
  private final Context mContext;

  public static class Result {
    private Runnable runnable;
    private String exception = null;
    private Uri uri = null;
  }

  public static class Request {
    private final Uri mUri;
    private final List<LineData<Line>> mEntries;
    private final Runnable mRunnable;

    public Request(Uri uri, List<LineData<Line>> entries, final Runnable runnable) {
      mUri = uri;
      mEntries = entries;
      mRunnable = runnable;
    }

  }

  public interface SaveResultListener {
    void onSaveResult(Uri uri, boolean success, final Runnable userRunnable);
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
      if (result.uri != null) {
        final DocumentFile dfile = DocumentFile.fromSingleUri(mContext, result.uri);
        if (dfile != null && dfile.exists() && !dfile.delete()) {
          Log.e(this.getClass().getSimpleName(), "File delete error");
        }
      }
      UIHelper.toast(mContext, mContext.getString(R.string.operation_canceled));
    } else if (result.exception == null)
      UIHelper.toast(mContext, mContext.getString(R.string.save_success));
    else
      UIHelper.toast(mContext, mContext.getString(R.string.exception) + ": " + result.exception);
    if (mListener != null)
      mListener.onSaveResult(result.uri, result.exception == null && !isCancelled(), result.runnable);
  }

  /**
   * Closes the stream.
   */
  private void close() {
    if (mOutputStream != null) {
      try {
        mOutputStream.close();
      } catch (final IOException e) {
        Log.e(this.getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
      }
      mOutputStream = null;
    }
    if (mParcelFileDescriptor != null) {
      try {
        mParcelFileDescriptor.close();
      } catch (final IOException e) {
        Log.e(this.getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
      }
      mParcelFileDescriptor = null;
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
    //final Activity activity = mActivityRef.get();
    final Result result = new Result();
    if (request == null) {
      result.exception = "Invalid param!";
      return result;
    }
    result.uri = request.mUri;
    result.runnable = request.mRunnable;
    publishProgress(0L);
    try {
      mParcelFileDescriptor = contentResolver.openFileDescriptor(result.uri, "wt");
      List<Byte> bytes = new ArrayList<>();
      for (LineData<Line> entry : request.mEntries)
        bytes.addAll(entry.getValue().getRaw());
      final byte[] data = SysHelper.toByteArray(bytes, mCancel);
      if (!isCancelled()) {
        mOutputStream = new FileOutputStream(mParcelFileDescriptor.getFileDescriptor());
        mTotalSize = data.length;
        final long count = mTotalSize / MAX_LENGTH;
        final long remain = mTotalSize - (count * MAX_LENGTH);

        long offset = 0;
        for (long i = 0; i < count && !isCancelled(); i++) {
          mOutputStream.write(data, (int) offset, MAX_LENGTH);
          publishProgress((long) MAX_LENGTH);
          offset += MAX_LENGTH;
        }
        if (!isCancelled() && remain > 0) {
          mOutputStream.write(data, (int) offset, (int) remain);
          publishProgress(remain);
        }
        mOutputStream.flush();
      }
    } catch (final Exception e) {
      result.exception = e.getMessage();
    } finally {
      close();
    }
    return result;
  }
}
