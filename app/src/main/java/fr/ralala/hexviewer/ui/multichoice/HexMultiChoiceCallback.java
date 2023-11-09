package fr.ralala.hexviewer.ui.multichoice;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.os.Handler;
import android.os.Looper;
import android.view.ActionMode;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.LineEntry;
import fr.ralala.hexviewer.ui.activities.MainActivity;
import fr.ralala.hexviewer.ui.adapters.HexTextArrayAdapter;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.SysHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * MultiChoiceModeListener implementation (Hex view)
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class HexMultiChoiceCallback extends GenericMultiChoiceCallback {
  private final ImageView mRefreshActionViewDelete;

  @SuppressLint("InflateParams")
  public HexMultiChoiceCallback(MainActivity mainActivity, final ListView listView, final HexTextArrayAdapter adapter) {
    super(mainActivity, listView, adapter);
    mRefreshActionViewDelete = (ImageView) mLayoutInflater.inflate(R.layout.refresh_action_view_delete, null);
  }

  /**
   * Returns the menu id.
   *
   * @return R.menu.main_plain_multi_choice
   */
  public int getMenuId() {
    return R.menu.main_hex_multi_choice;
  }

  /**
   * Copy action.
   *
   * @param mode The ActionMode providing the selection mode.
   * @return false on error.
   */
  protected boolean actionCopy(ActionMode mode) {
    List<Integer> selected = new ArrayList<>(mAdapter.getSelectedIds());
    if (selected.isEmpty()) {
      displayError(R.string.error_no_line_selected);
      return false;
    }
    StringBuilder sb = new StringBuilder();
    for (Integer i : selected) {
      LineEntry ld = mAdapter.getItem(i);
      sb.append(ld.getPlain()).append("\n");
    }
    try {
      ClipData clip = ClipData.newPlainText(mActivity.getString(R.string.app_name), sb);
      mClipboard.setPrimaryClip(clip);
    } catch (Exception exception) {
      ApplicationCtx.addLog(mActivity, "CopyHex",
        "E: TransactionTooLargeException size: " + sb.toString().length());
      displayError(R.string.error_too_many_text_copied);
      return false;
    }
    UIHelper.toast(mActivity, String.format(mActivity.getString(R.string.text_copied),
      SysHelper.sizeToHuman(mActivity, sb.length(), true, true, false)));
    // Close CAB
    new Handler(Looper.getMainLooper()).postDelayed(mode::finish, 500);
    return true;
  }

  /**
   * Clear action.
   *
   * @param item The item that was clicked.
   * @param mode The ActionMode providing the selection mode.
   */
  protected void actionClear(MenuItem item, ActionMode mode) {
    setActionView(item, mRefreshActionViewDelete, () -> {
      final List<Integer> selected = mAdapter.getSelectedIds();
      Map<Integer, LineEntry> map = new HashMap<>();
      // Captures all selected ids with a loop
      for (int i = selected.size() - 1; i >= 0; i--) {
        int position = selected.get(i);
        LineEntry lf = mAdapter.getItem(position);
        map.put(position, lf);
      }
      mActivity.getUnDoRedo().insertInUnDoRedoForDelete(mActivity, map).execute();
      mActivity.refreshTitle();
      closeActionMode(mode, false);
    });
  }

  /**
   * Edit action.
   *
   * @param mode The ActionMode providing the selection mode.
   * @return false on error.
   */
  protected boolean actionEdit(ActionMode mode) {
    if (!mActivity.getSearchQuery().trim().isEmpty()) {
      displayError(R.string.error_edition_search_in_progress);
      return false;
    }
    List<Integer> selected = new ArrayList<>(mAdapter.getSelectedIds());
    if (selected.isEmpty()) {
      displayError(R.string.error_no_line_selected);
      return false;
    }
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    int previous = selected.get(0);
    for (Integer i : selected) {
      if (previous != i && previous + 1 != i) {
        displayError(R.string.error_edition_continuous_selection);
        return false;
      }
      previous = i;
      LineEntry ld = mAdapter.getItem(i);
      for (Byte b : ld.getRaw())
        byteArrayOutputStream.write(b);
    }
    mActivity.getLauncherLineUpdate().startActivity(byteArrayOutputStream.toByteArray(),
      selected.get(0), selected.size(),
      mActivity.getFileData().getShiftOffset(), ((HexTextArrayAdapter) mAdapter).getCurrentLine(selected.get(0)));
    closeActionMode(mode, true);
    return true;
  }

}
