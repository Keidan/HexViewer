package fr.ralala.hexviewer.ui.launchers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.models.FileData;
import fr.ralala.hexviewer.ui.activities.MainActivity;
import fr.ralala.hexviewer.ui.activities.PartialOpenActivity;
import fr.ralala.hexviewer.ui.tasks.TaskOpen;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Launcher used with the partial open part.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class LauncherPartialOpen {
  private final MainActivity mActivity;
  private FileData mPrevious;
  private boolean mAddRecent;
  private ActivityResultLauncher<Intent> activityResultLauncherOpen;

  public LauncherPartialOpen(MainActivity activity) {
    mActivity = activity;
    register();
  }

  /**
   * Starts the activity.
   */
  public void startActivity(final FileData previous, final boolean addRecent) {
    mPrevious = previous;
    mAddRecent = addRecent;
    PartialOpenActivity.startActivity(mActivity, activityResultLauncherOpen, mActivity.getFileData());
  }

  /**
   * Registers result launcher for the activity for opening a file.
   */
  private void register() {
    Runnable cancel = () -> {
      ApplicationCtx.getInstance().setSequential(false);
      if (mPrevious == null) {
        mActivity.onOpenResult(false, false);
      } else {
        mActivity.setFileData(mPrevious);
        mActivity.setTitle(mActivity.getResources().getConfiguration());
      }
    };
    activityResultLauncherOpen = mActivity.registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
          ApplicationCtx.getInstance().setSequential(false);
          if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null) {
              Bundle bundle = data.getExtras();
              final long startOffset = bundle.getLong(PartialOpenActivity.RESULT_START_OFFSET);
              final long endOffset = bundle.getLong(PartialOpenActivity.RESULT_END_OFFSET);
              mActivity.getFileData().setOffsets(startOffset, endOffset);
              mActivity.getUnDoRedo().clear();
              new TaskOpen(mActivity, mActivity.getPayloadHex().getAdapter(), mActivity, mAddRecent).execute(mActivity.getFileData());
            } else {
              Log.e(getClass().getSimpleName(), "LauncherPartialOpen -> Invalid data object!!!");
              cancel.run();
            }
          } else {
            cancel.run();
          }
        });
  }
}
