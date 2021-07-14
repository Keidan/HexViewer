package fr.ralala.hexviewer.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import fr.ralala.hexviewer.ApplicationCtx;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * File functions.
 * </p>
 *
 * @author Keidan
 *
 * License: GPLv3
 *
 * ******************************************************************************
 */
public class FileHelper {

  /**
   * Prepares the intent for the directory opening mode.
   *
   * @return Intent
   */
  public static Intent prepareForOpenDirectory() {
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
    intent.addCategory(Intent.CATEGORY_DEFAULT);
    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    intent.addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
    return intent;
  }

  /**
   * Prepares the intent for the file opening mode.
   *
   * @return Intent
   */
  public static Intent prepareForOpenFile() {
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
    intent.setType("*/*");
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    intent.addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    return intent;
  }

  /**
   * Retrieving the permissions associated with a Uri
   *
   * @param c       Android content.
   * @param uri     Uri
   * @param fromDir From dir ?
   * @return False if permission is not granted for this Uri.
   */
  public static boolean takeUriPermissions(final Context c, final Uri uri, boolean fromDir) {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
      return true;
    boolean success = false;
    try {
      final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
      c.getContentResolver().takePersistableUriPermission(uri, takeFlags);
      if (!fromDir) {
        Uri dir = getParentUri(uri);
        if (!hasUriPermission(c, dir, false))
          try {
            c.getContentResolver().takePersistableUriPermission(dir, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
          } catch (Exception e) {
            Log.e(SysHelper.class.getSimpleName(), "Exception: " + e.getMessage(), e);
          }
      }
      success = true;
    } catch (Exception e) {
      Log.e(SysHelper.class.getSimpleName(), "Exception: " + e.getMessage(), e);
    }
    return success;
  }

  /**
   * Releases the permissions associated to a Uri.
   *
   * @param c   Android content.
   * @param uri Uri
   */
  public static void releaseUriPermissions(final Context c, final Uri uri) {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q)
      if (hasUriPermission(c, uri, true))
        try {
          final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
          c.getContentResolver().releasePersistableUriPermission(uri, takeFlags);

          Uri dir = getParentUri(uri);
          final List<UriPermission> list = c.getContentResolver().getPersistedUriPermissions();
          int found = 0;
          for (UriPermission up : list) {
            if (up.getUri().equals(dir)) {
              found++;
            }
          }
          if (found == 1) {
            c.getContentResolver().releasePersistableUriPermission(dir, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
          }
        } catch (Exception e) {
          Log.e(SysHelper.class.getSimpleName(), "Exception: " + e.getMessage(), e);
        }
  }

  /**
   * Test if permission is granted for this Uri.
   *
   * @param c              Android content.
   * @param uri            Uri
   * @param readPermission True = read, false = write.
   * @return False if permission is not granted for this Uri.
   */
  public static boolean hasUriPermission(final Context c, final Uri uri, boolean readPermission) {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
      return true;
    final List<UriPermission> list = c.getContentResolver().getPersistedUriPermissions();
    boolean found = false;
    for (UriPermission up : list) {
      if (up.getUri().equals(uri) && ((up.isReadPermission() && readPermission) || (up.isWritePermission() && !readPermission))) {
        found = true;
        break;
      }
    }
    return found;
  }

  /**
   * Test if the file pointed by the Uri exists or not.
   *
   * @param cr  ContentResolver
   * @param uri Uri
   * @return boolean
   */
  public static boolean isFileExists(final ContentResolver cr, final Uri uri) {
    ParcelFileDescriptor pfd;
    boolean exists = false;
    try {
      pfd = cr.openFileDescriptor(uri, "r");
      if (pfd != null) {
        exists = true;
        pfd.close();
      }
    } catch (Exception e) {
      Log.e(SysHelper.class.getSimpleName(), "Exception: " + e.getMessage(), e);
    }
    return exists;
  }

  /**
   * Returns the size of the file pointed to by the Uri.
   *
   * @param cr  ContentResolver
   * @param uri Uri
   * @return long
   */
  public static long getFileSize(ContentResolver cr, Uri uri) {
    ParcelFileDescriptor pfd = null;
    long size = 0L;
    try {
      pfd = cr.openFileDescriptor(uri, "r");
      if (pfd != null) {
        size = pfd.getStatSize();
        pfd.close();
      }
    } catch (IOException e) {
      Log.e(SysHelper.class.getSimpleName(), "Exception: " + e.getMessage(), e);
    } finally {
      if (pfd != null)
        try {
          pfd.close();
        } catch (IOException e) {
          Log.e(SysHelper.class.getSimpleName(), "Exception: " + e.getMessage(), e);
        }
    }
    return size;
  }

  /**
   * Gets the file name from a Uri.
   *
   * @param uri Uri
   * @return String
   */
  public static String getFileName(final Uri uri) {
    String result = null;
    if (uri.getScheme().equals("content")) {
      try (Cursor cursor = ApplicationCtx.getInstance().getContentResolver().query(uri, null, null, null, null)) {
        if (cursor != null && cursor.moveToFirst()) {
          result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
        }
      } catch (Exception e) {
        Log.e(FileHelper.class.getSimpleName(), "Exception: " + e.getMessage(), e);
      }
    }
    if (result == null) {
      result = uri.getPath();
      int cut = result.lastIndexOf('/');
      if (cut != -1) {
        result = result.substring(cut + 1);
      }
    }
    return result;
  }

  /**
   * Gets the parent from a Uri.
   *
   * @param uri Uri
   * @return String
   */
  public static Uri getParentUri(final Uri uri) {
    final String filename = getFileName(uri);
    final String encoded = uri.getEncodedPath();
    String parent = encoded.substring(0, encoded.length() - filename.length());
    if (parent.endsWith("%2F"))
      parent = parent.substring(0, parent.length() - 3);
    String path;
    final String documentPrimary = "/document/primary%3A";
    if (parent.startsWith(documentPrimary))
      path = "/tree/primary%3A" + parent.substring(documentPrimary.length());
    else
      path = parent;
    return Uri.parse(uri.getScheme() + "://" + uri.getHost() + path);
  }
}
