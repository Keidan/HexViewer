package fr.ralala.hexviewer.ui.fragments;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Settings fragments for lists in landscape mode.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class SettingsFragmentListsLandscape extends AbstractSettingsFragment implements Preference.OnPreferenceClickListener {
  protected CheckBoxPreference mHexRowHeightAutoLandscape;
  protected Preference mHexRowHeightLandscape;
  protected Preference mHexFontSizeLandscape;
  protected CheckBoxPreference mHexRowHeightAutoLineNumbersLandscape;
  protected Preference mHexRowHeightLineNumbersLandscape;
  protected Preference mHexFontSizeLineNumbersLandscape;
  protected CheckBoxPreference mPlainRowHeightAutoLandscape;
  protected Preference mPlainRowHeightLandscape;
  protected Preference mPlainFontSizeLandscape;

  public SettingsFragmentListsLandscape(AppCompatActivity owner) {
    super(owner);
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
    setPreferencesFromResource(R.xml.preferences_lists_landscape, rootKey);

    CheckBoxPreference hexDisplayDataLandscape = findPreference(ApplicationCtx.CFG_LANDSCAPE_HEX_DISPLAY_DATA);
    CheckBoxPreference hexDisplayDataLineNumbersLandscape = findPreference(ApplicationCtx.CFG_LANDSCAPE_HEX_DISPLAY_DATA_LINE_NUMBERS);
    mHexRowHeightAutoLandscape = findPreference(ApplicationCtx.CFG_LANDSCAPE_HEX_ROW_HEIGHT_AUTO);
    mHexRowHeightLandscape = findPreference(ApplicationCtx.CFG_LANDSCAPE_HEX_ROW_HEIGHT);
    mHexFontSizeLandscape = findPreference(ApplicationCtx.CFG_LANDSCAPE_HEX_FONT_SIZE);
    mHexRowHeightAutoLineNumbersLandscape = findPreference(ApplicationCtx.CFG_LANDSCAPE_HEX_ROW_HEIGHT_AUTO_LINE_NUMBERS);
    mHexRowHeightLineNumbersLandscape = findPreference(ApplicationCtx.CFG_LANDSCAPE_HEX_ROW_HEIGHT_LINE_NUMBERS);
    mHexFontSizeLineNumbersLandscape = findPreference(ApplicationCtx.CFG_LANDSCAPE_HEX_FONT_SIZE_LINE_NUMBERS);
    mPlainRowHeightAutoLandscape = findPreference(ApplicationCtx.CFG_LANDSCAPE_PLAIN_ROW_HEIGHT_AUTO);
    mPlainRowHeightLandscape = findPreference(ApplicationCtx.CFG_LANDSCAPE_PLAIN_ROW_HEIGHT);
    mPlainFontSizeLandscape = findPreference(ApplicationCtx.CFG_LANDSCAPE_PLAIN_FONT_SIZE);


    mHexRowHeightAutoLandscape.setOnPreferenceClickListener(this);
    mHexRowHeightLandscape.setOnPreferenceClickListener(this);
    mHexFontSizeLandscape.setOnPreferenceClickListener(this);
    mHexRowHeightAutoLineNumbersLandscape.setOnPreferenceClickListener(this);
    mHexRowHeightLineNumbersLandscape.setOnPreferenceClickListener(this);
    mHexFontSizeLineNumbersLandscape.setOnPreferenceClickListener(this);
    mPlainRowHeightAutoLandscape.setOnPreferenceClickListener(this);
    mPlainRowHeightLandscape.setOnPreferenceClickListener(this);
    mPlainFontSizeLandscape.setOnPreferenceClickListener(this);

    if (hexDisplayDataLandscape != null)
      hexDisplayDataLandscape.setChecked(mApp.getListSettingsHexLandscape().isDisplayDataColumn());
    if (hexDisplayDataLineNumbersLandscape != null)
      hexDisplayDataLineNumbersLandscape.setChecked(mApp.getListSettingsHexLineNumbersLandscape().isDisplayDataColumn());

    mHexRowHeightAutoLandscape.setChecked(mApp.getListSettingsHexLandscape().isRowHeightAuto());
    mHexRowHeightLandscape.setEnabled(!mApp.getListSettingsHexLandscape().isRowHeightAuto());
    mHexRowHeightAutoLineNumbersLandscape.setChecked(mApp.getListSettingsHexLineNumbersLandscape().isRowHeightAuto());
    mHexRowHeightLineNumbersLandscape.setEnabled(!mApp.getListSettingsHexLineNumbersLandscape().isRowHeightAuto());

    mPlainRowHeightAutoLandscape.setChecked(mApp.getListSettingsPlainLandscape().isRowHeightAuto());
    mPlainRowHeightLandscape.setEnabled(!mApp.getListSettingsPlainLandscape().isRowHeightAuto());
  }


  /**
   * Called when a preference has been clicked.
   *
   * @param preference The preference that was clicked
   * @return {@code true} if the click was handled
   */
  @Override
  public boolean onPreferenceClick(Preference preference) {
    if (preference.equals(mHexRowHeightAutoLandscape)) {
      mHexRowHeightLandscape.setEnabled(!mHexRowHeightAutoLandscape.isChecked());
    } else if (preference.equals(mHexRowHeightLandscape)) {
      displayDialog(mHexRowHeightLandscape.getTitle(),
          mApp.getListSettingsHexLandscape().getRowHeight(),
          MIN_HEX_ROW_HEIGHT,
          MAX_HEX_ROW_HEIGHT,
          (n) -> mApp.getListSettingsHexLandscape().setRowHeight(n));
    } else if (preference.equals(mHexFontSizeLandscape)) {
      displayDialog(mHexFontSizeLandscape.getTitle(),
          mApp.getListSettingsHexLandscape().getFontSize(),
          MIN_HEX_FONT_SIZE,
          MAX_HEX_FONT_SIZE,
          (n) -> mApp.getListSettingsHexLandscape().setFontSize(n), true);
    } else if (preference.equals(mHexRowHeightAutoLineNumbersLandscape)) {
      mHexRowHeightLineNumbersLandscape.setEnabled(!mHexRowHeightAutoLineNumbersLandscape.isChecked());
    } else if (preference.equals(mHexRowHeightLineNumbersLandscape)) {
      displayDialog(mHexRowHeightLineNumbersLandscape.getTitle(),
          mApp.getListSettingsHexLineNumbersLandscape().getRowHeight(),
          MIN_HEX_ROW_HEIGHT,
          MAX_HEX_ROW_HEIGHT,
          (n) -> mApp.getListSettingsHexLineNumbersLandscape().setRowHeight(n));
    } else if (preference.equals(mHexFontSizeLineNumbersLandscape)) {
      displayDialog(mHexFontSizeLineNumbersLandscape.getTitle(),
          mApp.getListSettingsHexLineNumbersLandscape().getFontSize(),
          MIN_HEX_FONT_SIZE,
          MAX_HEX_FONT_SIZE,
          (n) -> mApp.getListSettingsHexLineNumbersLandscape().setFontSize(n), true);
    } else if (preference.equals(mPlainRowHeightAutoLandscape)) {
      mPlainRowHeightLandscape.setEnabled(!mPlainRowHeightAutoLandscape.isChecked());
    } else if (preference.equals(mPlainRowHeightLandscape)) {
      displayDialog(mPlainRowHeightLandscape.getTitle(),
          mApp.getListSettingsPlainLandscape().getRowHeight(),
          MIN_PLAIN_ROW_HEIGHT,
          MAX_PLAIN_ROW_HEIGHT,
          (n) -> mApp.getListSettingsPlainLandscape().setRowHeight(n));
    } else if (preference.equals(mPlainFontSizeLandscape)) {
      displayDialog(mPlainFontSizeLandscape.getTitle(),
          mApp.getListSettingsPlainLandscape().getFontSize(),
          MIN_PLAIN_FONT_SIZE,
          MAX_PLAIN_FONT_SIZE,
          (n) -> mApp.getListSettingsPlainLandscape().setFontSize(n), true);
    }
    return false;
  }
}