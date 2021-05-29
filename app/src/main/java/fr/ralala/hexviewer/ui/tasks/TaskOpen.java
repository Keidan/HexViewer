package fr.ralala.hexviewer.ui.tasks;

import android.app.Activity;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.SysHelper;
import fr.ralala.hexviewer.utils.Payload;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Task used to open a file.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class TaskOpen extends ProgressTask<Uri, TaskOpen.Result> {
  private static final String TAG = TaskOpen.class.getSimpleName();
  private static final int MAX_LENGTH = SysHelper.MAX_BY_ROW * 10000;
  private final ArrayAdapter<String> mAdapter;
  private final ArrayAdapter<String> mAdapterPlain;
  private final OpenResultListener mListener;
  private InputStream mInputStream = null;

  public static class Result {
    private List<String> list = null;
    private List<String> listPlain = null;
    private String exception = null;
  }

  public interface OpenResultListener {
    void onOpenResult(boolean success);
  }

  public TaskOpen(final Activity activity,
                  final ArrayAdapter<String> adapter,
                  final ArrayAdapter<String> adapterPlain,
                  final OpenResultListener listener) {
    super(activity, true);
    mAdapter = adapter;
    mAdapterPlain = adapterPlain;
    mListener = listener;
  }

  /**
   * Called before the execution of the task.
   */
  @Override
  protected void onPreExecute() {
    super.onPreExecute();
    mAdapterPlain.clear();
    mAdapter.clear();
  }

  /**
   * Called after the execution of the task.
   *
   * @param result The result.
   */
  @Override
  protected void onPostExecute(final Result result) {
    Activity a = mActivityRef.get();
    if(mCancel.get())
      UIHelper.toast(a, a.getString(R.string.operation_canceled));
    else if(result.exception != null)
      UIHelper.toast(a, a.getString(R.string.exception) + ": " + result.exception);
    else {
      if (result.list != null)
        mAdapter.addAll(result.list);
      if (result.listPlain != null)
        mAdapterPlain.addAll(result.listPlain);
    }
    if(mListener != null)
      mListener.onOpenResult(result.exception == null && !mCancel.get());
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
   * Called when the task is cancelled.
   */
  @Override
  protected void onCancelled() {
    super.onCancelled();
    close();
    if(mListener != null)
      mListener.onOpenResult(false);
  }

  /**
   * Called after the execution of the process.
   *
   * @param values The params.
   */
  @Override
  protected Result doInBackground(Uri... values) {
    final Activity activity = mActivityRef.get();
    final Result result = new Result();
    final List<String> list = new ArrayList<>();
    try {
      final ApplicationCtx app = (ApplicationCtx) activity.getApplication();
      final Uri uri = values[0];
      /* Size + stream */
      final ContentResolver cr = activity.getContentResolver();
      mTotalSize = getFileSize(cr, uri);
      publishProgress(0L);
      mInputStream = cr.openInputStream(uri);
      if (mInputStream != null) {
        /* cleanup */
        final Payload payload = app.getPayload();
        payload.clear();
        /* prepare buffer */
        final byte[] data = new byte[MAX_LENGTH];
        int reads;
        /* read data */
        while (!mCancel.get() && (reads = mInputStream.read(data)) != -1) {
          payload.add(data, reads, mCancel);
          try {
            list.addAll(SysHelper.formatBuffer(data, reads, mCancel));
          } catch (IllegalArgumentException iae) {
            result.exception = iae.getMessage();
            break;
          }
          publishProgress((long)reads);
        }
        /* prepare result */
        if(result.exception == null) {
          result.listPlain = payload.getPlain();
          result.list = list;
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
   * Returns the file size.
   * @param cr ContentResolver
   * @param uri Uri
   * @return long
   */
  private static long getFileSize(ContentResolver cr, Uri uri) {
    ParcelFileDescriptor pfd = null;
    long size = 0L;
    try {
      pfd = cr.openFileDescriptor(uri, "r");
      if(pfd != null) {
        size = pfd.getStatSize();
        pfd.close();
      }
    } catch (IOException e) {
      Log.e(TAG, "Exception: " + e.getMessage(), e);
    } finally {
      if(pfd != null)
        try {
          pfd.close();
        } catch (IOException e) {
          Log.e(TAG, "Exception: " + e.getMessage(), e);
        }
    }
    return size;
  }
}
