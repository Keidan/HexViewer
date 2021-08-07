package fr.ralala.hexviewer.ui.undoredo.commands;

import android.util.Log;

import java.util.List;
import java.util.Map;

import fr.ralala.hexviewer.models.Line;
import fr.ralala.hexviewer.models.LineData;
import fr.ralala.hexviewer.models.LineFilter;
import fr.ralala.hexviewer.ui.activities.MainActivity;
import fr.ralala.hexviewer.ui.adapters.HexTextArrayAdapter;
import fr.ralala.hexviewer.ui.undoredo.ICommand;
import fr.ralala.hexviewer.utils.SysHelper;

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
public class DeleteCommand implements ICommand {
  private final Map<Integer, LineFilter<Line>> mList;
  private final MainActivity mActivity;

  public DeleteCommand(final MainActivity activity, final Map<Integer, LineFilter<Line>> entries) {
    mList = entries;
    mActivity = activity;
  }

  /**
   * Execute the command.
   */
  public void execute() {
    Log.i(getClass().getName(), "execute");
    HexTextArrayAdapter adapter = mActivity.getPayloadHex().getAdapter();
    String query = mActivity.getSearchQuery();
    if (!query.isEmpty())
      adapter.manualFilterUpdate(""); /* reset filter */
    List<Integer> list = SysHelper.getMapKeys(mList);

    List<LineFilter<Line>> filteredList = adapter.getFilteredList();
    List<LineData<Line>> items = adapter.getItems();

    for (int i = list.size() - 1; i >= 0; i--) {
      int position = list.get(i);
      LineFilter<Line> ld = filteredList.get(position);
      items.remove(ld.getOrigin());
      filteredList.remove(position);
    }
    /* rebuilds origin indexes */
    for (int i = 0; i < filteredList.size(); i++) {
      LineFilter<Line> ld = filteredList.get(i);
      ld.setOrigin(i);
    }
    if (!query.isEmpty())
      adapter.manualFilterUpdate(query); /* restore filter */
    adapter.notifyDataSetChanged();
  }

  /**
   * Un-Execute the command.
   */
  public void unExecute() {
    Log.i(getClass().getName(), "unExecute");
    HexTextArrayAdapter adapter = mActivity.getPayloadHex().getAdapter();
    String query = mActivity.getSearchQuery();
    if (!query.isEmpty())
      adapter.manualFilterUpdate(""); /* reset filter */

    List<LineFilter<Line>> filteredList = adapter.getFilteredList();
    List<LineData<Line>> items = adapter.getItems();

    for (Integer i : SysHelper.getMapKeys(mList)) {
      LineFilter<Line> ld = mList.get(i);
      if (ld != null) {
        filteredList.add(ld.getOrigin(), ld);
        items.add(ld.getOrigin(), ld.getData());
      }
    }
    /* rebuilds origin indexes */
    for (int i = 0; i < filteredList.size(); i++) {
      LineFilter<Line> ld = filteredList.get(i);
      ld.setOrigin(i);
    }
    if (!query.isEmpty())
      adapter.manualFilterUpdate(query); /* restore filter */
    adapter.notifyDataSetChanged();
  }
}
