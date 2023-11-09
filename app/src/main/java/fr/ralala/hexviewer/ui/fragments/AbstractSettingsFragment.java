package fr.ralala.hexviewer.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.utils.UIHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Abstract settings fragments
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public abstract class AbstractSettingsFragment extends PreferenceFragmentCompat {
  protected static final int MIN_HEX_ROW_HEIGHT = 10;
  protected static final int MAX_HEX_ROW_HEIGHT = 1000;
  protected static final int MIN_HEX_FONT_SIZE = 1;
  protected static final int MAX_HEX_FONT_SIZE = 100;
  protected static final int MIN_PLAIN_ROW_HEIGHT = 10;
  protected static final int MAX_PLAIN_ROW_HEIGHT = 1000;
  protected static final int MIN_PLAIN_FONT_SIZE = 1;
  protected static final int MAX_PLAIN_FONT_SIZE = 100;
  protected final AppCompatActivity mActivity;
  protected final ApplicationCtx mApp;

  protected AbstractSettingsFragment(AppCompatActivity owner) {
    mActivity = owner;
    mApp = (ApplicationCtx)owner.getApplicationContext();
  }

  /* ----------------------------- */
  protected interface InputValidated<T> {
    void onValidated(T n);
  }

  /**
   * Displays the input dialog box.
   *
   * @param title        Dialog title.
   * @param defaultValue Default value.
   * @param minValue     Min value.
   * @param maxValue     Max value.
   * @param iv           Callback which will be called if the entry is valid.
   */
  @SuppressLint("InflateParams")
  protected void displayDialog(CharSequence title, int defaultValue, int minValue, int maxValue, InputValidated<Integer> iv) {
    displayDialog(title, defaultValue, minValue, maxValue, v -> iv.onValidated(v.intValue()), false);
  }

  /**
   * Displays the input dialog box.
   *
   * @param title        Dialog title.
   * @param defaultValue Default value.
   * @param minValue     Min value.
   * @param maxValue     Max value.
   * @param iv           Callback which will be called if the entry is valid.
   */
  @SuppressLint("InflateParams")
  protected void displayDialog(CharSequence title, float defaultValue, float minValue, float maxValue, InputValidated<Float> iv, boolean decimal) {
    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
    builder.setCancelable(false)
        .setTitle(title)
        .setPositiveButton(android.R.string.ok, null)
        .setNegativeButton(android.R.string.cancel, (dialog, whichButton) -> {
        });
    LayoutInflater factory = LayoutInflater.from(mActivity);
    builder.setView(factory.inflate(R.layout.content_dialog_pref_input, null));
    final AlertDialog dialog = builder.create();
    dialog.show();
    EditText et = dialog.findViewById(R.id.editText);
    if (et != null) {
      int inputType = InputType.TYPE_CLASS_NUMBER;
      String def;
      int maxLen;
      if (decimal) {
        maxLen = 5;
        inputType |= InputType.TYPE_NUMBER_FLAG_DECIMAL;
        def = String.valueOf(defaultValue);
      } else {
        maxLen = 3;
        def = String.valueOf((int) defaultValue);
      }
      et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLen)});
      et.setInputType(inputType);
      et.setText(def);
      et.requestFocus();
      Editable text = et.getText();
      if (text.length() > 0) {
        text.replace(0, 1, text.subSequence(0, 1), 0, 1);
        et.selectAll();
      }
    }
    final InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
      if (et != null && validInput(et, defaultValue, minValue, maxValue, iv, decimal)) {
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
        dialog.dismiss();
      }
    });
    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(v -> {
      if (et != null)
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
      dialog.dismiss();
    });
  }

  private boolean validateInputValue(EditText et, InputValidated<Float> iv, float nb,
                                     float minValue, float maxValue, boolean decimal) {
    boolean result = false;
    if (nb < minValue) {
      UIHelper.shakeError(et, mActivity.getString(R.string.error_less_than) + ": " + minValue);
      et.setText(String.valueOf(!decimal ? (int) minValue : minValue));
      et.selectAll();
    } else if (nb > maxValue) {
      UIHelper.shakeError(et, mActivity.getString(R.string.error_greater_than) + ": " + maxValue);
      et.setText(String.valueOf(!decimal ? (int) maxValue : maxValue));
      et.selectAll();
    } else {
      result = true;
      et.setError(null);
      iv.onValidated(nb);
    }
    return result;
  }
  /**
   * Validation of the input.
   *
   * @param et           EditText
   * @param defaultValue Default value.
   * @param minValue     Min value.
   * @param maxValue     Max value.
   * @param iv           Callback
   * @return False on error.
   */
  protected boolean validInput(EditText et, float defaultValue, float minValue, float maxValue,
                               InputValidated<Float> iv, boolean decimal) {
    boolean result = false;
    try {
      Editable s = et.getText();
      float nb = -1;
      if (s.length() != 0) {
        nb = Float.parseFloat(s.toString());
      }
      result = validateInputValue(et, iv, nb, minValue, maxValue, decimal);
    } catch (Exception ex) {
      UIHelper.shakeError(et, ex.getMessage());
      et.setText(String.valueOf(!decimal ? (int) defaultValue : defaultValue));
      et.selectAll();
    }
    return result;
  }
}