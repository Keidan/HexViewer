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
 * ******************************************************************************
 */
public class SettingsFragmentListsLandscape extends AbstractSettingsFragment implements Preference.OnPreferenceClickListener {
  protected CheckBoxPreference mHexRowHeightAuto;
  protected Preference mHexRowHeight;
  protected Preference mHexFontSize;
  protected CheckBoxPreference mPlainRowHeightAuto;
  protected Preference mPlainRowHeight;
  protected Preference mPlainFontSize;

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

    mHexRowHeightAuto = findPreference(ApplicationCtx.CFG_LANDSCAPE_HEX_ROW_HEIGHT_AUTO);
    mHexRowHeight = findPreference(ApplicationCtx.CFG_LANDSCAPE_HEX_ROW_HEIGHT);
    mHexFontSize = findPreference(ApplicationCtx.CFG_LANDSCAPE_HEX_FONT_SIZE);
    mPlainRowHeightAuto = findPreference(ApplicationCtx.CFG_LANDSCAPE_PLAIN_ROW_HEIGHT_AUTO);
    mPlainRowHeight = findPreference(ApplicationCtx.CFG_LANDSCAPE_PLAIN_ROW_HEIGHT);
    mPlainFontSize = findPreference(ApplicationCtx.CFG_LANDSCAPE_PLAIN_FONT_SIZE);

    mHexRowHeightAuto.setOnPreferenceClickListener(this);
    mHexRowHeight.setOnPreferenceClickListener(this);
    mHexFontSize.setOnPreferenceClickListener(this);
    mPlainRowHeightAuto.setOnPreferenceClickListener(this);
    mPlainRowHeight.setOnPreferenceClickListener(this);
    mPlainFontSize.setOnPreferenceClickListener(this);

    mHexRowHeightAuto.setChecked(mApp.isHexRowHeightAutoLandscape());
    mPlainRowHeightAuto.setChecked(mApp.isPlainRowHeightAutoLandscape());
    mHexRowHeight.setEnabled(!mApp.isHexRowHeightAutoLandscape());
    mPlainRowHeight.setEnabled(!mApp.isPlainRowHeightAutoLandscape());
  }


  /**
   * Called when a preference has been clicked.
   *
   * @param preference The preference that was clicked
   * @return {@code true} if the click was handled
   */
  @Override
  public boolean onPreferenceClick(Preference preference) {
    if (preference.equals(mHexRowHeightAuto)) {
      mHexRowHeight.setEnabled(!mHexRowHeightAuto.isChecked());
    } else if (preference.equals(mHexRowHeight)) {
      displayDialog(mHexRowHeight.getTitle(),
          mApp.getHexRowHeightLandscape(),
          MIN_HEX_ROW_HEIGHT,
          MAX_HEX_ROW_HEIGHT,
          mApp::setHexRowHeightLandscape);
    } else if (preference.equals(mHexFontSize)) {
      displayDialog(mHexFontSize.getTitle(),
          mApp.getHexFontSizeLandscape(),
          MIN_HEX_FONT_SIZE,
          MAX_HEX_FONT_SIZE,
          mApp::setHexFontSizeLandscape, true);
    } else if (preference.equals(mPlainRowHeightAuto)) {
      mPlainRowHeight.setEnabled(!mPlainRowHeightAuto.isChecked());
    } else if (preference.equals(mPlainRowHeight)) {
      displayDialog(mPlainRowHeight.getTitle(),
          mApp.getPlainRowHeightLandscape(),
          MIN_PLAIN_ROW_HEIGHT,
          MAX_PLAIN_ROW_HEIGHT,
          mApp::setPlainRowHeightLandscape);
    } else if (preference.equals(mPlainFontSize)) {
      displayDialog(mPlainFontSize.getTitle(),
          mApp.getPlainFontSizeLandscape(),
          MIN_PLAIN_FONT_SIZE,
          MAX_PLAIN_FONT_SIZE,
          mApp::setPlainFontSizeLandscape, true);
    }
    return false;
  }
}