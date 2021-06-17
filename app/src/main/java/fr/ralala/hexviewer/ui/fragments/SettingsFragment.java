package fr.ralala.hexviewer.ui.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.BuildConfig;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.utils.UIHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Settings fragments
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
  private static final int MIN_ABBREVIATE_PORTRAIT = 1;
  private static final int MAX_ABBREVIATE_PORTRAIT = 25;
  private static final int MIN_ABBREVIATE_LANDSCAPE = 4;
  private static final int MAX_ABBREVIATE_LANDSCAPE = 80;
  private static final int MIN_HEX_ROW_HEIGHT = 100;
  private static final int MAX_HEX_ROW_HEIGHT = 1000;
  private static final int MIN_HEX_FONT_SIZE = 1;
  private static final int MAX_HEX_FONT_SIZE = 50;
  private static final int MIN_PLAIN_ROW_HEIGHT = 100;
  private static final int MAX_PLAIN_ROW_HEIGHT = 1000;
  private static final int MIN_PLAIN_FONT_SIZE = 1;
  private static final int MAX_PLAIN_FONT_SIZE = 50;
  private final Activity mActivity;
  private final ApplicationCtx mApp;
  protected Preference mAbbreviatePortrait;
  protected Preference mAbbreviateLandscape;
  protected CheckBoxPreference mHexRowHeightAuto;
  protected Preference mHexRowHeight;
  protected Preference mHexFontSize;
  protected CheckBoxPreference mPlainRowHeightAuto;
  protected Preference mPlainRowHeight;
  protected Preference mPlainFontSize;
  protected Preference mLicense;
  protected Preference mVersion;

  public SettingsFragment(Activity owner) {
    mActivity = owner;
    mApp = (ApplicationCtx) mActivity.getApplicationContext();
  }

  /**
   * Called during {@link #onCreate(Bundle)} to supply the preferences for this fragment.
   * Subclasses are expected to call {@link #setPreferenceScreen(PreferenceScreen)} either
   * directly or via helper methods such as {@link #addPreferencesFromResource(int)}.
   *
   * @param savedInstanceState If the fragment is being re-created from a previous saved state,
   *                           this is the state.
   * @param rootKey            If non-null, this preference fragment should be rooted at the
   *                           {@link PreferenceScreen} with this key.
   */
  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.preferences, rootKey);

    mAbbreviatePortrait = findPreference(ApplicationCtx.CFG_ABBREVIATE_PORTRAIT);
    mAbbreviateLandscape = findPreference(ApplicationCtx.CFG_ABBREVIATE_LANDSCAPE);
    mHexRowHeightAuto = findPreference(ApplicationCtx.CFG_HEX_ROW_HEIGHT_AUTO);
    mHexRowHeight = findPreference(ApplicationCtx.CFG_HEX_ROW_HEIGHT);
    mHexFontSize = findPreference(ApplicationCtx.CFG_HEX_FONT_SIZE);
    mPlainRowHeightAuto = findPreference(ApplicationCtx.CFG_PLAIN_ROW_HEIGHT_AUTO);
    mPlainRowHeight = findPreference(ApplicationCtx.CFG_PLAIN_ROW_HEIGHT);
    mPlainFontSize = findPreference(ApplicationCtx.CFG_PLAIN_FONT_SIZE);
    mLicense = findPreference(ApplicationCtx.CFG_LICENSE);
    mVersion = findPreference(ApplicationCtx.CFG_VERSION);

    mAbbreviatePortrait.setOnPreferenceClickListener(this);
    mAbbreviateLandscape.setOnPreferenceClickListener(this);
    mHexRowHeightAuto.setOnPreferenceClickListener(this);
    mHexRowHeight.setOnPreferenceClickListener(this);
    mHexFontSize.setOnPreferenceClickListener(this);
    mPlainRowHeightAuto.setOnPreferenceClickListener(this);
    mPlainRowHeight.setOnPreferenceClickListener(this);
    mPlainFontSize.setOnPreferenceClickListener(this);
    mLicense.setOnPreferenceClickListener(this);
    mVersion.setOnPreferenceClickListener(this);

    mHexRowHeightAuto.setChecked(mApp.isHexRowHeightAuto());
    mPlainRowHeightAuto.setChecked(mApp.isPlainRowHeightAuto());
    mHexRowHeight.setEnabled(!mApp.isHexRowHeightAuto());
    mPlainRowHeight.setEnabled(!mApp.isPlainRowHeightAuto());
    mVersion.setSummary(BuildConfig.VERSION_NAME);
  }


  /**
   * Called when a preference has been clicked.
   *
   * @param preference The preference that was clicked
   * @return {@code true} if the click was handled
   */
  @Override
  public boolean onPreferenceClick(Preference preference) {
    if (preference.equals(mAbbreviatePortrait)) {
      displayDialog(mAbbreviatePortrait.getTitle(),
          mApp.getAbbreviatePortrait(),
          MIN_ABBREVIATE_PORTRAIT,
          MAX_ABBREVIATE_PORTRAIT,
          mApp::setAbbreviatePortrait);
    } else if (preference.equals(mAbbreviateLandscape)) {
      displayDialog(mAbbreviateLandscape.getTitle(),
          mApp.getAbbreviateLandscape(),
          MIN_ABBREVIATE_LANDSCAPE,
          MAX_ABBREVIATE_LANDSCAPE,
          mApp::setAbbreviateLandscape);
    } else if (preference.equals(mHexRowHeightAuto)) {
      mHexRowHeight.setEnabled(!mHexRowHeightAuto.isChecked());
    } else if (preference.equals(mHexRowHeight)) {
      displayDialog(mHexRowHeight.getTitle(),
          mApp.getHexRowHeight(),
          MIN_HEX_ROW_HEIGHT,
          MAX_HEX_ROW_HEIGHT,
          mApp::setHexRowHeight);
    } else if (preference.equals(mHexFontSize)) {
      displayDialog(mHexFontSize.getTitle(),
          mApp.getHexFontSize(),
          MIN_HEX_FONT_SIZE,
          MAX_HEX_FONT_SIZE,
          mApp::setHexFontSize, true);
    } else if (preference.equals(mPlainRowHeightAuto)) {
      mPlainRowHeight.setEnabled(!mPlainRowHeightAuto.isChecked());
    } else if (preference.equals(mPlainRowHeight)) {
      displayDialog(mPlainRowHeight.getTitle(),
          mApp.getPlainRowHeight(),
          MIN_PLAIN_ROW_HEIGHT,
          MAX_PLAIN_ROW_HEIGHT,
          mApp::setPlainRowHeight);
    } else if (preference.equals(mPlainFontSize)) {
      displayDialog(mPlainFontSize.getTitle(),
          mApp.getPlainFontSize(),
          MIN_PLAIN_FONT_SIZE,
          MAX_PLAIN_FONT_SIZE,
          mApp::setPlainFontSize, true);
    } else if (preference.equals(mLicense)) {
      Intent browserIntent = new Intent(Intent.ACTION_VIEW, getProjectUri("license.txt"));
      startActivity(browserIntent);
    } else if (preference.equals(mVersion)) {
      Intent browserIntent = new Intent(Intent.ACTION_VIEW, getProjectUri(null));
      startActivity(browserIntent);
    }
    return false;
  }

  /**
   * Returns the project Uri.
   *
   * @param file We want the url based on a file?
   * @return The Uri
   */
  private Uri getProjectUri(final String file) {
    if (file == null)
      return Uri.parse("https://github.com/Keidan/HexViewer/tree/v" + BuildConfig.VERSION_NAME);
    return Uri.parse("https://github.com/Keidan/HexViewer/blob/v" + BuildConfig.VERSION_NAME + "/" + file);
  }

  /* ----------------------------- */
  private interface InputValidated<T> {
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
  private void displayDialog(CharSequence title, int defaultValue, int minValue, int maxValue, InputValidated<Integer> iv) {
    displayDialog(title, defaultValue, minValue, maxValue, (v) -> iv.onValidated(v.intValue()), false);
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
  private void displayDialog(CharSequence title, float defaultValue, float minValue, float maxValue, InputValidated<Float> iv, boolean decimal) {
    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
    builder.setCancelable(false)
        .setTitle(title)
        .setPositiveButton(android.R.string.yes, null)
        .setNegativeButton(android.R.string.no, (dialog, whichButton) -> {
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
      if(decimal) {
        maxLen = 5;
        inputType |= InputType.TYPE_NUMBER_FLAG_DECIMAL;
        def = String.valueOf(defaultValue);
      } else {
        maxLen = 3;
        def = String.valueOf((int)defaultValue);
      }
      et.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLen)});
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
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((v) -> {
      if (et != null && validInput(et, defaultValue, minValue, maxValue, iv, decimal)) {
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
        dialog.dismiss();
      }
    });
    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener((v) -> {
      if(et != null)
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
      dialog.dismiss();
    });
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
  private boolean validInput(EditText et, float defaultValue, float minValue, float maxValue, InputValidated<Float> iv, boolean decimal) {
    try {
      Editable s = et.getText();
      float nb = Float.parseFloat(s.toString());
      if (s.length() == 0) {
        et.setText(String.valueOf(!decimal ? (int)minValue : minValue));
        et.selectAll();
        return false;
      } else {
        if (nb < minValue) {
          UIHelper.shakeError(et, mActivity.getString(R.string.error_less_than) + ": " + minValue);
          et.setText(String.valueOf(!decimal ? (int)minValue : minValue));
          et.selectAll();
          return false;
        } else if (nb > maxValue) {
          UIHelper.shakeError(et, mActivity.getString(R.string.error_greater_than) + ": " + maxValue);
          et.setText(String.valueOf(!decimal ? (int)maxValue : maxValue));
          et.selectAll();
          return false;
        } else
          et.setError(null);
        iv.onValidated(nb);
        return true;
      }
    } catch (Exception ex) {
      UIHelper.shakeError(et, ex.getMessage());
      et.setText(String.valueOf(!decimal ? (int)defaultValue : defaultValue));
      et.selectAll();
      return false;
    }
  }
}