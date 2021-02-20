package fr.ralala.hexviewer.ui.tasks;


import android.app.Activity;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.Helper;
import fr.ralala.hexviewer.utils.Payload;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Task used to save a file.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class TaskSave extends ProgressTask<File, TaskSave.Result> {
  private static final int MAX_LENGTH = Helper.MAX_BY_ROW * 10000;
  private OutputStream mOutputStream = null;

  public static class Result {
    String exception = null;
    File file = null;
  }

  public TaskSave(final Activity activity) {
    super(activity, false);
  }

  /**
   * Called after the execution of the task.
   *
   * @param result The result.
   */
  @Override
  protected void onPostExecute(final Result result) {
    super.onPostExecute(result);
    Activity a = mActivityRef.get();
    if(mCancel.get())
    {
      if(result.file != null)
        if(!result.file.delete())
          Log.e(this.getClass().getSimpleName(), "File delete error");
      UIHelper.toast(a, a.getString(R.string.operation_canceled));
    } else if(result.exception == null)
      UIHelper.toast(a, a.getString(R.string.save_success));
    else
      UIHelper.toast(a, a.getString(R.string.exception) + ": " + result.exception);
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
   *
   * @param files The files.
   * @return Null or the exception message
   */
  @Override
  protected Result doInBackground(final File... files) {
    Activity activity = mActivityRef.get();
    Result result = new Result();
    result.file = files[0];
    final ApplicationCtx app = (ApplicationCtx) activity.getApplication();
    Payload payload = app.getPayload();
    publishProgress(0L);
    try {
      final byte[] data = payload.to(mCancel);
      if(!mCancel.get()) {
        mOutputStream = new FileOutputStream(result.file);
        mTotalSize = data.length;
        long count = mTotalSize / MAX_LENGTH;
        long remain = mTotalSize - (count * MAX_LENGTH);

        long offset = 0;
        for (long i = 0; i < count && !mCancel.get(); i++) {
          mOutputStream.write(data, (int) offset, MAX_LENGTH);
          publishProgress((long) MAX_LENGTH);
          offset += MAX_LENGTH;
        }
        if (!mCancel.get() && remain > 0) {
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
