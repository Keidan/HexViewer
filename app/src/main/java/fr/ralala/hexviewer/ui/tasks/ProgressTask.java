package fr.ralala.hexviewer.ui.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.view.View;

import java.lang.ref.WeakReference;

import fr.ralala.hexviewer.R;

/**
 *******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Generic task with progress.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public abstract class ProgressTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
  private final AlertDialog mDialog;
  WeakReference<Activity> mActivityRef;
  volatile boolean mRunning = true;

  ProgressTask(final Activity activity) {
    mActivityRef = new WeakReference<>(activity);
    Activity a = mActivityRef.get();
    mDialog = new AlertDialog.Builder(a).create();
    mDialog.setCancelable(true);
    View v = a.getLayoutInflater().inflate(R.layout.progress_dialog, null);
    mDialog.setView(v);
    mDialog.setOnCancelListener((dialog) -> cancel(true));
  }

  /**
   * Called before the execution of the task.
   */
  @Override
  protected void onPreExecute() {
    super.onPreExecute();
    if(mDialog != null)
      mDialog.show();
  }


  /**
   * Called when the task is cancelled.
   */
  @Override
  protected void onCancelled() {
    super.onCancelled();
    mRunning = false;
  }

  /**
   * Called after the execution of the task.
   * @param result The result.
   */
  @Override
  protected void onPostExecute(final Result result) {
    if(mDialog != null)
      mDialog.dismiss();
  }

}