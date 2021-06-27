package fr.ralala.hexviewer.ui.undoredo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import fr.ralala.hexviewer.ui.adapters.HexTextArrayAdapter;
import fr.ralala.hexviewer.utils.LineEntry;

import static fr.ralala.hexviewer.ui.adapters.SearchableListArrayAdapter.FilterData;

public class DeleteCommand implements ICommand {
  private final Map<Integer, FilterData<LineEntry>> mDeleteList;
  private final HexTextArrayAdapter mAdapter;

  public DeleteCommand(final HexTextArrayAdapter adapter, final Map<Integer, FilterData<LineEntry>> entries) {
    mDeleteList = entries;
    mAdapter = adapter;
  }

  /**
   * Execute the command.
   */
  public void execute() {
    List<Integer> list = getKeys();
    for(int i = list.size() - 1; i >= 0; i--) {
      mAdapter.removeItem(i);
    }
    mAdapter.notifyDataSetChanged();
  }

  /**
   * Un-Execute the command.
   */
  public void unExecute() {
    for(Integer i : getKeys()) {
      FilterData<LineEntry> fd = mDeleteList.get(i);
      mAdapter.getFilteredList().add(i, fd);
      mAdapter.getItems().add(fd.origin, fd.value);
    }
    mAdapter.notifyDataSetChanged();
  }

  private List<Integer> getKeys() {
    List<Integer> sortedKeys = new ArrayList<>(mDeleteList.keySet());
    Collections.sort(sortedKeys);
    return sortedKeys;
  }
}
