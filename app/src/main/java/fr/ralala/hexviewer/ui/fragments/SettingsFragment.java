package fr.ralala.hexviewer.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.BuildConfig;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.activities.settings.SettingsActivity;
import fr.ralala.hexviewer.ui.activities.settings.SettingsListsLandscapeActivity;
import fr.ralala.hexviewer.ui.activities.settings.SettingsListsPortraitActivity;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Settings fragments
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class SettingsFragment extends AbstractSettingsFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
  protected Preference mAbbreviatePortrait;
  protected Preference mAbbreviateLandscape;
  protected Preference mSettingsListsPortrait;
  protected Preference mSettingsListsLandscape;
  protected Preference mLicense;
  protected Preference mVersion;
  private ListPreference mLanguage;

  public SettingsFragment(AppCompatActivity owner) {
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
    setPreferencesFromResource(R.xml.preferences, rootKey);

    mAbbreviatePortrait = findPreference(ApplicationCtx.CFG_ABBREVIATE_PORTRAIT);
    mAbbreviateLandscape = findPreference(ApplicationCtx.CFG_ABBREVIATE_LANDSCAPE);
    mSettingsListsPortrait = findPreference(ApplicationCtx.CFG_LISTS_PORTRAIT);
    mSettingsListsLandscape = findPreference(ApplicationCtx.CFG_LISTS_LANDSCAPE);
    mLicense = findPreference(ApplicationCtx.CFG_LICENSE);
    mVersion = findPreference(ApplicationCtx.CFG_VERSION);
    mLanguage = findPreference(ApplicationCtx.CFG_LANGUAGE);

    mAbbreviatePortrait.setOnPreferenceClickListener(this);
    mAbbreviateLandscape.setOnPreferenceClickListener(this);
    mSettingsListsPortrait.setOnPreferenceClickListener(this);
    mSettingsListsLandscape.setOnPreferenceClickListener(this);
    mLicense.setOnPreferenceClickListener(this);
    mVersion.setOnPreferenceClickListener(this);
    mLanguage.setOnPreferenceChangeListener(this);

    mVersion.setSummary(BuildConfig.VERSION_NAME);

    mLanguage.setDefaultValue(mApp.getApplicationLanguage(getContext()));
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
    } else if (preference.equals(mLicense)) {
      Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Keidan/HexViewer/blob/master/license.txt"));
      startActivity(browserIntent);
    } else if (preference.equals(mVersion)) {
      Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Keidan/HexViewer"));
      startActivity(browserIntent);
    } else if (preference.equals(mSettingsListsPortrait)) {
      SettingsListsPortraitActivity.startActivity(mActivity);
    } else if (preference.equals(mSettingsListsLandscape)) {
      SettingsListsLandscapeActivity.startActivity(mActivity);
    }
    return false;
  }

  /**
   * Called when a preference has been changed.
   *
   * @param preference The preference that was clicked
   * @param newValue   The new value.
   * @return {@code true} if the click was handled
   */
  @Override
  public boolean onPreferenceChange(Preference preference, Object newValue) {
    if (preference.equals(mLanguage)) {
      if (!((SettingsActivity) mActivity).isChanged()) {
        mApp.setApplicationLanguage("" + newValue);
        mActivity.finish();
        return true;
      } else {
        new AlertDialog.Builder(mActivity)
            .setCancelable(false)
            .setIcon(R.mipmap.ic_launcher)
            .setTitle(preference.getTitle())
            .setMessage(R.string.control_language_change)
            .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> dialog.dismiss()).show();
      }
    }
    return false;
  }
}