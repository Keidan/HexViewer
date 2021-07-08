package fr.ralala.hexviewer.ui.undoredo.commands;

import java.util.List;
import java.util.Map;

import fr.ralala.hexviewer.models.Line;
import fr.ralala.hexviewer.models.LineFilter;
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
    //faire décalage indexes
    List<Integer> list = SysHelper.getMapKeys(mList);
    for (int i = list.size() - 1; i >= 0; i--) {
      int position = list.get(i);
      LineFilter<Line> ld = mAdapter.getFilteredList().get(position);
      mAdapter.getItems().get(ld.getOrigin()).setFalselyDeleted(true);
      mAdapter.getFilteredList().remove(position);
    }
    mAdapter.notifyDataSetChanged();
  }

  /**
   * Un-Execute the command.
   */
  public void unExecute() {
    //faire restaure décalage indexes
    for (Integer i : SysHelper.getMapKeys(mList)) {
      LineFilter<Line> ld = mList.get(i);
      mAdapter.getFilteredList().add(i, ld);
      mAdapter.getItems().get(ld.getOrigin()).setFalselyDeleted(false);
    }
    mAdapter.notifyDataSetChanged();
  }
}
