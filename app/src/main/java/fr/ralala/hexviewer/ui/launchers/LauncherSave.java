package fr.ralala.hexviewer.ui.launchers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.documentfile.provider.DocumentFile;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.activities.MainActivity;
import fr.ralala.hexviewer.ui.dialog.SaveDialog;
import fr.ralala.hexviewer.ui.tasks.TaskSave;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.FileHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Launcher used with the save part.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class LauncherSave {
  private final MainActivity mActivity;
  private ActivityResultLauncher<Intent> activityResultLauncherSave;
  private final SaveDialog mSaveDialog;

  public LauncherSave(MainActivity activity) {
    mActivity = activity;
    mSaveDialog = new SaveDialog(activity,
        activity.getString(R.string.action_save_title));
    register();
  }

  /**
   * Starts the activity.
   */
  public void startActivity() {
    UIHelper.openFilePickerInDirectorSelectionMode(activityResultLauncherSave);
  }

  /**
   * Registers result launcher for the activity for saving a file.
   */
  private void register() {
    activityResultLauncherSave = mActivity.registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
          if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            if (data != null) {
              if (!mActivity.getFileData().isOpenFromAppIntent())
                FileHelper.takeUriPermissions(mActivity, data.getData(), true);
              processFileSaveWithDialog(data.getData());
            } else
              Log.e(getClass().getSimpleName(), "Null data!!!");
          }
        });
  }


  /**
   * Process the saving of the file
   *
   * @param uri Uri data.
   */
  private void processFileSaveWithDialog(final Uri uri) {
    mActivity.setOrphanDialog(mSaveDialog.show(mActivity.getFileData().getName(), (dialog, content, layout) -> {
      mActivity.setOrphanDialog(null);
      final String s_file = content.getText().toString();
      if (s_file.trim().isEmpty()) {
        layout.setError(mActivity.getString(R.string.error_filename));
        return;
      }
      processFileSave(uri, s_file);
      dialog.dismiss();
    }));
  }


  /**
   * Process the saving of the file
   *
   * @param uri      Uri data.
   * @param filename The filename
   */
  private void processFileSave(final Uri uri, final String filename) {
    DocumentFile sourceDir = DocumentFile.fromTreeUri(mActivity, uri);
    if (sourceDir == null) {
      UIHelper.toast(mActivity, mActivity.getString(R.string.uri_exception));
      Log.e(getClass().getSimpleName(), "1 - Uri exception: '" + uri + "'");
      return;
    }
    DocumentFile file = null;
    for (DocumentFile f : sourceDir.listFiles()) {
      if (f.getName() != null && f.getName().endsWith(filename)) {
        file = f;
        break;
      }
    }
    final DocumentFile f_file = file;

    if (file != null) {
      UIHelper.showConfirmDialog(mActivity, mActivity.getString(R.string.action_save_title),
          mActivity.getString(R.string.confirm_overwrite),
          (view) -> {
            new TaskSave(mActivity, mActivity).execute(new TaskSave.Request(f_file.getUri(), mActivity.getPayloadHex().getAdapter().getItems(), null));
            mActivity.setTitle(mActivity.getResources().getConfiguration());
          });
    } else {
      DocumentFile d_file = sourceDir.createFile("application/octet-stream", filename);
      if (d_file == null) {
        UIHelper.toast(mActivity, mActivity.getString(R.string.uri_exception));
        Log.e(getClass().getSimpleName(), "2 - Uri exception: '" + uri + "'");
      } else {
        new TaskSave(mActivity, mActivity).execute(new TaskSave.Request(d_file.getUri(), mActivity.getPayloadHex().getAdapter().getItems(), null));
        mActivity.setTitle(mActivity.getResources().getConfiguration());
      }
    }
  }
}
