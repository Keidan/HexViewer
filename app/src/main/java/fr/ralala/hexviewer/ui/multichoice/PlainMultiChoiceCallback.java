package fr.ralala.hexviewer.ui.multichoice;

import android.view.ActionMode;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.LineEntry;
import fr.ralala.hexviewer.ui.activities.MainActivity;
import fr.ralala.hexviewer.ui.adapters.PlainTextListArrayAdapter;
import fr.ralala.hexviewer.utils.SysHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * MultiChoiceModeListener implementation (Plain view)
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class PlainMultiChoiceCallback extends GenericMultiChoiceCallback {

  public PlainMultiChoiceCallback(MainActivity mainActivity, final ListView listView, final PlainTextListArrayAdapter adapter) {
    super(mainActivity, listView, adapter);
  }

  /**
   * Returns the menu id.
   *
   * @return R.menu.main_plain_multi_choice
   */
  public int getMenuId() {
    return R.menu.main_plain_multi_choice;
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
      sb.append(SysHelper.ignoreNonDisplayedChar(ld.getPlain()));
    }
    return copyAndClose("CopyPlain", mode, sb);
  }

  /**
   * Clear action.
   *
   * @param item The item that was clicked.
   * @param mode The ActionMode providing the selection mode.
   */
  protected void actionClear(MenuItem item, ActionMode mode) {
    // nothing
  }

  /**
   * Edit action.
   *
   * @param mode The ActionMode providing the selection mode.
   * @return false on error.
   */
  protected boolean actionEdit(ActionMode mode) {
    return false;
  }
}
