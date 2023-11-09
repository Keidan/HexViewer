package fr.ralala.hexviewer.ui.launchers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.documentfile.provider.DocumentFile;

import java.util.Locale;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.FileData;
import fr.ralala.hexviewer.ui.activities.MainActivity;
import fr.ralala.hexviewer.ui.dialog.SaveDialog;
import fr.ralala.hexviewer.ui.tasks.TaskSave;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.io.FileHelper;

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
          } else {
            Log.e(getClass().getSimpleName(), "Null data!!!");
            ApplicationCtx.addLog(mActivity, "Save", "Null intent data!");
          }
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
      UIHelper.showErrorDialog(mActivity, R.string.error_title,
        mActivity.getString(R.string.uri_exception) + ": '" + uri + "'");
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

    if (file != null) {
      final DocumentFile f = file;
      UIHelper.showConfirmDialog(mActivity, mActivity.getString(R.string.action_save_title),
        mActivity.getString(R.string.confirm_overwrite),
        view -> {
          FileData fd = new FileData(mActivity, f.getUri(), false);
          new TaskSave(mActivity, mActivity).execute(new TaskSave.Request(fd,
            mActivity.getPayloadHex().getAdapter().getEntries().getItems(), null));
          mActivity.refreshTitle();
        });
    } else {
      DocumentFile dFile = sourceDir.createFile("application/octet-stream", filename);
      if (dFile == null) {
        UIHelper.showErrorDialog(mActivity, R.string.error_title,
          mActivity.getString(R.string.uri_exception) + ": '" + filename + "'");
        Log.e(getClass().getSimpleName(), "2 - Uri exception: '" + uri + "', filename: '" + filename + "'");
      } else {
        FileData fd = new FileData(mActivity, dFile.getUri(), false);
        mActivity.setFileData(fd);
        ApplicationCtx.addLog(mActivity, "Save",
          String.format(Locale.US, "Save file: '%s'", mActivity.getFileData()));
        new TaskSave(mActivity, mActivity).execute(new TaskSave.Request(mActivity.getFileData(),
          mActivity.getPayloadHex().getAdapter().getEntries().getItems(), null));
        mActivity.refreshTitle();
      }
    }
  }
}
