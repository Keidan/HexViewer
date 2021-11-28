package fr.ralala.hexviewer.ui.launchers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.FileData;
import fr.ralala.hexviewer.ui.activities.MainActivity;
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
  private final MainActivity mActivity;
  private final ApplicationCtx mApp;
  private ActivityResultLauncher<Intent> activityResultLauncherRecentlyOpen;

  public LauncherRecentlyOpen(MainActivity activity) {
    mApp = ApplicationCtx.getInstance();
    mActivity = activity;
    register();
  }

  /**
   * Starts the activity.
   */
  public void startActivity() {
    RecentlyOpenActivity.startActivity(mActivity, activityResultLauncherRecentlyOpen);
  }

  /**
   * Registers result launcher for the activity for line update.
   */
  private void register() {
    activityResultLauncherRecentlyOpen = mActivity.registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
          if (result.getResultCode() == Activity.RESULT_OK) {
            mActivity.setOrphanDialog(null);
            Intent data = result.getData();
            if (data != null && data.getData() != null) {
              Uri uri = data.getData();
              long startOffset = data.getLongExtra(RecentlyOpenActivity.RESULT_START_OFFSET, 0L);
              long endOffset = data.getLongExtra(RecentlyOpenActivity.RESULT_END_OFFSET, 0L);
              FileData fd = new FileData(mActivity, uri, false, startOffset, endOffset);
              if (FileHelper.isFileExists(mActivity.getContentResolver(), uri)) {
                if (FileHelper.hasUriPermission(mActivity, uri, true)) {
                  final Runnable r = () -> mActivity.getLauncherOpen().processFileOpen(fd, true);
                  if (mActivity.getUnDoRedo().isChanged()) {// a save operation is pending?
                    UIHelper.confirmFileChanged(mActivity, mActivity.getFileData(), r, () -> new TaskSave(mActivity, mActivity).execute(
                        new TaskSave.Request(mActivity.getFileData(), mActivity.getPayloadHex().getAdapter().getEntries().getItems(), r)));
                  } else
                    r.run();
                } else {
                  UIHelper.toast(mActivity, String.format(mActivity.getString(R.string.error_file_permission), FileHelper.getFileName(uri)));
                  mApp.getRecentlyOpened().remove(fd);
                }
              } else {
                UIHelper.toast(mActivity, String.format(mActivity.getString(R.string.error_file_not_found), FileHelper.getFileName(uri)));
                mApp.getRecentlyOpened().remove(fd);
                FileHelper.releaseUriPermissions(mActivity, uri);
              }
            } else if (mApp.getRecentlyOpened().list().isEmpty())
              mActivity.getMenuRecentlyOpen().setEnabled(false);
          }
        });
  }

}
