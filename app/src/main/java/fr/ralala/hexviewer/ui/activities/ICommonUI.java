package fr.ralala.hexviewer.ui.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;

import fr.ralala.hexviewer.application.ApplicationCtx;
import fr.ralala.hexviewer.models.FileData;
import fr.ralala.hexviewer.ui.launchers.LauncherLineUpdate;
import fr.ralala.hexviewer.ui.launchers.LauncherOpen;
import fr.ralala.hexviewer.ui.launchers.LauncherPartialOpen;
import fr.ralala.hexviewer.ui.payload.PayloadHexHelper;
import fr.ralala.hexviewer.ui.payload.PayloadPlainSwipe;
import fr.ralala.hexviewer.ui.tasks.TaskOpen;
import fr.ralala.hexviewer.ui.tasks.TaskSave;
import fr.ralala.hexviewer.ui.undoredo.UnDoRedo;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Common UI components to limit the spread of the main activity.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
// The purpose of this interface is indeed to cross packages...
@SuppressWarnings("squid:S7091")
public interface ICommonUI extends TaskOpen.OpenResultListener, TaskSave.SaveResultListener {
  /**
   * Returns the PayloadHexHelper
   *
   * @return PayloadHexHelper
   */
  PayloadHexHelper getPayloadHex();

  /**
   * Returns the PayloadPlainSwipe
   *
   * @return PayloadPlainSwipe
   */
  PayloadPlainSwipe getPayloadPlain();


  /**
   * Returns the launcher used with the partial open
   *
   * @return LauncherPartialOpen
   */
  LauncherPartialOpen getLauncherPartialOpen();

  /**
   * Returns the LauncherOpen
   *
   * @return LauncherOpen
   */
  LauncherOpen getLauncherOpen();

  /**
   * Returns the LauncherLineUpdate
   *
   * @return LauncherLineUpdate
   */
  LauncherLineUpdate getLauncherLineUpdate();

  /**
   * Returns the search query.
   *
   * @return String
   */
  String getSearchQuery();

  /**
   * Returns the undo/redo.
   *
   * @return UnDoRedo
   */
  UnDoRedo getUnDoRedo();

  /**
   * Returns the file data.
   *
   * @return FileData
   */
  FileData getFileData();

  /**
   * Sets the file data.
   *
   * @param fd FileData
   */
  void setFileData(FileData fd);

  /**
   * Returns the current application context.
   *
   * @return ApplicationCtx
   */
  ApplicationCtx getApplicationCtx();

  /**
   * Refreshes the activity title.
   */
  void refreshTitle();

  /**
   * Sets the orphan dialog.
   *
   * @param orphan The dialog.
   */
  void setOrphanDialog(AlertDialog orphan);

  /**
   * Returns the menu RecentlyOpen
   *
   * @return MenuItem
   */
  AppCompatTextView getMenuRecentlyOpen();

  /**
   * Method to be invoked when a line has been clicked.
   */
  void onLineItemClick(int position);
}
