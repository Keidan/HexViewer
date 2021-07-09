package fr.ralala.hexviewer.ui.adapters.config;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.ui.adapters.SearchableListArrayAdapter;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Implementation of the user configuration for landscape mode.
 * </p>
 *
 * @author Keidan
 *
 * License: GPLv3
 * <p>
 * ******************************************************************************
 */
public class UserConfigLandscape implements SearchableListArrayAdapter.UserConfig {
  private final ApplicationCtx mApp;
  private final boolean mIsHexList;

  public UserConfigLandscape(boolean isHexList) {
    mIsHexList = isHexList;
    mApp = ApplicationCtx.getInstance();
  }

  @Override
  public float getFontSize() {
    return mIsHexList ? mApp.getHexFontSizeLandscape() : mApp.getPlainFontSizeLandscape();
  }

  @Override
  public int getRowHeight() {
    return mIsHexList ? mApp.getHexRowHeightLandscape() : mApp.getPlainRowHeightLandscape();
  }

  @Override
  public boolean isRowHeightAuto() {
    return mIsHexList ? mApp.isHexRowHeightAutoLandscape() : mApp.isPlainRowHeightAutoLandscape();
  }
}
