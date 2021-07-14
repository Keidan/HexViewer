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
 * <p>
 * License: GPLv3
 * </p>
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
    if (mIsHexList) {
      if (mApp.isLineNumber())
        return mApp.getListSettingsHexLineNumbersPortrait().getFontSize();
      return mApp.getListSettingsHexPortrait().getFontSize();
    }
    return mApp.getListSettingsPlainPortrait().getFontSize();
  }

  @Override
  public int getRowHeight() {
    if (mIsHexList) {
      if (mApp.isLineNumber())
        return mApp.getListSettingsHexLineNumbersPortrait().getRowHeight();
      return mApp.getListSettingsHexPortrait().getRowHeight();
    }
    return mApp.getListSettingsPlainPortrait().getRowHeight();
  }

  @Override
  public boolean isRowHeightAuto() {
    if (mIsHexList) {
      if (mApp.isLineNumber())
        return mApp.getListSettingsHexLineNumbersPortrait().isRowHeightAuto();
      return mApp.getListSettingsHexPortrait().isRowHeightAuto();
    }
    return mApp.getListSettingsPlainPortrait().isRowHeightAuto();
  }

  @Override
  public boolean isDataColumnNotDisplayed() {
    if (mIsHexList) {
      if (mApp.isLineNumber())
        return !mApp.getListSettingsHexLineNumbersPortrait().isDisplayDataColumn();
      return !mApp.getListSettingsHexPortrait().isDisplayDataColumn();
    }
    return false;
  }
}
