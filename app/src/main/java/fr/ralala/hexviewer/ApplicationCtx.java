package fr.ralala.hexviewer;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import androidx.emoji.bundled.BundledEmojiCompatConfig;
import androidx.emoji.text.EmojiCompat;
import androidx.preference.PreferenceManager;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Main application context
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class ApplicationCtx extends Application {
  public static final String CFG_ABBREVIATE_PORTRAIT = "abbreviatePortrait";
  public static final String CFG_ABBREVIATE_LANDSCAPE = "abbreviateLandscape";
  public static final String CFG_HEX_ROW_HEIGHT = "hexRowHeight";
  public static final String CFG_HEX_ROW_HEIGHT_AUTO = "hexRowHeightAuto";
  public static final String CFG_HEX_FONT_SIZE = "hexFontSize";
  public static final String CFG_PLAIN_ROW_HEIGHT = "plainRowHeight";
  public static final String CFG_PLAIN_ROW_HEIGHT_AUTO = "plainRowHeightAuto";
  public static final String CFG_PLAIN_FONT_SIZE = "plainFontSize";
  public static final String CFG_SMART_INPUT = "smartInput";
  public static final String CFG_RECENTLY_OPEN = "recentlyOpen";
  public static final String CFG_VERSION = "version";
  public static final String CFG_LICENSE = "license";
  public static final String CFG_LANGUAGE = "language";
  private SharedPreferences mSharedPreferences;
  private String mDefaultAbbreviatePortrait;
  private String mDefaultAbbreviateLandscape;
  private String mDefaultHexRowHeight;
  private boolean mDefaultHexRowHeightAuto;
  private String mDefaultHexFontSize;
  private String mDefaultPlainRowHeight;
  private boolean mDefaultPlainRowHeightAuto;
  private String mDefaultPlainFontSize;
  private boolean mDefaultSmartInput;
  private List<String> mRecentlyOpened;
  private static ApplicationCtx instance;
  private String mLanguage = null;

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
    mDefaultHexRowHeightAuto = Boolean.parseBoolean(getString(R.string.default_hex_row_height_auto));
    mDefaultHexRowHeight = getString(R.string.default_hex_row_height);
    mDefaultHexFontSize = getString(R.string.default_hex_font_size);
    mDefaultPlainRowHeightAuto = Boolean.parseBoolean(getString(R.string.default_plain_row_height_auto));
    mDefaultPlainRowHeight = getString(R.string.default_plain_row_height);
    mDefaultPlainFontSize = getString(R.string.default_plain_font_size);
    mDefaultSmartInput = Boolean.parseBoolean(getString(R.string.default_smart_input));
    mRecentlyOpened = getRecentlyOpened();

    /* EmojiCompat */
    EmojiCompat.Config config = new BundledEmojiCompatConfig(this);
    EmojiCompat.init(config);
    loadDefaultLocal();
    setApplicationLanguage(mLanguage);
  }

  private SharedPreferences getPref(final Context context) {
    if (mSharedPreferences == null)
      mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    return mSharedPreferences;
  }

  /* ---------- Settings ---------- */

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
   * Returns the row height auto state for the hex list view.
   *
   * @return boolean
   */
  public boolean isHexRowHeightAuto() {
    try {
      return getPref(this).getBoolean(CFG_HEX_ROW_HEIGHT_AUTO, mDefaultHexRowHeightAuto);
    } catch (Exception ignore) {
      return mDefaultHexRowHeightAuto;
    }
  }

  /**
   * Returns the row height for the hex list view.
   *
   * @return int
   */
  public int getHexRowHeight() {
    try {
      return Integer.parseInt(getPref(this).getString(CFG_HEX_ROW_HEIGHT, mDefaultHexRowHeight));
    } catch (Exception ignore) {
      return Integer.parseInt(mDefaultHexRowHeight);
    }
  }

  /**
   * Change the row height for the hex list view.
   *
   * @param number The new number.
   */
  public void setHexRowHeight(int number) {
    SharedPreferences.Editor e = getPref(this).edit();
    e.putString(CFG_HEX_ROW_HEIGHT, String.valueOf(number));
    e.apply();
  }

  /**
   * Returns the font size for the hex list view.
   *
   * @return float
   */
  public float getHexFontSize() {
    try {
      return Float.parseFloat(getPref(this).getString(CFG_HEX_FONT_SIZE, mDefaultHexFontSize));
    } catch (Exception ignore) {
      return Float.parseFloat(mDefaultHexFontSize);
    }
  }

  /**
   * Change the font size for the hex list view.
   *
   * @param number The new number.
   */
  public void setHexFontSize(float number) {
    SharedPreferences.Editor e = getPref(this).edit();
    e.putString(CFG_HEX_FONT_SIZE, String.valueOf(number));
    e.apply();
  }


  /**
   * Returns the row height auto state for the hex list view.
   *
   * @return boolean
   */
  public boolean isPlainRowHeightAuto() {
    try {
      return getPref(this).getBoolean(CFG_PLAIN_ROW_HEIGHT_AUTO, mDefaultPlainRowHeightAuto);
    } catch (Exception ignore) {
      return mDefaultPlainRowHeightAuto;
    }
  }

  /**
   * Returns the row height for the hex list view.
   *
   * @return int
   */
  public int getPlainRowHeight() {
    try {
      return Integer.parseInt(getPref(this).getString(CFG_PLAIN_ROW_HEIGHT, mDefaultPlainRowHeight));
    } catch (Exception ignore) {
      return Integer.parseInt(mDefaultPlainRowHeight);
    }
  }

  /**
   * Change the row height for the hex list view.
   *
   * @param number The new number.
   */
  public void setPlainRowHeight(int number) {
    SharedPreferences.Editor e = getPref(this).edit();
    e.putString(CFG_PLAIN_ROW_HEIGHT, String.valueOf(number));
    e.apply();
  }

  /**
   * Returns the font size for the hex list view.
   *
   * @return float
   */
  public float getPlainFontSize() {
    try {
      return Float.parseFloat(getPref(this).getString(CFG_PLAIN_FONT_SIZE, mDefaultPlainFontSize));
    } catch (Exception ignore) {
      return Float.parseFloat(mDefaultPlainFontSize);
    }
  }

  /**
   * Change the font size for the hex list view.
   *
   * @param number The new number.
   */
  public void setPlainFontSize(float number) {
    SharedPreferences.Editor e = getPref(this).edit();
    e.putString(CFG_PLAIN_FONT_SIZE, String.valueOf(number));
    e.apply();
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
      if (loc.getCountry() != null && !loc.getCountry().isEmpty())
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
