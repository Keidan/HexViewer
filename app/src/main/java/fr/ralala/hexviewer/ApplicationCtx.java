package fr.ralala.hexviewer;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import androidx.emoji.bundled.BundledEmojiCompatConfig;
import androidx.emoji.text.EmojiCompat;
import androidx.preference.PreferenceManager;
import fr.ralala.hexviewer.models.ListSettings;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Main application context
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class ApplicationCtx extends Application {
  public static final String CFG_ABBREVIATE_PORTRAIT = "abbreviatePortrait";
  public static final String CFG_ABBREVIATE_LANDSCAPE = "abbreviateLandscape";
  public static final String CFG_LISTS_PORTRAIT = "listsPortrait";
  public static final String CFG_LISTS_LANDSCAPE = "listsLandscape";
  public static final String CFG_PORTRAIT_HEX_DISPLAY_DATA = "hexDisplayData";
  public static final String CFG_PORTRAIT_HEX_ROW_HEIGHT = "hexRowHeight";
  public static final String CFG_PORTRAIT_HEX_ROW_HEIGHT_AUTO = "hexRowHeightAuto";
  public static final String CFG_PORTRAIT_HEX_FONT_SIZE = "hexFontSize";
  public static final String CFG_LANDSCAPE_HEX_DISPLAY_DATA = "hexDisplayDataLandscape";
  public static final String CFG_LANDSCAPE_HEX_ROW_HEIGHT = "hexRowHeightLandscape";
  public static final String CFG_LANDSCAPE_HEX_ROW_HEIGHT_AUTO = "hexRowHeightAutoLandscape";
  public static final String CFG_LANDSCAPE_HEX_FONT_SIZE = "hexFontSizeLandscape";
  public static final String CFG_PORTRAIT_HEX_DISPLAY_DATA_LINE_NUMBERS = "hexDisplayDataLineNumbers";
  public static final String CFG_PORTRAIT_HEX_ROW_HEIGHT_LINE_NUMBERS = "hexRowHeightLineNumbers";
  public static final String CFG_PORTRAIT_HEX_ROW_HEIGHT_AUTO_LINE_NUMBERS = "hexRowHeightAutoLineNumbers";
  public static final String CFG_PORTRAIT_HEX_FONT_SIZE_LINE_NUMBERS = "hexFontSizeLineNumbers";
  public static final String CFG_LANDSCAPE_HEX_DISPLAY_DATA_LINE_NUMBERS = "hexDisplayDataLineNumbersLandscape";
  public static final String CFG_LANDSCAPE_HEX_ROW_HEIGHT_LINE_NUMBERS = "hexRowHeightLineNumbersLandscape";
  public static final String CFG_LANDSCAPE_HEX_ROW_HEIGHT_AUTO_LINE_NUMBERS = "hexRowHeightAutoLineNumbersLandscape";
  public static final String CFG_LANDSCAPE_HEX_FONT_SIZE_LINE_NUMBERS = "hexFontSizeLineNumbersLandscape";
  public static final String CFG_PORTRAIT_PLAIN_ROW_HEIGHT = "plainRowHeight";
  public static final String CFG_PORTRAIT_PLAIN_ROW_HEIGHT_AUTO = "plainRowHeightAuto";
  public static final String CFG_PORTRAIT_PLAIN_FONT_SIZE = "plainFontSize";
  public static final String CFG_LANDSCAPE_PLAIN_ROW_HEIGHT = "plainRowHeightLandscape";
  public static final String CFG_LANDSCAPE_PLAIN_ROW_HEIGHT_AUTO = "plainRowHeightAutoLandscape";
  public static final String CFG_LANDSCAPE_PLAIN_FONT_SIZE = "plainFontSizeLandscape";
  public static final String CFG_SMART_INPUT = "smartInput";
  public static final String CFG_RECENTLY_OPEN = "recentlyOpen";
  public static final String CFG_VERSION = "version";
  public static final String CFG_LICENSE = "license";
  public static final String CFG_LANGUAGE = "language";
  public static final String CFG_LINES_NUMBER = "linesNumber";
  public static final String CFG_OVERWRITE = "overwrite";
  public static final String CFG_SCREEN_ORIENTATION = "screenOrientation";
  public static final String CFG_NB_BYTES_PER_LINE = "nbBytesPerLine";
  private SharedPreferences mSharedPreferences;
  private String mDefaultAbbreviatePortrait;
  private String mDefaultAbbreviateLandscape;
  private boolean mDefaultSmartInput;
  private boolean mDefaultOverwrite;
  private List<String> mRecentlyOpened;
  private static ApplicationCtx instance;
  private String mLanguage = null;
  private boolean mDefaultLinesNumber;
  private ListSettings mListSettingsHexPortrait;
  private ListSettings mListSettingsHexLandscape;
  private ListSettings mListSettingsHexLineNumbersPortrait;
  private ListSettings mListSettingsHexLineNumbersLandscape;
  private ListSettings mListSettingsPlainPortrait;
  private ListSettings mListSettingsPlainLandscape;
  private String mDefaultScreenOrientation;
  private String mDefaultNbBytesPerLine;

  public static ApplicationCtx getInstance() {
    return instance;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    instance = this;
    mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    mDefaultAbbreviatePortrait = getString(R.string.default_abbreviate_portrait);
    mDefaultAbbreviateLandscape = getString(R.string.default_abbreviate_landscape);
    mDefaultSmartInput = Boolean.parseBoolean(getString(R.string.default_smart_input));
    mRecentlyOpened = getRecentlyOpened();
    mDefaultLinesNumber = Boolean.parseBoolean(getString(R.string.default_lines_number));
    mDefaultOverwrite = Boolean.parseBoolean(getString(R.string.default_overwrite));
    mDefaultScreenOrientation = getString(R.string.default_screen_orientation);
    mDefaultNbBytesPerLine = getString(R.string.default_nb_bytes_per_line);

    mListSettingsHexPortrait = new ListSettings(this,
        CFG_PORTRAIT_HEX_DISPLAY_DATA,
        CFG_PORTRAIT_HEX_ROW_HEIGHT, CFG_PORTRAIT_HEX_ROW_HEIGHT_AUTO, CFG_PORTRAIT_HEX_FONT_SIZE,
        R.string.default_hex_display_data_portrait,
        R.string.default_hex_row_height_portrait, R.string.default_hex_row_height_auto_portrait,
        R.string.default_hex_font_size_portrait);
    mListSettingsHexLandscape = new ListSettings(this,
        CFG_LANDSCAPE_HEX_DISPLAY_DATA,
        CFG_LANDSCAPE_HEX_ROW_HEIGHT, CFG_LANDSCAPE_HEX_ROW_HEIGHT_AUTO, CFG_LANDSCAPE_HEX_FONT_SIZE,
        R.string.default_hex_display_data_landscape,
        R.string.default_hex_row_height_landscape, R.string.default_hex_row_height_auto_landscape,
        R.string.default_hex_font_size_landscape);

    mListSettingsHexLineNumbersPortrait = new ListSettings(this,
        CFG_PORTRAIT_HEX_DISPLAY_DATA_LINE_NUMBERS,
        CFG_PORTRAIT_HEX_ROW_HEIGHT_LINE_NUMBERS, CFG_PORTRAIT_HEX_ROW_HEIGHT_AUTO_LINE_NUMBERS, CFG_PORTRAIT_HEX_FONT_SIZE_LINE_NUMBERS,
        R.string.default_hex_display_data_portrait_lines_numbers,
        R.string.default_hex_row_height_portrait_lines_numbers, R.string.default_hex_row_height_auto_portrait_lines_numbers,
        R.string.default_hex_font_size_portrait_lines_numbers);
    mListSettingsHexLineNumbersLandscape = new ListSettings(this,
        CFG_LANDSCAPE_HEX_DISPLAY_DATA_LINE_NUMBERS,
        CFG_LANDSCAPE_HEX_ROW_HEIGHT_LINE_NUMBERS, CFG_LANDSCAPE_HEX_ROW_HEIGHT_AUTO_LINE_NUMBERS, CFG_LANDSCAPE_HEX_FONT_SIZE_LINE_NUMBERS,
        R.string.default_hex_display_data_landscape_lines_numbers,
        R.string.default_hex_row_height_landscape_lines_numbers, R.string.default_hex_row_height_auto_landscape_lines_numbers,
        R.string.default_hex_font_size_landscape_lines_numbers);

    mListSettingsPlainPortrait = new ListSettings(this,
        null,
        CFG_PORTRAIT_PLAIN_ROW_HEIGHT, CFG_PORTRAIT_PLAIN_ROW_HEIGHT_AUTO, CFG_PORTRAIT_PLAIN_FONT_SIZE,
        0, R.string.default_plain_row_height_portrait, R.string.default_plain_row_height_auto_portrait,
        R.string.default_plain_font_size_portrait);
    mListSettingsPlainLandscape = new ListSettings(this,
        null,
        CFG_LANDSCAPE_PLAIN_ROW_HEIGHT, CFG_LANDSCAPE_PLAIN_ROW_HEIGHT_AUTO, CFG_LANDSCAPE_PLAIN_FONT_SIZE,
        0, R.string.default_plain_row_height_landscape, R.string.default_plain_row_height_auto_landscape,
        R.string.default_plain_font_size_landscape);
    /* EmojiCompat */
    EmojiCompat.Config config = new BundledEmojiCompatConfig(this);
    EmojiCompat.init(config);
    loadDefaultLocal();
    setApplicationLanguage(mLanguage);
  }

  public SharedPreferences getPref(final Context context) {
    if (mSharedPreferences == null)
      mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    return mSharedPreferences;
  }

  /* ---------- Settings ---------- */

  /**
   * Sets the number of bytes per line.
   *
   * @param nb The new value.
   */
  public void setNbBytesPerLine(String nb) {
    SharedPreferences.Editor e = getPref(this).edit();
    e.putString(CFG_NB_BYTES_PER_LINE, nb);
    e.apply();
  }

  /**
   * Returns the number of bytes per line.
   *
   * @return SysHelper.MAX_BY_ROW_8 or SysHelper.MAX_BY_ROW_16
   */
  public int getNbBytesPerLine() {
    try {
      return Integer.parseInt(getPref(this).getString(CFG_NB_BYTES_PER_LINE, mDefaultNbBytesPerLine));
    } catch (Exception ignore) {
      return Integer.parseInt(mDefaultNbBytesPerLine);
    }
  }

  /**
   * Returns the orientation of the screen according to the configuration.
   *
   * @return SCREEN_ORIENTATION_LANDSCAPE,
   * SCREEN_ORIENTATION_PORTRAIT or
   * SCREEN_ORIENTATION_UNSPECIFIED
   */
  public String getScreenOrientationStr() {
    return getPref(this).getString(CFG_SCREEN_ORIENTATION, mDefaultScreenOrientation);
  }

  /**
   * Returns the orientation of the screen according to the configuration.
   *
   * @param ref The reference value, if it is null, the value stored in the parameters will be used.
   * @return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
   * ActivityInfo.SCREEN_ORIENTATION_PORTRAIT or
   * ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
   */
  public int getScreenOrientation(final String ref) {
    String s = ref != null ? ref : getScreenOrientationStr();
    if (s.equals("SCREEN_ORIENTATION_LANDSCAPE"))
      return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    if (s.equals("SCREEN_ORIENTATION_PORTRAIT"))
      return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
  }

  /**
   * Tests if the line numbers are displayed or not.
   *
   * @return bool
   */
  public boolean isLineNumber() {
    try {
      return getPref(this).getBoolean(CFG_LINES_NUMBER, mDefaultLinesNumber);
    } catch (Exception ignore) {
      return mDefaultLinesNumber;
    }
  }

  /**
   * Show/Hide the line numbers.
   *
   * @param mode The new mode.
   */
  public void setLineNumber(boolean mode) {
    SharedPreferences.Editor e = getPref(this).edit();
    e.putBoolean(CFG_LINES_NUMBER, mode);
    e.apply();
  }

  /**
   * Adds a new element to the list.
   *
   * @param recent The new element
   */
  public void addRecentlyOpened(String recent) {
    mRecentlyOpened.remove(recent);
    mRecentlyOpened.add(recent);
    setRecentlyOpened(mRecentlyOpened);
  }

  /**
   * Removes an existing element from the list.
   *
   * @param recent The new element
   */
  public void removeRecentlyOpened(String recent) {
    mRecentlyOpened.remove(recent);
    setRecentlyOpened(mRecentlyOpened);
  }

  /**
   * Returns the list of recently opened files.
   *
   * @return Set<String>
   */
  public List<String> getRecentlyOpened() {
    final List<String> uris = new ArrayList<>();
    final String content = getPref(this).getString(CFG_RECENTLY_OPEN, "");
    final String[] split = content.split("\\|");
    if (split.length != 0 && !split[0].equals(""))
      Collections.addAll(uris, split);
    return uris;
  }

  /**
   * Sets the list of recently opened files.
   *
   * @param list The list
   */
  private void setRecentlyOpened(List<String> list) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < list.size(); i++) {
      sb.append(list.get(i));
      if (i != list.size() - 1)
        sb.append("|");
    }
    SharedPreferences.Editor e = getPref(this).edit();
    e.putString(CFG_RECENTLY_OPEN, sb.toString());
    e.apply();
  }

  /**
   * Test if smart input is enabled or not.
   *
   * @return bool
   */
  public boolean isSmartInput() {
    try {
      return getPref(this).getBoolean(CFG_SMART_INPUT, mDefaultSmartInput);
    } catch (Exception ignore) {
      return mDefaultSmartInput;
    }
  }

  /**
   * Enable/Disable the smart input.
   *
   * @param mode The new mode.
   */
  public void setSmartInput(boolean mode) {
    SharedPreferences.Editor e = getPref(this).edit();
    e.putBoolean(CFG_SMART_INPUT, mode);
    e.apply();
  }

  /**
   * Test if overwrite is enabled or not.
   *
   * @return bool
   */
  public boolean isOverwrite() {
    try {
      return getPref(this).getBoolean(CFG_OVERWRITE, mDefaultOverwrite);
    } catch (Exception ignore) {
      return mDefaultOverwrite;
    }
  }

  /**
   * Enable/Disable the overwrite mode.
   *
   * @param mode The new mode.
   */
  public void setOverwrite(boolean mode) {
    SharedPreferences.Editor e = getPref(this).edit();
    e.putBoolean(CFG_OVERWRITE, mode);
    e.apply();
  }

  /**
   * Returns the number of characters to display for the file name in portrait mode.
   *
   * @return int
   */
  public int getAbbreviatePortrait() {
    try {
      return Integer.parseInt(getPref(this).getString(CFG_ABBREVIATE_PORTRAIT, mDefaultAbbreviatePortrait));
    } catch (Exception ignore) {
      return Integer.parseInt(mDefaultAbbreviatePortrait);
    }
  }

  /**
   * Change the number of characters to display for the file name in portrait mode.
   *
   * @param number The new number.
   */
  public void setAbbreviatePortrait(int number) {
    SharedPreferences.Editor e = getPref(this).edit();
    e.putString(CFG_ABBREVIATE_PORTRAIT, String.valueOf(number));
    e.apply();
  }

  /**
   * Returns the number of characters to display for the file name in landscape mode.
   *
   * @return int
   */
  public int getAbbreviateLandscape() {
    try {
      return Integer.parseInt(getPref(this).getString(CFG_ABBREVIATE_LANDSCAPE, mDefaultAbbreviateLandscape));
    } catch (Exception ignore) {
      return Integer.parseInt(mDefaultAbbreviateLandscape);
    }
  }

  /**
   * Change the number of characters to display for the file name in landscape mode.
   *
   * @param number The new number.
   */
  public void setAbbreviateLandscape(int number) {
    SharedPreferences.Editor e = getPref(this).edit();
    e.putString(CFG_ABBREVIATE_LANDSCAPE, String.valueOf(number));
    e.apply();
  }

  /**
   * Returns the list settings for the hex list (portrait).
   *
   * @return ListSettings
   */
  public ListSettings getListSettingsHexPortrait() {
    return mListSettingsHexPortrait;
  }

  /**
   * Returns the list settings for the hex list (landscape).
   *
   * @return ListSettings
   */
  public ListSettings getListSettingsHexLandscape() {
    return mListSettingsHexLandscape;
  }

  /**
   * Returns the list settings for the hex list with line numbers(portrait).
   *
   * @return ListSettings
   */
  public ListSettings getListSettingsHexLineNumbersPortrait() {
    return mListSettingsHexLineNumbersPortrait;
  }

  /**
   * Returns the list settings for the hex list with line numbers(landscape).
   *
   * @return ListSettings
   */
  public ListSettings getListSettingsHexLineNumbersLandscape() {
    return mListSettingsHexLineNumbersLandscape;
  }

  /**
   * Returns the list settings for the plain text list(portrait).
   *
   * @return ListSettings
   */
  public ListSettings getListSettingsPlainPortrait() {
    return mListSettingsPlainPortrait;
  }

  /**
   * Returns the list settings for the plain text list(landscape).
   *
   * @return ListSettings
   */
  public ListSettings getListSettingsPlainLandscape() {
    return mListSettingsPlainLandscape;
  }
  /*-------------------- LOCALE --------------------*/

  /**
   * Change the application language.
   *
   * @param activity The activity to restart.
   */
  public void applyApplicationLanguage(Activity activity) {
    String cfg = getApplicationLanguage(this);
    String cfgLang = cfg.replace('-', '_');
    Locale locale = Locale.getDefault();
    if (!locale.toString().equals(cfgLang))
      activity.recreate();
  }

  /**
   * Sets the application language (config only).
   *
   * @param lang The new language.
   */
  public void setApplicationLanguage(final String lang) {
    SharedPreferences sp = getPref(this);
    SharedPreferences.Editor e = sp.edit();
    e.putString(ApplicationCtx.CFG_LANGUAGE, lang);
    e.apply();
  }

  /**
   * Returns the application language.
   *
   * @param context Context.
   * @return String.
   */
  public String getApplicationLanguage(final Context context) {
    return getPref(context).getString(CFG_LANGUAGE, mLanguage);
  }

  /**
   * Set the base context for this ContextWrapper.
   * All calls will then be delegated to the base context.
   * Throws IllegalStateException if a base context has already been set.
   *
   * @param base The new base context for this wrapper.
   */
  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(onAttach(base));
  }

  /**
   * Loads the default locale.
   */
  private void loadDefaultLocal() {
    if (mLanguage == null) {
      Locale loc = Locale.getDefault();
      mLanguage = loc.getLanguage();
      loc.getCountry();
      if (!loc.getCountry().isEmpty())
        mLanguage += "-" + loc.getCountry();
    }
  }

  /**
   * This method must be called in attachBaseContext.
   *
   * @param context Context
   * @return The new cfg context.
   */
  public Context onAttach(Context context) {
    loadDefaultLocal();
    String lang = getApplicationLanguage(context);
    mLanguage = lang;
    String[] split = lang.split("-");
    Locale locale;
    if (split.length == 2)
      locale = new Locale(split[0], split[1]);
    else
      locale = new Locale(split[0]);

    Locale.setDefault(locale);

    Configuration configuration = context.getResources().getConfiguration();
    configuration.setLocale(locale);
    configuration.setLayoutDirection(locale);

    return context.createConfigurationContext(configuration);
  }
}
