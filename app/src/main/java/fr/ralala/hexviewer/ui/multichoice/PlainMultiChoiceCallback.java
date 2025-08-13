package fr.ralala.hexviewer.ui.multichoice;

import android.view.MenuItem;
import android.widget.ListView;

import androidx.appcompat.view.ActionMode;

import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.activities.MainActivity;
import fr.ralala.hexviewer.ui.adapters.PlainTextListArrayAdapter;

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
   * Clear action.
   *
   * @param item The item that was clicked.
   * @param mode The ActionMode providing the selection mode.
   */
  @Override
  protected void actionClear(MenuItem item, ActionMode mode) {
    // nothing
  }

  /**
   * Edit action.
   *
   * @param mode The ActionMode providing the selection mode.
   * @return false on error.
   */
  @Override
  protected boolean actionEdit(ActionMode mode) {
    return false;
  }

  /**
   * Test whether we are in plain text implementation or not
   *
   * @return boolean
   */
  protected boolean isFromPlainText() {
    return true;
  }
}
