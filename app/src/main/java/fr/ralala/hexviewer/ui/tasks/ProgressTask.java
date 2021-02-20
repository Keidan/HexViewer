package fr.ralala.hexviewer.ui.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.utils.Helper;

/**
 *******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Generic task with progress.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public abstract class ProgressTask<Params, Result> extends AsyncTask<Params, Long, Result> {
  private final AlertDialog mDialog;
  final WeakReference<Activity> mActivityRef;
  final WeakReference<TextView> mTextRef;
  final AtomicBoolean mCancel;
  long mTotalSize = 0L;
  long mCurrentSize = 0L;

  ProgressTask(final Activity activity, boolean loading) {
    mCancel = new AtomicBoolean(false);
    mActivityRef = new WeakReference<>(activity);
    Activity a = mActivityRef.get();
    mDialog = new AlertDialog.Builder(a).create();
    mDialog.setCancelable(false);
    View v = a.getLayoutInflater().inflate(R.layout.progress_dialog, null);
    mTextRef = new WeakReference<>(v.findViewById(R.id.text));
    mTextRef.get().setText(loading ? R.string.loading : R.string.saving);
    v.findViewById(R.id.cancel).setOnClickListener((view) -> {
      mCancel.set(true);
      cancel(true);
      mDialog.dismiss();
    });
    mDialog.setView(v);
  }


  @Override
  protected void onProgressUpdate(Long ...values) {
    mCurrentSize += values[0];
    String text = mActivityRef.get().getString(R.string.loading) + " ";
    text += Helper.sizeToHuman(mCurrentSize) + " / " + Helper.sizeToHuman(mTotalSize);
    mTextRef.get().setText(text);
  }

  /**
   * Called before the execution of the task.
   */
  @Override
  protected void onPreExecute() {
    super.onPreExecute();
    mCurrentSize = 0L;
    if(mDialog != null)
      mDialog.show();
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