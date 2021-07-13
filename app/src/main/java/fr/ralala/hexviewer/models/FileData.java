package fr.ralala.hexviewer.models;

import android.net.Uri;

import fr.ralala.hexviewer.utils.FileHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * File data representation (used by MainActivity)
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class FileData {
  private final String mName;
  private final Uri mUri;
  private boolean mOpenFromAppIntent;

  public FileData(final Uri uri, boolean openFromAppIntent) {
    mName = FileHelper.getFileName(uri);
    mUri = uri;
    mOpenFromAppIntent = openFromAppIntent;
  }

  /**
   * Tests if the file is opened from the application's intent.
   *
   * @return boolean
   */
  public boolean isOpenFromAppIntent() {
    return mOpenFromAppIntent;
  }

  /**
   * Clears the open from app intent flag.
   */
  public void clearOpenFromAppIntent() {
    mOpenFromAppIntent = false;
  }

  /**
   * Tests if the name is empty.
   *
   * @param fd FileData
   * @return boolean
   */
  public static boolean isEmpty(FileData fd) {
    return fd == null || fd.mName == null || fd.mName.isEmpty();
  }

  /**
   * Returns the file name.
   *
   * @return String
   */
  public String getName() {
    return mName;
  }

  /**
   * Returns the Uri
   *
   * @return Uri
   */
  public Uri getUri() {
    return mUri;
  }
}
