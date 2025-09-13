package fr.ralala.hexviewer.ui.undoredo.commands;

import android.util.Log;

import java.util.List;
import java.util.Map;

import fr.ralala.hexviewer.models.LineEntry;
import fr.ralala.hexviewer.ui.activities.ICommonUI;
import fr.ralala.hexviewer.ui.undoredo.ICommand;
import fr.ralala.hexviewer.ui.undoredo.UnDoRedo;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Command used when updating and deleting (2 in one) an entry.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class UpdateAndDeleteCommand implements ICommand {
  private final DeleteCommand mDelete;
  private final UpdateCommand mUpdate;


  public UpdateAndDeleteCommand(final UnDoRedo undoRedo, final ICommonUI commonUI,
                                final int firstPosition,
                                List<LineEntry> entriesUpdated,
                                final Map<Integer, LineEntry> entriesDeleted) {
    mUpdate = new UpdateCommand(undoRedo, commonUI, firstPosition, entriesUpdated.size(), entriesUpdated);
    mDelete = new DeleteCommand(commonUI, entriesDeleted);
  }

  /**
   * Execute the command.
   */
  public void execute() {
    Log.i(getClass().getName(), "execute");
    mDelete.execute();
    mUpdate.execute();
  }

  /**
   * Un-Execute the command.
   */
  public void unExecute() {
    Log.i(getClass().getName(), "unExecute");
    mUpdate.unExecute();
    mDelete.unExecute();
  }
}
