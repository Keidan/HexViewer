package fr.ralala.hexviewer.ui.launchers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.models.FileData;
import fr.ralala.hexviewer.ui.activities.ICommonUI;
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
  private final AppCompatActivity mActivity;
  private final ICommonUI mCommonUI;
  private FileData mPrevious;
  @SuppressWarnings("squid:S1450")
  private boolean mAddRecent;
  @SuppressWarnings("squid:S1450")
  private String mOldToString;
  private ActivityResultLauncher<Intent> activityResultLauncherOpen;
  private final ApplicationCtx mApp;

  public LauncherPartialOpen(AppCompatActivity activity, ICommonUI commonUI) {
    mActivity = activity;
    mCommonUI = commonUI;
    mApp = mCommonUI.getApplicationCtx();
    register();
  }

  /**
   * Starts the activity.
   */
  public void startActivity(final FileData previous, final String oldToString, final boolean addRecent) {
    mPrevious = previous;
    mAddRecent = addRecent;
    mOldToString = oldToString;
    PartialOpenActivity.startActivity(mActivity, activityResultLauncherOpen, mCommonUI.getFileData());
  }

  /**
   * Registers result launcher for the activity for opening a file.
   */
  private void register() {
    Runnable cancel = () -> {
      mApp.setSequential(false);
      if (mPrevious == null) {
        mCommonUI.onOpenResult(false, false);
      } else {
        mCommonUI.setFileData(mPrevious);
        mCommonUI.refreshTitle();
      }
    };
    activityResultLauncherOpen = mActivity.registerForActivityResult(
      new ActivityResultContracts.StartActivityForResult(),
      result -> {
        mApp.setSequential(false);
        if (result.getResultCode() == Activity.RESULT_OK) {
          Intent data = result.getData();
          if (data != null && data.getExtras() != null) {
            Bundle bundle = data.getExtras();
            final long startOffset = bundle.getLong(PartialOpenActivity.RESULT_START_OFFSET);
            final long endOffset = bundle.getLong(PartialOpenActivity.RESULT_END_OFFSET);
            mCommonUI.getFileData().setOffsets(startOffset, endOffset, endOffset != 0L);
            mCommonUI.getUnDoRedo().clear();
            new TaskOpen(mActivity, mCommonUI.getPayloadHex().getAdapter(), mCommonUI, mOldToString, mAddRecent).execute(mCommonUI.getFileData());
          } else {
            Log.e(getClass().getSimpleName(), "LauncherPartialOpen -> Invalid data object!!!");
            ApplicationCtx.addLog(mActivity, "PartialOpen", "Null intent data!");
            cancel.run();
          }
        } else {
          cancel.run();
        }
      });
  }
}
