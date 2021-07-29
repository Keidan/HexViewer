package fr.ralala.hexviewer.ui.tasks;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.utils.SysHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Generic task with progress.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public abstract class ProgressTask<C, P, T> extends TaskRunner<C, P, Long, T> {
  private final AlertDialog mDialog;
  protected final TextView mTextView;
  protected long mTotalSize = 0L;
  protected long mCurrentSize = 0L;
  private final String progressText;

  ProgressTask(final Activity activity, boolean loading) {
    progressText = activity.getString(R.string.loading) + " ";
    mDialog = new AlertDialog.Builder(activity).create();
    mDialog.setCancelable(false);
    final View v = activity.getLayoutInflater().inflate(R.layout.progress_dialog, null);
    mTextView = v.findViewById(R.id.text);
    mTextView.setText(loading ? R.string.loading : R.string.saving);
    v.findViewById(R.id.cancel).setOnClickListener((view) -> {
      cancel();
      mDialog.dismiss();
    });
    mDialog.setView(v);
  }

  /**
   * Runs on the UI thread.
   *
   * @param value The value indicating progress.
   */
  @Override
  public void onProgressUpdate(Long value) {
    mCurrentSize += value;
    String text = progressText;
    text += SysHelper.sizeToHuman(mTextView.getContext(), mCurrentSize) + " / " + SysHelper.sizeToHuman(mTextView.getContext(), mTotalSize);
    mTextView.setText(text);
  }

  /**
   * Called before the execution of the task.
   *
   * @return The Config.
   */
  @Override
  public C onPreExecute() {
    mCurrentSize = 0L;
    if (mDialog != null)
      mDialog.show();
    return null;
  }

  /**
   * Called after the execution of the task.
   *
   * @param result The result.
   */
  @Override
  public void onPostExecute(final T result) {
    if (mDialog != null)
      mDialog.dismiss();
  }

}