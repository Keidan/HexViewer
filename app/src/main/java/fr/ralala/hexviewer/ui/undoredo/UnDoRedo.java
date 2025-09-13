package fr.ralala.hexviewer.ui.undoredo;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.lines.LineEntry;
import fr.ralala.hexviewer.ui.activities.ICommonUI;
import fr.ralala.hexviewer.ui.undoredo.commands.DeleteCommand;
import fr.ralala.hexviewer.ui.undoredo.commands.UpdateAndDeleteCommand;
import fr.ralala.hexviewer.ui.undoredo.commands.UpdateCommand;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Undo Redo Manager
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
public class UnDoRedo {
  private static final int CONTROL_UNDO = 0;
  private static final int CONTROL_REDO = 1;
  private final Context mContext;
  private final ICommonUI mCommonUI;
  private final Control[] mControls;
  private final Deque<ICommand> mUndo;
  private final Deque<ICommand> mRedo;
  private int mReferenceIndex;

  public UnDoRedo(Context context, ICommonUI commonUI) {
    mContext = context;
    mCommonUI = commonUI;
    mControls = new Control[2];
    mUndo = new ArrayDeque<>();
    mRedo = new ArrayDeque<>();
  }

  /**
   * Sets the controls.
   *
   * @param containerUndo FrameLayout
   * @param viewUndo      ImageView
   * @param containerRedo FrameLayout
   * @param viewRedo      ImageView
   */
  public void setControls(final FrameLayout containerUndo, final ImageView viewUndo, final FrameLayout containerRedo, final ImageView viewRedo) {
    mControls[CONTROL_UNDO] = new Control();
    mControls[CONTROL_UNDO].container = containerUndo;
    mControls[CONTROL_UNDO].img = viewUndo;
    mControls[CONTROL_UNDO].disable = R.drawable.ic_undo_disabled;
    mControls[CONTROL_UNDO].enable = R.drawable.ic_undo;
    mControls[CONTROL_REDO] = new Control();
    mControls[CONTROL_REDO].container = containerRedo;
    mControls[CONTROL_REDO].img = viewRedo;
    mControls[CONTROL_REDO].disable = R.drawable.ic_redo_disabled;
    mControls[CONTROL_REDO].enable = R.drawable.ic_redo;
  }

  /**
   * Tests if a change is detected.
   *
   * @return boolean
   */
  public boolean isChanged() {
    return mReferenceIndex != mUndo.size();
  }

  /**
   * Updates change index.
   */
  public void refreshChange() {
    mReferenceIndex = mUndo.size();
  }

  /**
   * Updates command.
   *
   * @param firstPosition The first position index.
   * @param refNbLines    The reference number of lines.
   * @param entries       The entries.
   * @return The command.
   */
  public ICommand insertInUnDoRedoForUpdate(final int firstPosition, final int refNbLines, List<LineEntry> entries) {
    ICommand cmd = new UpdateCommand(this, mCommonUI, firstPosition, refNbLines, entries);
    mUndo.push(cmd);
    manageControl(mControls[CONTROL_UNDO], true);
    manageControl(mControls[CONTROL_REDO], false);
    mRedo.clear();

    mCommonUI.refreshTitle();
    return cmd;
  }

  /**
   * Updates and delete command.
   *
   * @param firstPosition  The first position index.
   * @param entriesUpdated Entries to be updated.
   * @param entriesDeleted Entries to be deleted.
   * @return The command.
   */
  public ICommand insertInUnDoRedoForUpdateAndDelete(final int firstPosition,
                                                     List<LineEntry> entriesUpdated,
                                                     final Map<Integer, LineEntry> entriesDeleted) {
    ICommand cmd = new UpdateAndDeleteCommand(this, mCommonUI, firstPosition, entriesUpdated, entriesDeleted);
    mUndo.push(cmd);
    manageControl(mControls[CONTROL_UNDO], true);
    manageControl(mControls[CONTROL_REDO], false);
    mRedo.clear();

    mCommonUI.refreshTitle();
    return cmd;
  }

  /**
   * Inserts delete command.
   *
   * @param entries The entries.
   * @return The command.
   */
  public ICommand insertInUnDoRedoForDelete(final Map<Integer, LineEntry> entries) {
    ICommand cmd = new DeleteCommand(mCommonUI, entries);
    mUndo.push(cmd);
    manageControl(mControls[CONTROL_UNDO], true);
    manageControl(mControls[CONTROL_REDO], false);
    mRedo.clear();

    mCommonUI.refreshTitle();
    return cmd;
  }

  /**
   * Undo action
   */
  public void undo() {
    if (!mUndo.isEmpty()) {
      ICommand command = mUndo.pop();
      mRedo.push(command);
      command.unExecute();
      manageControl(mControls[CONTROL_REDO], true);
    }
    mCommonUI.refreshTitle();
    manageControl(mControls[CONTROL_UNDO], !mUndo.isEmpty());
    if (!isChanged())
      mCommonUI.getPayloadHex().resetUpdateStatus();
  }

  /**
   * Redo action.
   */
  public void redo() {
    if (!mRedo.isEmpty()) {
      ICommand command = mRedo.pop();
      mUndo.push(command);
      command.execute();
      manageControl(mControls[CONTROL_UNDO], true);
    }
    mCommonUI.refreshTitle();
    manageControl(mControls[CONTROL_REDO], !mRedo.isEmpty());
    if (!isChanged())
      mCommonUI.getPayloadHex().resetUpdateStatus();
  }

  /**
   * Clears the undo/redo stacks.
   */
  public void clear() {
    for (Control ctrl : mControls)
      manageControl(ctrl, false);
    mUndo.clear();
    mRedo.clear();
    mReferenceIndex = 0;
    mCommonUI.refreshTitle();
  }

  /**
   * Manages control state.
   *
   * @param control The control.
   * @param enabled Enabled ?
   */
  private void manageControl(final Control control, final boolean enabled) {
    if (control != null && control.img != null) {
      if (control.container != null)
        control.container.setEnabled(enabled);
      control.img.setImageDrawable(ContextCompat.getDrawable(mContext, enabled ? control.enable : control.disable));
      control.img.setEnabled(enabled);
    }
  }

  private static class Control {
    private FrameLayout container;
    private ImageView img;
    private int enable;
    private int disable;
  }
}
