package fr.ralala.hexviewer.ui.launchers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import fr.ralala.hexviewer.application.ApplicationCtx;
import fr.ralala.hexviewer.models.lines.LineEntry;
import fr.ralala.hexviewer.ui.activities.ICommonUI;
import fr.ralala.hexviewer.ui.activities.LineUpdateActivity;
import fr.ralala.hexviewer.ui.adapters.HexTextArrayAdapter;
import fr.ralala.hexviewer.utils.system.SysHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Launcher used with LineUpdate activity
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * <p>
 * ******************************************************************************
 */
// For now, I don't have the courage to change everything.
@SuppressWarnings("squid:S7091")
public class LauncherLineUpdate {
  private final AppCompatActivity mActivity;
  private final ICommonUI mCommonUI;
  private final ApplicationCtx mApp;
  private ActivityResultLauncher<Intent> activityResultLauncherLineUpdate;

  public LauncherLineUpdate(AppCompatActivity activity, ICommonUI commonUI) {
    mActivity = activity;
    mCommonUI = commonUI;
    mApp = (ApplicationCtx) mActivity.getApplicationContext();
    register();
  }

  /**
   * Starts the activity.
   *
   * @param texts    The hex texts.
   * @param position The position in the list view.
   */
  public void startActivity(final byte[] texts, final int position, final int nbLines, final int shiftOffset, final long startRow) {
    Intent intent = new Intent(mActivity, LineUpdateActivity.class);
    intent.putExtra(LineUpdateActivity.ACTIVITY_EXTRA_TEXTS, texts);
    intent.putExtra(LineUpdateActivity.ACTIVITY_EXTRA_POSITION, position);
    intent.putExtra(LineUpdateActivity.ACTIVITY_EXTRA_NB_LINES, nbLines);
    intent.putExtra(LineUpdateActivity.ACTIVITY_EXTRA_FILENAME, mCommonUI.getFileData().getName());
    intent.putExtra(LineUpdateActivity.ACTIVITY_EXTRA_CHANGE, mCommonUI.getUnDoRedo().isChanged());
    intent.putExtra(LineUpdateActivity.ACTIVITY_EXTRA_SEQUENTIAL, mCommonUI.getFileData().isSequential());
    intent.putExtra(LineUpdateActivity.ACTIVITY_EXTRA_SHIFT_OFFSET, shiftOffset);
    intent.putExtra(LineUpdateActivity.ACTIVITY_EXTRA_START_OFFSET, startRow);
    activityResultLauncherLineUpdate.launch(intent);
  }

  private void processIntentData(Intent data) {
    Bundle bundle = data.getExtras();
    if (bundle == null)
      return;
    String refString = bundle.getString(LineUpdateActivity.RESULT_REFERENCE_STRING);
    String newString = bundle.getString(LineUpdateActivity.RESULT_NEW_STRING);
    int position = bundle.getInt(LineUpdateActivity.RESULT_POSITION);
    int nbLines = bundle.getInt(LineUpdateActivity.RESULT_NB_LINES);

    byte[] buf = SysHelper.hexStringToByteArray(Objects.requireNonNull(newString));
    final byte[] ref = SysHelper.hexStringToByteArray(Objects.requireNonNull(refString));
    if (Arrays.equals(ref, buf)) {
      /* nothing to do */
      return;
    }

    final List<LineEntry> li = new ArrayList<>();
    int nbBytesPerLine = mApp.getNbBytesPerLine();
    if (position == 0 && mCommonUI.getFileData().getShiftOffset() != 0) {
      byte[] b = new byte[Math.min(buf.length, nbBytesPerLine - mCommonUI.getFileData().getShiftOffset())];
      System.arraycopy(buf, 0, b, 0, b.length);
      SysHelper.formatBuffer(li, b, b.length, null, nbBytesPerLine, mCommonUI.getFileData().getShiftOffset());
      byte[] buff2 = new byte[buf.length - b.length];
      System.arraycopy(buf, b.length, buff2, 0, buff2.length);
      buf = buff2;
    }
    processUndoRedo(li, buf, nbBytesPerLine, position, nbLines);
  }

  private void processUndoRedo(final List<LineEntry> li, final byte[] buf,
                               final int nbBytesPerLine, final int position, final int nbLines) {
    SysHelper.formatBuffer(li, buf, buf.length, null, nbBytesPerLine);
    HexTextArrayAdapter adapter = mCommonUI.getPayloadHex().getAdapter();
    if (li.isEmpty()) {
      Map<Integer, LineEntry> map = new TreeMap<>();
      for (int i = position; i < position + nbLines; i++) {
        map.put(adapter.getEntries().getItemIndex(i), adapter.getItem(i));
      }
      mCommonUI.getUnDoRedo().insertInUnDoRedoForDelete(map).execute();
    } else if (li.size() >= nbLines) {
      mCommonUI.getUnDoRedo().insertInUnDoRedoForUpdate(position, nbLines, li).execute();
    } else {
      Map<Integer, LineEntry> map = new TreeMap<>();
      for (int i = position + li.size(); i < position + nbLines; i++) {
        map.put(adapter.getEntries().getItemIndex(i), adapter.getItem(i));
      }
      mCommonUI.getUnDoRedo().insertInUnDoRedoForUpdateAndDelete(position, li, map).execute();
    }
  }

  /**
   * Registers result launcher for the activity for line update.
   */
  private void register() {
    activityResultLauncherLineUpdate = mActivity.registerForActivityResult(
      new ActivityResultContracts.StartActivityForResult(),
      result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
          Intent data = result.getData();
          if (data != null) {
            processIntentData(data);
          } else {
            Log.e(getClass().getSimpleName(), "Null data!!!");
            ApplicationCtx.addLog(mActivity, "LineUpdate", "Null intent data!");
          }
        }
      });
  }

}
