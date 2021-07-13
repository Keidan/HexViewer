package fr.ralala.hexviewer.ui.popup;

import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * As the checkbox used in the popup menu is quite particular, this class avoids burdening the code of the MainActivity class.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class PopupCheckboxHelper {
  private final LinearLayout mContainer;
  private final TextView mTextView;
  private final CheckBox mCheckbox;
  private final List<Integer> mIds;

  public PopupCheckboxHelper(View root, int idContainer, int idTextView, int idCheckbox) {
    mIds = new ArrayList<>();
    mIds.add(idContainer);
    mIds.add(idTextView);
    mIds.add(idCheckbox);
    mContainer = root.findViewById(idContainer);
    mTextView = root.findViewById(idTextView);
    mCheckbox = root.findViewById(idCheckbox);
  }

  /**
   * Test if the id is in the list.
   *
   * @param id                       The identifier to be tested.
   * @param containerAndTextViewOnly Container and text view only ?
   * @return boolean
   */
  public boolean containsId(int id, boolean containerAndTextViewOnly) {
    if (!containerAndTextViewOnly)
      return mIds.contains(id);
    return mIds.get(0) == id || mIds.get(1) == id;
  }

  /**
   * Toggle the check state.
   */
  public void toggleCheck() {
    if (mCheckbox != null)
      mCheckbox.setChecked(!mCheckbox.isChecked());
  }

  /**
   * Toggle the check state.
   *
   * @param checked The new state.
   */
  public void setChecked(boolean checked) {
    if (mCheckbox != null)
      mCheckbox.setChecked(checked);
  }

  /**
   * Sets the on click listener
   *
   * @param listener OnClickListener
   */
  public void setOnClickListener(View.OnClickListener listener) {
    if (mCheckbox != null)
      mCheckbox.setOnClickListener(listener);
    if (mTextView != null)
      mTextView.setOnClickListener(listener);
    if (mContainer != null)
      mContainer.setOnClickListener(listener);
  }

  /**
   * Sets whether the checkbox is enabled.
   *
   * @param enabled If true then the item will be invokable; if false it is won't be invokable.
   * @return checked
   */
  public boolean setEnable(final boolean enabled) {
    boolean checked = false;
    if (mCheckbox != null) {
      checked = mCheckbox.isChecked();
      mCheckbox.setEnabled(enabled);
    }
    if (mTextView != null) {
      mTextView.setEnabled(enabled);
    }
    if (mContainer != null) {
      mContainer.setEnabled(enabled);
    }
    return checked;
  }

  /**
   * Tests if the checkbox is checked.
   *
   * @return boolean
   */
  public boolean isChecked() {
    return mCheckbox != null && mCheckbox.isChecked();
  }
}
