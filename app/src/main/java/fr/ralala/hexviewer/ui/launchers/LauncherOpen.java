package fr.ralala.hexviewer.ui.launchers;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.Locale;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.FileData;
import fr.ralala.hexviewer.ui.activities.MainActivity;
import fr.ralala.hexviewer.ui.tasks.TaskOpen;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.io.FileHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Launcher used with the open part.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class LauncherOpen {
  private final MainActivity mActivity;
  private final LinearLayout mMainLayout;
  private final ApplicationCtx mApp;
  private ActivityResultLauncher<Intent> activityResultLauncherOpen;

  public LauncherOpen(MainActivity activity, LinearLayout mainLayout) {
    mActivity = activity;
    mApp = (ApplicationCtx) activity.getApplicationContext();
    mMainLayout = mainLayout;
    register();
  }

  /**
   * Starts the activity.
   */
  public void startActivity() {
    UIHelper.openFilePickerInFileSelectionMode(mActivity, activityResultLauncherOpen, mMainLayout);
  }

  /**
   * Registers result launcher for the activity for opening a file.
   */
  private void register() {
    activityResultLauncherOpen = mActivity.registerForActivityResult(
      new ActivityResultContracts.StartActivityForResult(),
      result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
          Intent data = result.getData();
          if (data != null) {
            if (FileHelper.takeUriPermissions(mActivity, data.getData(), false)) {
              processFileOpen(new FileData(mActivity, data.getData(), false, 0L, 0L));
            } else
              UIHelper.showErrorDialog(mActivity, R.string.error_title, String.format(mActivity.getString(R.string.error_file_permission), FileHelper.getFileName(mApp, data.getData())));
          } else {
            Log.e(getClass().getSimpleName(), "Null data!!!");
            mApp.setSequential(false);
          }
        } else
          mApp.setSequential(false);
      });
  }

  /**
   * Process the opening of the file
   */
  private void processFileOpen(final FileData fd) {
    processFileOpen(fd, null, true);
  }


  /**
   * Process the opening of the file
   *
   * @param fd FileData.
   */
  public void processFileOpen(final FileData fd, final String oldToString, final boolean addRecent) {
    if (fd != null && fd.getUri() != null && fd.getUri().getPath() != null) {
      final FileData previous = mActivity.getFileData();
      mActivity.setFileData(fd);
      Runnable r = () -> {
        mActivity.getUnDoRedo().clear();
        ApplicationCtx.addLog(mActivity, "Open",
          String.format(Locale.US, "Open file: '%s'", mActivity.getFileData()));
        new TaskOpen(mActivity, mActivity.getPayloadHex().getAdapter(), mActivity, oldToString, addRecent).execute(mActivity.getFileData());
      };
      if (mApp.isSequential())
        mActivity.getLauncherPartialOpen().startActivity(previous, oldToString, addRecent);
      else
        r.run();
    } else {
      UIHelper.showErrorDialog(mActivity, R.string.error_title, mActivity.getString(R.string.error_filename));
    }
  }
}
