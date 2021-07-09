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
 * Settings fragments
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class SettingsFragmentListsPortrait extends AbstractSettingsFragment implements Preference.OnPreferenceClickListener {
  protected CheckBoxPreference mHexRowHeightAuto;
  protected Preference mHexRowHeight;
  protected Preference mHexFontSize;
  protected CheckBoxPreference mPlainRowHeightAuto;
  protected Preference mPlainRowHeight;
  protected Preference mPlainFontSize;

  public SettingsFragmentListsPortrait(AppCompatActivity owner) {
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
    setPreferencesFromResource(R.xml.preferences_lists_portrait, rootKey);

    mHexRowHeightAuto = findPreference(ApplicationCtx.CFG_PORTRAIT_HEX_ROW_HEIGHT_AUTO);
    mHexRowHeight = findPreference(ApplicationCtx.CFG_PORTRAIT_HEX_ROW_HEIGHT);
    mHexFontSize = findPreference(ApplicationCtx.CFG_PORTRAIT_HEX_FONT_SIZE);
    mPlainRowHeightAuto = findPreference(ApplicationCtx.CFG_PORTRAIT_PLAIN_ROW_HEIGHT_AUTO);
    mPlainRowHeight = findPreference(ApplicationCtx.CFG_PORTRAIT_PLAIN_ROW_HEIGHT);
    mPlainFontSize = findPreference(ApplicationCtx.CFG_PORTRAIT_PLAIN_FONT_SIZE);

    mHexRowHeightAuto.setOnPreferenceClickListener(this);
    mHexRowHeight.setOnPreferenceClickListener(this);
    mHexFontSize.setOnPreferenceClickListener(this);
    mPlainRowHeightAuto.setOnPreferenceClickListener(this);
    mPlainRowHeight.setOnPreferenceClickListener(this);
    mPlainFontSize.setOnPreferenceClickListener(this);

    mHexRowHeightAuto.setChecked(mApp.isHexRowHeightAutoPortrait());
    mPlainRowHeightAuto.setChecked(mApp.isPlainRowHeightAutoPortrait());
    mHexRowHeight.setEnabled(!mApp.isHexRowHeightAutoPortrait());
    mPlainRowHeight.setEnabled(!mApp.isPlainRowHeightAutoPortrait());
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
          mApp.getHexRowHeightPortrait(),
          MIN_HEX_ROW_HEIGHT,
          MAX_HEX_ROW_HEIGHT,
          mApp::setHexRowHeightPortrait);
    } else if (preference.equals(mHexFontSize)) {
      displayDialog(mHexFontSize.getTitle(),
          mApp.getHexFontSizePortrait(),
          MIN_HEX_FONT_SIZE,
          MAX_HEX_FONT_SIZE,
          mApp::setHexFontSizePortrait, true);
    } else if (preference.equals(mPlainRowHeightAuto)) {
      mPlainRowHeight.setEnabled(!mPlainRowHeightAuto.isChecked());
    } else if (preference.equals(mPlainRowHeight)) {
      displayDialog(mPlainRowHeight.getTitle(),
          mApp.getPlainRowHeightPortrait(),
          MIN_PLAIN_ROW_HEIGHT,
          MAX_PLAIN_ROW_HEIGHT,
          mApp::setPlainRowHeightPortrait);
    } else if (preference.equals(mPlainFontSize)) {
      displayDialog(mPlainFontSize.getTitle(),
          mApp.getPlainFontSizePortrait(),
          MIN_PLAIN_FONT_SIZE,
          MAX_PLAIN_FONT_SIZE,
          mApp::setPlainFontSizePortrait, true);
    }
    return false;
  }

}