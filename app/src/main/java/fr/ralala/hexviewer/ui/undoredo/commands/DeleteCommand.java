package fr.ralala.hexviewer.ui.undoredo.commands;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.ralala.hexviewer.models.LineEntry;
import fr.ralala.hexviewer.ui.activities.ICommonUI;
import fr.ralala.hexviewer.ui.adapters.HexTextArrayAdapter;
import fr.ralala.hexviewer.ui.undoredo.ICommand;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Command used when deleting an entry.
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
public class DeleteCommand implements ICommand {
  private final Map<Integer, LineEntry> mList;
  private final ICommonUI mCommonUI;

  public DeleteCommand(final ICommonUI commonUI, final Map<Integer, LineEntry> entries) {
    mList = entries;
    mCommonUI = commonUI;
  }

  /**
   * Execute the command.
   */
  public void execute() {
    Log.i(getClass().getName(), "execute");
    HexTextArrayAdapter adapter = mCommonUI.getPayloadHex().getAdapter();
    String query = mCommonUI.getSearchQuery();
    if (!query.isEmpty())
      adapter.manualFilterUpdate(""); /* reset filter */
    List<Integer> list = new ArrayList<>(mList.keySet());
    adapter.getEntries().removeItems(list);
    /* rebuilds origin indexes */
    adapter.getEntries().reloadAllIndexes(0);
    if (!query.isEmpty())
      adapter.manualFilterUpdate(query); /* restore filter */
    adapter.refresh();
  }

  /**
   * Un-Execute the command.
   */
  public void unExecute() {
    Log.i(getClass().getName(), "unExecute");
    HexTextArrayAdapter adapter = mCommonUI.getPayloadHex().getAdapter();
    String query = mCommonUI.getSearchQuery();
    if (!query.isEmpty())
      adapter.manualFilterUpdate(""); /* reset filter */

    List<Integer> list = new ArrayList<>(mList.keySet());
    for (Integer i : list) {
      LineEntry ld = mList.get(i);
      if (ld != null) {
        adapter.getEntries().addItem(ld.getIndex(), ld);
      }
    }
    /* rebuilds origin indexes */
    adapter.getEntries().reloadAllIndexes(0);

    if (!query.isEmpty())
      adapter.manualFilterUpdate(query); /* restore filter */
    adapter.refresh();
  }
}
