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
  private final AlertDialog mDialog;

  public interface DialogPositiveClick {
    void onClick(AlertDialog dialog, EditText editText, TextInputLayout editTextLayout);
  }

  @SuppressLint("InflateParams")
  public SaveDialog(MainActivity activity, String title) {
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder.setCancelable(false)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(title)
        .setPositiveButton(android.R.string.yes, null)
        .setNegativeButton(android.R.string.no, (dialog, whichButton) -> {
        });
    LayoutInflater factory = LayoutInflater.from(activity);
    builder.setView(factory.inflate(R.layout.content_dialog_save, null));
    mDialog = builder.create();
  }


  /**
   * Displays the dialog
   *
   * @param defaultValue  Default value.
   * @param positiveClick Listener called when clicking on the validation button.
   * @return AlertDialog
   */
  public AlertDialog show(String defaultValue, DialogPositiveClick positiveClick) {
    mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN |
        WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    mDialog.show();
    EditText et = mDialog.findViewById(R.id.editText);
    TextInputLayout layout = mDialog.findViewById(R.id.tilEditText);
    if (et != null && layout != null) {
      et.setText(defaultValue);
      et.addTextChangedListener(UIHelper.getResetLayoutWatcher(layout, false));
    }
    mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((v) ->
        positiveClick.onClick(mDialog, et, layout));
    return mDialog;
  }

}
