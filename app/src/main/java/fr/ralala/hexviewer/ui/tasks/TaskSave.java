package fr.ralala.hexviewer.ui.tasks;


import android.app.Activity;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.Payload;

/**
 *******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Task used to save a file.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class TaskSave extends ProgressTask<Void, Void, Void> {
  private OutputStream mOutputStream = null;

  public TaskSave(final Activity activity) {
    super(activity);
  }

  /**
   * Called after the execution of the task.
   * @param empty The result.
   */
  @Override
  protected void onPostExecute(final Void empty) {
    super.onPostExecute(empty);
    Activity a = mActivityRef.get();
    UIHelper.toast(a, a.getString(R.string.save_success));
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
   * @param empty The result.
   */
  @Override
  protected Void doInBackground(final Void... empty) {
    Activity activity = mActivityRef.get();
    final ApplicationCtx app = (ApplicationCtx) activity.getApplication();
    Payload payload = app.getPayload();
    try {
      mOutputStream = new FileOutputStream(app.getFilename());
      mOutputStream.write(payload.to());
      mOutputStream.flush();
    } catch (final Exception e) {
      activity.runOnUiThread(() -> UIHelper.toast(activity, "Exception: " + e.getMessage()));
    } finally {
      close();
    }
    return null;
  }
}
