package fr.ralala.hexviewer.ui.adapters.config;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.ui.adapters.SearchableListArrayAdapter;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Implementation of the user configuration for portrait mode.
 * </p>
 *
 * @author Keidan
 *
 * License: GPLv3
 * <p>
 * ******************************************************************************
 */
public class UserConfigPortrait implements SearchableListArrayAdapter.UserConfig {
  private final ApplicationCtx mApp;
  private final boolean mIsHexList;

  public UserConfigPortrait(boolean isHexList) {
    mIsHexList = isHexList;
    mApp = ApplicationCtx.getInstance();
  }

  @Override
  public float getFontSize() {
    return mIsHexList ? mApp.getHexFontSizePortrait() : mApp.getPlainFontSizePortrait();
  }

  @Override
  public int getRowHeight() {
    return mIsHexList ? mApp.getHexRowHeightPortrait() : mApp.getPlainRowHeightPortrait();
  }

  @Override
  public boolean isRowHeightAuto() {
    return mIsHexList ? mApp.isHexRowHeightAutoPortrait() : mApp.isPlainRowHeightAutoPortrait();
  }
}
