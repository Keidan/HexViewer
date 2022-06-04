package fr.ralala.hexviewer.ui.dialog;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AlertDialog;

import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.activities.MainActivity;
import fr.ralala.hexviewer.ui.utils.UIHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Dialog box used for saving the file.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class SaveDialog {
  private final MainActivity mActivity;
  private final String mTitle;

  public interface DialogPositiveClick {
    void onClick(AlertDialog dialog, EditText editText, TextInputLayout editTextLayout);
  }

  public SaveDialog(MainActivity activity, String title) {
    mActivity = activity;
    mTitle = title;
  }


  /**
   * Displays the dialog
   *
   * @param defaultValue  Default value.
   * @param positiveClick Listener called when clicking on the validation button.
   * @return AlertDialog
   */
  @SuppressLint("InflateParams")
  public AlertDialog show(String defaultValue, DialogPositiveClick positiveClick) {
    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
    builder.setCancelable(false)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(mTitle)
        .setPositiveButton(android.R.string.ok, null)
        .setNegativeButton(android.R.string.cancel, (dialog, whichButton) -> {
        });
    LayoutInflater factory = LayoutInflater.from(mActivity);
    builder.setView(factory.inflate(R.layout.content_dialog_save, null));
    AlertDialog dialog = builder.create();
    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN |
        WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    dialog.show();
    EditText et = dialog.findViewById(R.id.editText);
    TextInputLayout layout = dialog.findViewById(R.id.tilEditText);
    if (et != null && layout != null) {
      et.setText(defaultValue);
      et.addTextChangedListener(UIHelper.getResetLayoutWatcher(layout, false));
    }
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v ->
        positiveClick.onClick(dialog, et, layout));
    return dialog;
  }

}
