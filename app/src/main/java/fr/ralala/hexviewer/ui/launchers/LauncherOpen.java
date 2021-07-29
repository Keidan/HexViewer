package fr.ralala.hexviewer.ui.launchers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.FileData;
import fr.ralala.hexviewer.ui.activities.MainActivity;
import fr.ralala.hexviewer.ui.tasks.TaskOpen;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.FileHelper;

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
  private ActivityResultLauncher<Intent> activityResultLauncherOpen;

  public LauncherOpen(MainActivity activity, LinearLayout mainLayout) {
    mActivity = activity;
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
                processFileOpen(data);
              } else
                UIHelper.toast(mActivity, String.format(mActivity.getString(R.string.error_file_permission), FileHelper.getFileName(data.getData())));
            } else
              Log.e(getClass().getSimpleName(), "Null data!!!");
          }
        });
  }

  /**
   * Process the opening of the file
   *
   * @param data Intent data.
   */
  private void processFileOpen(final Intent data) {
    Uri uri = data.getData();
    processFileOpen(uri, false, true);
  }


  /**
   * Process the opening of the file
   *
   * @param uri Uri data.
   */
  public void processFileOpen(final Uri uri, final boolean openFromAppIntent, final boolean addRecent) {
    if (uri != null && uri.getPath() != null) {
      mActivity.setFileData(new FileData(uri, openFromAppIntent));
      mActivity.getUnDoRedo().clear();
      new TaskOpen(mActivity, mActivity.getPayloadHex().getAdapter(), mActivity, addRecent).execute(uri);
    } else {
      UIHelper.toast(mActivity, mActivity.getString(R.string.error_filename));
    }
  }
}
