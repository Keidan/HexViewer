package fr.ralala.hexviewer.ui.launchers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.FileData;
import fr.ralala.hexviewer.ui.activities.ICommonUI;
import fr.ralala.hexviewer.ui.activities.RecentlyOpenActivity;
import fr.ralala.hexviewer.ui.tasks.TaskSave;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.io.FileHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Launcher used with RecentlyOpen activity
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class LauncherRecentlyOpen {
  private final AppCompatActivity mActivity;
  private final ICommonUI mCommonUI;
  private final ApplicationCtx mApp;
  private ActivityResultLauncher<Intent> activityResultLauncherRecentlyOpen;

  public LauncherRecentlyOpen(AppCompatActivity activity, ICommonUI commonUI) {
    mActivity = activity;
    mCommonUI = commonUI;
    mApp = mCommonUI.getApplicationCtx();
    register();
  }

  /**
   * Starts the activity.
   */
  public void startActivity() {
    RecentlyOpenActivity.startActivity(mActivity, activityResultLauncherRecentlyOpen);
  }

  private void processIntentData(Intent data) {
    Uri uri = data.getData();
    long startOffset = data.getLongExtra(RecentlyOpenActivity.RESULT_START_OFFSET, 0L);
    long endOffset = data.getLongExtra(RecentlyOpenActivity.RESULT_END_OFFSET, 0L);
    final FileData fd = new FileData(mActivity, uri, false, startOffset, endOffset);
    final String oldToString;
    if (data.hasExtra(RecentlyOpenActivity.RESULT_OLD_TO_STRING))
      oldToString = data.getStringExtra(RecentlyOpenActivity.RESULT_OLD_TO_STRING);
    else
      oldToString = null;
    if (FileHelper.isFileExists(mActivity, mActivity.getContentResolver(), uri)) {
      processFile(fd, uri, oldToString);
    } else {
      UIHelper.showErrorDialog(mActivity, R.string.error_title, String.format(mActivity.getString(R.string.error_file_not_found), FileHelper.getFileName(mApp, uri)));
      mApp.getRecentlyOpened().remove(fd);
      FileHelper.releaseUriPermissions(mActivity, uri);
    }
  }

  private void processFile(final FileData fd, final Uri uri, final String oldToString) {
    if (FileHelper.hasUriPermission(mActivity, uri, true)) {
      final Runnable r = () -> {
        if (fd.getEndOffset() > fd.getRealSize())
          mApp.setSequential(true);
        mCommonUI.getLauncherOpen().processFileOpen(fd, oldToString, true);
      };
      if (mCommonUI.getUnDoRedo().isChanged()) {// a save operation is pending?
        UIHelper.confirmFileChanged(mActivity, mCommonUI.getFileData(), r, () -> new TaskSave(mActivity, mCommonUI).execute(
          new TaskSave.Request(mCommonUI.getFileData(), mCommonUI.getPayloadHex().getAdapter().getEntries().getItems(), r)));
      } else
        r.run();
    } else {
      UIHelper.showErrorDialog(mActivity, R.string.error_title, String.format(mActivity.getString(R.string.error_file_permission), FileHelper.getFileName(mApp, uri)));
      mApp.getRecentlyOpened().remove(fd);
    }
  }

  /**
   * Registers result launcher for the activity for line update.
   */
  private void register() {
    activityResultLauncherRecentlyOpen = mActivity.registerForActivityResult(
      new ActivityResultContracts.StartActivityForResult(),
      result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
          mCommonUI.setOrphanDialog(null);
          Intent data = result.getData();
          if (data != null && data.getData() != null) {
            processIntentData(data);
          } else if (mApp.getRecentlyOpened().list().isEmpty())
            mCommonUI.getMenuRecentlyOpen().setEnabled(false);
        }
      });
  }

}
