package fr.ralala.hexviewer.ui.undoredo;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Undo Redo command
 * </p>
 *
 * @author Keidan
 *
 * License: GPLv3
 * <p>
 * ******************************************************************************
 */
public interface ICommand {
  /**
   * Execute the command.
   */
  void execute();

  /**
   * Un-Execute the command.
   */
  void unExecute();
}
