package fr.ralala.hexviewer.ui.tasks;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.Line;
import fr.ralala.hexviewer.models.LineData;
import fr.ralala.hexviewer.ui.adapters.HexTextArrayAdapter;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.FileHelper;
import fr.ralala.hexviewer.utils.SysHelper;

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
public class TaskOpen extends ProgressTask<ContentResolver, Uri, TaskOpen.Result> {
  private static final String TAG = TaskOpen.class.getSimpleName();
  private static final int MAX_LENGTH = SysHelper.MAX_BY_ROW_16 * 20000;
  private final HexTextArrayAdapter mAdapter;
  private final OpenResultListener mListener;
  private InputStream mInputStream = null;
  private final boolean mAddRecent;
  private final ContentResolver mContentResolver;
  private final Context mContext;

  public static class Result {
    private List<LineData<Line>> listHex = null;
    private String exception = null;
  }

  public interface OpenResultListener {
    void onOpenResult(boolean success, boolean fromOpen);
  }

  public TaskOpen(final Activity activity,
                  final HexTextArrayAdapter adapter,
                  final OpenResultListener listener, final boolean addRecent) {
    super(activity, true);
    mContext = activity;
    mContentResolver = activity.getContentResolver();
    mAdapter = adapter;
    mListener = listener;
    mAddRecent = addRecent;
  }

  /**
   * Called before the execution of the task.
   *
   * @return The Config.
   */
  @Override
  public ContentResolver onPreExecute() {
    super.onPreExecute();
    mAdapter.clear();
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
    if (isCancelled())
      UIHelper.toast(mContext, mContext.getString(R.string.operation_canceled));
    else if (result.exception != null)
      UIHelper.toast(mContext, mContext.getString(R.string.exception) + ": " + result.exception);
    else {
      if (result.listHex != null)
        mAdapter.addAll(result.listHex);
    }
    if (mListener != null)
      mListener.onOpenResult(result.exception == null && !isCancelled(), true);
    super.onPostExecute(result);
  }

  /**
   * Closes the stream.
   */
  private void close() {
    if (mInputStream != null) {
      try {
        mInputStream.close();
      } catch (final IOException e) {
        Log.e(TAG, "Exception: " + e.getMessage(), e);
      }
      mInputStream = null;
    }
  }

  /**
   * Called when the async task is cancelled.
   */
  @Override
  public void onCancelled() {
    close();
    if (mListener != null)
      mListener.onOpenResult(false, true);
  }

  /**
   * Performs a computation on a background thread.
   *
   * @param contentResolver ContentResolver.
   * @param uri             Uri.
   * @return The result.
   */
  @Override
  public Result doInBackground(ContentResolver contentResolver, Uri uri) {
    //final Activity activity = mActivityRef.get();
    final Result result = new Result();
    final List<LineData<Line>> list = new ArrayList<>();
    try {
      final ApplicationCtx app = ApplicationCtx.getInstance();
      /* Size + stream */
      mTotalSize = FileHelper.getFileSize(contentResolver, uri);
      publishProgress(0L);
      mInputStream = contentResolver.openInputStream(uri);
      if (mInputStream != null) {
        /* prepare buffer */
        final byte[] data = new byte[MAX_LENGTH];
        int reads;
        /* read data */
        while (!isCancelled() && (reads = mInputStream.read(data)) != -1) {
          try {
            list.addAll(SysHelper.formatBuffer(data, reads, mCancel,
                ApplicationCtx.getInstance().getNbBytesPerLine()));
          } catch (IllegalArgumentException iae) {
            result.exception = iae.getMessage();
            break;
          }
          publishProgress((long) reads);
        }
        /* prepare result */
        if (result.exception == null) {
          result.listHex = list;
          if (mAddRecent)
            app.addRecentlyOpened(uri.toString());
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
