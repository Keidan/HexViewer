package fr.ralala.hexviewer.utils;

import android.net.Uri;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * File data representation (used by MainActivity)
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class FileData {
  private final String mName;
  private final Uri mUri;

  public FileData(final Uri uri) {
    mName = FileHelper.getFileName(uri);
    mUri = uri;
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
