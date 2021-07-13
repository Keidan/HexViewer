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
import java.util.concurrent.atomic.AtomicBoolean;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.Line;
import fr.ralala.hexviewer.models.LineData;
import fr.ralala.hexviewer.ui.adapters.HexTextArrayAdapter;
import fr.ralala.hexviewer.ui.adapters.PlainTextListArrayAdapter;
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
 *
 * License: GPLv3
 * <p>
 * ******************************************************************************
 */
public class TaskOpen extends ProgressTask<ContentResolver, Uri, TaskOpen.Result> {
  private static final String TAG = TaskOpen.class.getSimpleName();
  private static final int MAX_LENGTH = SysHelper.MAX_BY_ROW * 10000;
  private final HexTextArrayAdapter mAdapter;
  private final PlainTextListArrayAdapter mAdapterPlain;
  private final OpenResultListener mListener;
  private InputStream mInputStream = null;
  private final boolean mAddRecent;
  private final ContentResolver mContentResolver;
  private final Context mContext;

  public static class Result {
    private List<LineData<Line>> listHex = null;
    private List<LineData<String>> listPlain = null;
    private String exception = null;
  }

  public interface OpenResultListener {
    void onOpenResult(boolean success, boolean fromOpen);
  }

  public TaskOpen(final Activity activity,
                  final HexTextArrayAdapter adapter,
                  final PlainTextListArrayAdapter adapterPlain,
                  final OpenResultListener listener, final boolean addRecent) {
    super(activity, true);
    mContext = activity;
    mContentResolver = activity.getContentResolver();
    mAdapter = adapter;
    mAdapterPlain = adapterPlain;
    mListener = listener;
    mAddRecent = addRecent;
  }

  /**
   * Called before the execution of the task.
   * @return The Config.
   */
  @Override
  public ContentResolver onPreExecute() {
    super.onPreExecute();
    mAdapterPlain.clear();
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
      if (result.listPlain != null)
        mAdapterPlain.addAll(result.listPlain);
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
   * @param uri Uri.
   * @return The result.
   */
  @Override
  public Result doInBackground(ContentResolver contentResolver, Uri uri) {
    //final Activity activity = mActivityRef.get();
    final Result result = new Result();
    final List<LineData<Line>> list = new ArrayList<>();
    final List<LineData<String>> plain = new ArrayList<>();
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
          addPlain(plain, data, reads, mCancel);
          try {
            list.addAll(SysHelper.formatBuffer(data, reads, mCancel));
          } catch (IllegalArgumentException iae) {
            result.exception = iae.getMessage();
            break;
          }
          publishProgress((long) reads);
        }
        /* prepare result */
        if (result.exception == null) {
          result.listPlain = plain;
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


  /**
   * Sets the plain content.
   *
   * @param plain   The list.
   * @param payload The new payload.
   * @param length  The array length.
   * @param cancel  Used to cancel this method.
   */
  public void addPlain(final List<LineData<String>> plain, final byte[] payload, final int length, final AtomicBoolean cancel) {
    final StringBuilder sb = new StringBuilder();
    int nbPerLine = 0;
    for (int i = 0; i < length && (cancel == null || !cancel.get()); i++) {
      if (nbPerLine != 0 && (nbPerLine % SysHelper.MAX_BY_LINE) == 0) {
        sb.append((char) payload[i]);
        plain.add(new LineData<>(sb.toString()));
        nbPerLine = 0;
        sb.setLength(0);
      } else {
        sb.append((char) payload[i]);
        nbPerLine++;
      }
    }
    if ((cancel == null || !cancel.get()) && nbPerLine != 0) {
      plain.add(new LineData<>(sb.toString()));
    }
  }
}
