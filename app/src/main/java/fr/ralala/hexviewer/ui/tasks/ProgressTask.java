package fr.ralala.hexviewer.ui.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.appcompat.app.AlertDialog;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.utils.SysHelper;

/**
 *******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Generic task with progress.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public abstract class ProgressTask<P, T> extends AsyncTask<P, Long, T> {
  private final AlertDialog mDialog;
  protected final WeakReference<Activity> mActivityRef;
  protected final WeakReference<TextView> mTextRef;
  protected final AtomicBoolean mCancel;
  protected long mTotalSize = 0L;
  protected long mCurrentSize = 0L;

  ProgressTask(final Activity activity, boolean loading) {
    mCancel = new AtomicBoolean(false);
    mActivityRef = new WeakReference<>(activity);
    final Activity a = mActivityRef.get();
    mDialog = new AlertDialog.Builder(a).create();
    mDialog.setCancelable(false);
    final View v = a.getLayoutInflater().inflate(R.layout.progress_dialog, null);
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
    text += SysHelper.sizeToHuman(mCurrentSize) + " / " + SysHelper.sizeToHuman(mTotalSize);
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
  protected void onPostExecute(final T result) {
    if(mDialog != null)
      mDialog.dismiss();
  }

}