package fr.ralala.hexviewer.ui.adapters.config;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * User configuration interface.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public interface UserConfig {
  float getFontSize();

  int getRowHeight();

  boolean isRowHeightAuto();

  boolean isDataColumnNotDisplayed();
}
