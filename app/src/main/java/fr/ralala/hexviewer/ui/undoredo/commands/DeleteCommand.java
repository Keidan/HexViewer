package fr.ralala.hexviewer.ui.undoredo.commands;

import java.util.List;
import java.util.Map;

import fr.ralala.hexviewer.models.LineFilter;
import fr.ralala.hexviewer.ui.adapters.HexTextArrayAdapter;
import fr.ralala.hexviewer.models.Line;
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
 * ******************************************************************************
 */
public class DeleteCommand implements ICommand {
  private final Map<Integer, LineFilter<Line>> mList;
  private final HexTextArrayAdapter mAdapter;

  public DeleteCommand(final HexTextArrayAdapter adapter, final Map<Integer, LineFilter<Line>> entries) {
    mList = entries;
    mAdapter = adapter;
  }

  /**
   * Execute the command.
   */
  public void execute() {
    List<Integer> list = SysHelper.getMapKeys(mList);
    for(int i = list.size() - 1; i >= 0; i--) {
      mAdapter.removeItem(list.get(i));
    }
    mAdapter.notifyDataSetChanged();
  }

  /**
   * Un-Execute the command.
   */
  public void unExecute() {
    for(Integer i : SysHelper.getMapKeys(mList)) {
      LineFilter<Line> fd = mList.get(i);
      mAdapter.getFilteredList().add(i, fd);
      mAdapter.getItems().add(fd.getOrigin(), fd.getData());
    }
    mAdapter.notifyDataSetChanged();
  }
}
