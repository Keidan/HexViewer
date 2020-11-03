package fr.ralala.hexviewer.ui.tasks;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.Helper;
import fr.ralala.hexviewer.utils.Payload;

/**
 *******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Task used to open a file.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class TaskOpen extends ProgressTask<Uri, Void, List<String>> {
  private ArrayAdapter<String> mAdapter;
  private InputStream mInputStream = null;

  public TaskOpen(final Activity activity, final ArrayAdapter<String> adapter) {
    super(activity);
    mAdapter = adapter;
  }

  /**
   * Called before the execution of the task.
   */
  @Override
  protected void onPreExecute() {
    super.onPreExecute();
    mAdapter.clear();
  }

  /**
   * Called after the execution of the task.
   * @param result The result.
   */
  @Override
  protected void onPostExecute(final List<String> result) {
    if(result != null)
      mAdapter.addAll(result);
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
        Log.e(this.getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
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
  }

  /**
   * Called after the execution of the process.
   * @param uris The result.
   */
  @Override
  protected List<String> doInBackground(final Uri... uris) {
    final Activity activity = mActivityRef.get();
    try {
      final ApplicationCtx app = (ApplicationCtx) activity.getApplication();
      if (uris.length != 0 && uris[0] != null)
        mInputStream = activity.getContentResolver().openInputStream(uris[0]);
      else
        mInputStream = new FileInputStream(app.getFilename());
      if(mInputStream != null) {
        Payload payload = app.getPayload();
        payload.clear();
        final byte[] data = new byte[1024*1024];
        int reads;
        while (mRunning && (reads = mInputStream.read(data)) != -1) {
          payload.add(data, reads);
        }
        return Helper.formatBuffer(payload.to());
      }
    } catch (final Exception e) {
      activity.runOnUiThread(() -> UIHelper.toast(activity, "Exception: " + e.getMessage()));
    } finally {
      close();
    }
    return null;
  }

}
