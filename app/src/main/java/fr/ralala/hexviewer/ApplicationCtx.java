package fr.ralala.hexviewer;

import android.app.Application;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.preference.PreferenceManager;
import fr.ralala.hexviewer.utils.Payload;

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
  private final Payload mPayload;
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
  private final AtomicBoolean mHexChanged;
  private static ApplicationCtx instance;

  /**
   * Constructs the application context.
   */
  public ApplicationCtx() {
    super();
    mPayload = new Payload();
    mHexChanged = new AtomicBoolean(false);
  }

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
  }

  /**
   * Returns the object allowing to know if a change has taken place or not.
   *
   * @return AtomicBoolean
   */
  public AtomicBoolean getHexChanged() {
    return mHexChanged;
  }

  /**
   * Returns the payload.
   *
   * @return Payload
   */
  public Payload getPayload() {
    return mPayload;
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
    final String content = mSharedPreferences.getString(CFG_RECENTLY_OPEN, "");
    final String[] split = content.split("\\|");
    if(split.length != 0 && !split[0].equals(""))
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
    for(int i = 0; i < list.size(); i++) {
      sb.append(list.get(i));
      if(i != list.size() - 1)
        sb.append("|");
    }
    SharedPreferences.Editor e = mSharedPreferences.edit();
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
      return mSharedPreferences.getBoolean(CFG_SMART_INPUT, mDefaultSmartInput);
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
    SharedPreferences.Editor e = mSharedPreferences.edit();
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
      return Integer.parseInt(mSharedPreferences.getString(CFG_ABBREVIATE_PORTRAIT, mDefaultAbbreviatePortrait));
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
    SharedPreferences.Editor e = mSharedPreferences.edit();
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
      return Integer.parseInt(mSharedPreferences.getString(CFG_ABBREVIATE_LANDSCAPE, mDefaultAbbreviateLandscape));
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
    SharedPreferences.Editor e = mSharedPreferences.edit();
    e.putString(CFG_ABBREVIATE_LANDSCAPE, String.valueOf(number));
    e.apply();
  }

  /**
   * Returns the row height auto state for the hex listview.
   *
   * @return boolean
   */
  public boolean isHexRowHeightAuto() {
    try {
      return mSharedPreferences.getBoolean(CFG_HEX_ROW_HEIGHT_AUTO, mDefaultHexRowHeightAuto);
    } catch (Exception ignore) {
      return mDefaultHexRowHeightAuto;
    }
  }

  /**
   * Returns the row height for the hex listview.
   *
   * @return int
   */
  public int getHexRowHeight() {
    try {
      return Integer.parseInt(mSharedPreferences.getString(CFG_HEX_ROW_HEIGHT, mDefaultHexRowHeight));
    } catch (Exception ignore) {
      return Integer.parseInt(mDefaultHexRowHeight);
    }
  }

  /**
   * Change the the row height for the hex listview.
   *
   * @param number The new number.
   */
  public void setHexRowHeight(int number) {
    SharedPreferences.Editor e = mSharedPreferences.edit();
    e.putString(CFG_HEX_ROW_HEIGHT, String.valueOf(number));
    e.apply();
  }

  /**
   * Returns the font size for the hex listview.
   *
   * @return float
   */
  public float getHexFontSize() {
    try {
      return Float.parseFloat(mSharedPreferences.getString(CFG_HEX_FONT_SIZE, mDefaultHexFontSize));
    } catch (Exception ignore) {
      return Float.parseFloat(mDefaultHexFontSize);
    }
  }

  /**
   * Change the the font size for the hex listview.
   *
   * @param number The new number.
   */
  public void setHexFontSize(float number) {
    SharedPreferences.Editor e = mSharedPreferences.edit();
    e.putString(CFG_HEX_FONT_SIZE, String.valueOf(number));
    e.apply();
  }


  /**
   * Returns the row height auto state for the hex listview.
   *
   * @return boolean
   */
  public boolean isPlainRowHeightAuto() {
    try {
      return mSharedPreferences.getBoolean(CFG_PLAIN_ROW_HEIGHT_AUTO, mDefaultPlainRowHeightAuto);
    } catch (Exception ignore) {
      return mDefaultPlainRowHeightAuto;
    }
  }

  /**
   * Returns the row height for the hex listview.
   *
   * @return int
   */
  public int getPlainRowHeight() {
    try {
      return Integer.parseInt(mSharedPreferences.getString(CFG_PLAIN_ROW_HEIGHT, mDefaultPlainRowHeight));
    } catch (Exception ignore) {
      return Integer.parseInt(mDefaultPlainRowHeight);
    }
  }

  /**
   * Change the the row height for the hex listview.
   *
   * @param number The new number.
   */
  public void setPlainRowHeight(int number) {
    SharedPreferences.Editor e = mSharedPreferences.edit();
    e.putString(CFG_PLAIN_ROW_HEIGHT, String.valueOf(number));
    e.apply();
  }

  /**
   * Returns the font size for the hex listview.
   *
   * @return float
   */
  public float getPlainFontSize() {
    try {
      return Float.parseFloat(mSharedPreferences.getString(CFG_PLAIN_FONT_SIZE, mDefaultPlainFontSize));
    } catch (Exception ignore) {
      return Float.parseFloat(mDefaultPlainFontSize);
    }
  }

  /**
   * Change the the font size for the hex listview.
   *
   * @param number The new number.
   */
  public void setPlainFontSize(float number) {
    SharedPreferences.Editor e = mSharedPreferences.edit();
    e.putString(CFG_PLAIN_FONT_SIZE, String.valueOf(number));
    e.apply();
  }
}
