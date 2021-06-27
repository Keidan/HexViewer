package fr.ralala.hexviewer.ui.undoredo;

import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.Map;
import java.util.Stack;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.activities.MainActivity;
import fr.ralala.hexviewer.ui.adapters.HexTextArrayAdapter;
import fr.ralala.hexviewer.utils.LineEntry;

import static fr.ralala.hexviewer.ui.adapters.SearchableListArrayAdapter.FilterData;
/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Undo Redo Manager
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class UndoRedoManager {
  private static final int CONTROL_UNDO = 0;
  private static final int CONTROL_REDO = 1;
  private final MainActivity mActivity;
  private final Control[] mControls;
  private final Stack<ICommand> mUndo;
  private final Stack<ICommand> mRedo;

  public UndoRedoManager(MainActivity activity) {
    mActivity = activity;
    mControls = new Control[2];
    mUndo = new Stack<>();
    mRedo = new Stack<>();
  }

  /**
   * Sets the controls.
   *
   * @param containerUndo FrameLayout
   * @param viewUndo ImageView
   * @param containerRedo FrameLayout
   * @param viewRedo ImageView
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
   * @return boolean
   */
  public boolean isChanged() {
    return !mUndo.empty();
  }

  /**
   * Inserts update command.
   *
   * @param le Line entry.
   */
  public void insertInUnDoRedoForUpdate() {
    /*ICommand cmd = new UpdateCommand(le);
    mUndo.push(cmd);
    manageControl(mControls[CONTROL_UNDO], true);
    manageControl(mControls[CONTROL_REDO], false);
    mRedo.clear();*/
  }

  /**
   * Inserts delete command.
   *
   * @param adapter HexTextArrayAdapter.
   * @param entries The entries.
   * @return The command.
   */
  public ICommand insertInUnDoRedoForDelete(final HexTextArrayAdapter adapter, final Map<Integer, FilterData<LineEntry>> entries) {
    ICommand cmd = new DeleteCommand(adapter, entries);
    mUndo.push(cmd);
    manageControl(mControls[CONTROL_UNDO], true);
    manageControl(mControls[CONTROL_REDO], false);
    mRedo.clear();
    return cmd;
  }

  /**
   * Undo action
   */
  public void undo() {
    if (!mUndo.isEmpty()) {
      ICommand command = mUndo.pop();
      command.unExecute();
      mRedo.push(command);
      manageControl(mControls[CONTROL_REDO], true);
    }
    manageControl(mControls[CONTROL_UNDO], !mUndo.isEmpty());
  }

  /**
   * Redo action.
   */
  public void redo() {
    if (!mRedo.isEmpty()) {
      ICommand command = mRedo.pop();
      command.execute();
      mUndo.push(command);
      manageControl(mControls[CONTROL_UNDO], true);
    }
    manageControl(mControls[CONTROL_REDO], !mRedo.isEmpty());
  }

  /**
   * Clears the undo/redo stacks.
   */
  public void clear() {
    for (Control ctrl : mControls)
      manageControl(ctrl, false);
    mUndo.clear();
    mRedo.clear();
  }

  /**
   * Manages control state.
   *
   * @param control The control.
   * @param enabled Enabled ?
   */
  private void manageControl(final Control control, final boolean enabled) {
    if (control != null && control.img != null) {
      if(control.container != null)
        control.container.setEnabled(enabled);
      control.img.setImageDrawable(ContextCompat.getDrawable(mActivity, enabled ? control.enable : control.disable));
      control.img.setEnabled(enabled);
    }
  }

  private static class Control {
    FrameLayout container;
    ImageView img;
    @DrawableRes
    int enable;
    @DrawableRes
    int disable;
  }
}
