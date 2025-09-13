package fr.ralala.hexviewer.ui.multichoice;

import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.lines.LineEntry;
import fr.ralala.hexviewer.ui.activities.ICommonUI;
import fr.ralala.hexviewer.ui.adapters.HexTextArrayAdapter;

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
// For now, I don't have the courage to change everything.
@SuppressWarnings("squid:S7091")
public class HexMultiChoiceCallback extends GenericMultiChoiceCallback {

  public HexMultiChoiceCallback(AppCompatActivity activity, ICommonUI commonUI, final ListView listView, final HexTextArrayAdapter adapter) {
    super(activity, commonUI, listView, adapter);
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
   * Clear action.
   *
   * @param item The item that was clicked.
   * @param mode The ActionMode providing the selection mode.
   */
  @Override
  protected void actionClear(MenuItem item, ActionMode mode) {
    setActionView(item, null, done -> {
      final List<Integer> selected = mAdapter.getSelectedIds();
      final int totalSelected = selected.size();
      final int batchSize = evaluateBatch(totalSelected);

      // Map to hold positions and corresponding LineEntry for deletion
      final Map<Integer, LineEntry> map = new TreeMap<>();

      final Handler handler = new Handler(Looper.getMainLooper());
      final int[] index = {0};

      Runnable batchRunnable = new Runnable() {
        @Override
        public void run() {
          int end = Math.min(index[0] + batchSize, totalSelected);

          // Process current batch: capture selected positions and entries
          for (int i = index[0]; i < end; i++) {
            int position = selected.get(i);
            LineEntry lf = mAdapter.getItem(position);
            map.put(position, lf);
          }

          index[0] = end;

          if (index[0] < totalSelected) {
            handler.postDelayed(this, 1);
          } else {
            // Once all batches processed, execute deletion and UI update
            mCommonUI.getUnDoRedo().insertInUnDoRedoForDelete(map).execute();
            mCommonUI.refreshTitle();
            closeActionMode(mode, false);
            done.run();
          }
        }
      };

      handler.postDelayed(batchRunnable, getOverlayDelay(totalSelected));
    });
  }

  /**
   * Edit action.
   *
   * @param mode The ActionMode providing the selection mode.
   * @return false on error.
   */
  @Override
  protected boolean actionEdit(ActionMode mode) {
    if (!mCommonUI.getSearchQuery().trim().isEmpty()) {
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
      if (ld != null)
        for (Byte b : ld.getRaw())
          byteArrayOutputStream.write(b);
    }
    mCommonUI.getLauncherLineUpdate().startActivity(byteArrayOutputStream.toByteArray(),
      selected.get(0), selected.size(),
      mCommonUI.getFileData().getShiftOffset(), ((HexTextArrayAdapter) mAdapter).getCurrentLine(selected.get(0)));
    closeActionMode(mode, true);
    return true;
  }

  /**
   * Test whether we are in plain text implementation or not
   *
   * @return boolean
   */
  protected boolean isFromPlainText() {
    return false;
  }
}
