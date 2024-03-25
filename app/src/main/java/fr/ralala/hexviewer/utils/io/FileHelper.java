package fr.ralala.hexviewer.utils.io;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.utils.SysHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * File functions.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class FileHelper {
  private static final String FILE_HELPER_TAG = "FileHelper";
  private static final String EXCEPTION_TAG = "Exception: ";
  private static final String BUCKET_ID = "?bucketId=";

  private FileHelper() {
  }

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
    ApplicationCtx.addLog(c, FILE_HELPER_TAG,
      String.format(Locale.US, "take Uri permissions file '%s', fromDir: %b",
        uri, fromDir));
    boolean success = false;
    try {
      final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
      c.getContentResolver().takePersistableUriPermission(uri, takeFlags);
      if (!fromDir) {
        takUriPermissionsForDir(c, uri);
      }
      success = true;
    } catch (Exception e) {
      Log.e(SysHelper.class.getSimpleName(), EXCEPTION_TAG + e.getMessage(), e);
      ApplicationCtx.addLog(c, FILE_HELPER_TAG,
        String.format(Locale.US, "Exception: '%s'", e.getMessage()));
    }
    return success;
  }

  private static void takUriPermissionsForDir(final Context c, final Uri uri) {
    Uri dir = getParentUri(c, uri);
    if (!hasUriPermission(c, dir, false))
      try {
        ApplicationCtx.addLog(c, FILE_HELPER_TAG,
          String.format(Locale.US, "take Uri permissions dir '%s'", uri));
        c.getContentResolver().takePersistableUriPermission(dir, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
      } catch (Exception e) {
        Log.e(SysHelper.class.getSimpleName(), EXCEPTION_TAG + e.getMessage(), e);
        ApplicationCtx.addLog(c, FILE_HELPER_TAG,
          String.format(Locale.US, "Dir Exception: '%s'", e.getMessage()));
      }
  }

  /**
   * Releases the permissions associated to a Uri.
   *
   * @param c   Android content.
   * @param uri Uri
   */
  public static void releaseUriPermissions(final Context c, final Uri uri) {
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q && hasUriPermission(c, uri, true))
      try {
        final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
        c.getContentResolver().releasePersistableUriPermission(uri, takeFlags);

        Uri dir = getParentUri(c, uri);
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
        Log.e(SysHelper.class.getSimpleName(), EXCEPTION_TAG + e.getMessage(), e);
        ApplicationCtx.addLog(c, FILE_HELPER_TAG,
          String.format(Locale.US, "Release Uri permissions exception: '%s'", e.getMessage()));
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
    ApplicationCtx.addLog(c, FILE_HELPER_TAG,
      String.format(Locale.US, "hasUriPermission: %b", found));
    return found;
  }

  /**
   * Test if the file pointed by the Uri exists or not.
   *
   * @param ctx Android context.
   * @param cr  ContentResolver
   * @param uri Uri
   * @return boolean
   */
  public static boolean isFileExists(Context ctx, final ContentResolver cr, final Uri uri) {
    ParcelFileDescriptor pfd;
    boolean exists = false;
    ApplicationCtx.addLog(ctx, FILE_HELPER_TAG,
      String.format(Locale.US, "File exists for uri: '%s'", uri));
    try {
      pfd = cr.openFileDescriptor(uri, "r");
      pfd.close();
      exists = true;
    } catch (Exception e) {
      Log.e(SysHelper.class.getSimpleName(), EXCEPTION_TAG + e.getMessage(), e);
      ApplicationCtx.addLog(ctx, FILE_HELPER_TAG,
        String.format(Locale.US, "File exists exception: '%s'", e.getMessage()));
    }
    ApplicationCtx.addLog(ctx, FILE_HELPER_TAG,
      String.format(Locale.US, "File exists: %b", exists));
    return exists;
  }

  /**
   * Returns the size of the file pointed to by the Uri.
   *
   * @param ctx Android context.
   * @param cr  ContentResolver
   * @param uri Uri
   * @return long (-1 = FileNotFoundException, -2 = other errors)
   */
  public static long getFileSize(Context ctx, ContentResolver cr, Uri uri) {
    ParcelFileDescriptor pfd = null;
    ApplicationCtx.addLog(ctx, FILE_HELPER_TAG,
      String.format(Locale.US, "Get file size for uri: '%s'", uri));
    long size;
    try {
      pfd = cr.openFileDescriptor(uri, "r");
      long sz = pfd.getStatSize();
      pfd.close();
      size = sz;
    } catch (Exception e) {
      Log.e(SysHelper.class.getSimpleName(), EXCEPTION_TAG + e.getMessage()/*, e*/);
      ApplicationCtx.addLog(ctx, FILE_HELPER_TAG,
        String.format(Locale.US, "Get file size exception0: '%s'", e.getMessage()));
      size = e instanceof FileNotFoundException ? -1 : -2;
    } finally {
      if (pfd != null)
        try {
          pfd.close();
        } catch (IOException e) {
          Log.e(SysHelper.class.getSimpleName(), EXCEPTION_TAG + e.getMessage()/*, e*/);
          ApplicationCtx.addLog(ctx, FILE_HELPER_TAG,
            String.format(Locale.US, "Get file size exception1: '%s'", e.getMessage()));
        }
    }
    ApplicationCtx.addLog(ctx, FILE_HELPER_TAG,
      String.format(Locale.US, "Get file size: '%d'", size));
    return size;
  }

  /**
   * Gets the file name from a Uri.
   *
   * @param ctx Android context.
   * @param uri Uri
   * @return String
   */
  public static String getFileName(final Context ctx, final Uri uri) {
    String result = null;
    ApplicationCtx.addLog(ctx, FILE_HELPER_TAG,
      String.format(Locale.US, "Get filename for uri: '%s'", uri));
    if (uri.getScheme().equals("content")) {
      ApplicationCtx.addLog(ctx, FILE_HELPER_TAG, "Uri scheme equals to content");
      try (Cursor cursor = ctx.getContentResolver().query(uri, null, null, null, null)) {
        if (cursor != null && cursor.moveToFirst()) {
          int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
          ApplicationCtx.addLog(ctx, FILE_HELPER_TAG,
            String.format(Locale.US, "Filename DISPLAY_NAME cursor index: '%d'", index));
          if (index >= 0) {
            result = cursor.getString(index);
            ApplicationCtx.addLog(ctx, FILE_HELPER_TAG,
              String.format(Locale.US, "Filename DISPLAY_NAME cursor value: '%s'", result));
          }
        }
      } catch (Exception e) {
        Log.e(FileHelper.class.getSimpleName(), EXCEPTION_TAG + e.getMessage()/*, e*/);
        ApplicationCtx.addLog(ctx, FILE_HELPER_TAG,
          String.format(Locale.US, "Get file name exception: '%s'", e.getMessage()));
      }
    }
    if (result == null) {
      result = extractName(getPathFromBucket(ctx, uri));
    }

    if (result == null) {
      result = extractName(uri.getPath());
    }
    ApplicationCtx.addLog(ctx, FILE_HELPER_TAG,
      String.format(Locale.US, "Filename result: '%s'", result));
    return result;
  }

  public static Uri adjustUri(final Context ctx, final Uri uri) {
    String path = getPathFromBucket(ctx, uri);
    if (path == null)
      return uri;
    return Uri.fromFile(new File(path));
  }

  /**
   * File name extraction.
   *
   * @param name Full name.
   * @return File name.
   */
  private static String extractName(final String name) {
    String result = name;
    if (result != null) {
      int cut = result.lastIndexOf('/');
      if (cut != -1) {
        result = result.substring(cut + 1);
      }
    }
    return result;
  }

  /**
   * Extracting the path from a URI with bucket id.
   *
   * @param ctx Android context.
   * @param uri Uri
   * @return String
   */
  private static String getPathFromBucket(final Context ctx, final Uri uri) {
    String result = null;
    int bucketId = uri.toString().indexOf(BUCKET_ID);
    if (bucketId != -1) {
      String bid = uri.toString().substring(bucketId + BUCKET_ID.length());
      ApplicationCtx.addLog(ctx, FILE_HELPER_TAG, "Bucket id mode: " + bid);
      final String[] projectionBucket = {
        MediaStore.MediaColumns.BUCKET_ID,
        MediaStore.MediaColumns.DATA};
      final String groupBy = "1) GROUP BY 1,(2";
      final String orderBy = "MAX(" + MediaStore.MediaColumns.DATE_TAKEN + ") DESC";
      try (Cursor cursor = ctx.getContentResolver().query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projectionBucket, groupBy, null, orderBy)) {
        if (cursor != null && cursor.moveToFirst()) {
          int dataColumn = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
          int idColumn = cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_ID);
          do {
            String data = cursor.getString(dataColumn);
            String id = cursor.getString(idColumn);
            ApplicationCtx.addLog(ctx, FILE_HELPER_TAG,
              String.format(Locale.US, "Bucket data '%s' id: %s", data, id));
            if (id.equals(bid)) {
              result = data;
              break;
            }
          } while (cursor.moveToNext());
        }
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
  public static Uri getParentUri(final Context ctx, final Uri uri) {
    final String filename = getFileName(ctx, uri);
    final String encoded = uri.getEncodedPath();
    final int filenameLen = (filename == null ? 0 : filename.length());
    final int length = encoded.length() - filenameLen;
    ApplicationCtx.addLog(ctx, FILE_HELPER_TAG,
      String.format(Locale.US, "Search for parent uri: '%s'", uri));
    String path;
    if (length > 0 && length < encoded.length()) {
      String parent = encoded.substring(0, encoded.length() - filenameLen);
      ApplicationCtx.addLog(ctx, FILE_HELPER_TAG,
        String.format(Locale.US, "Parent raw: '%s'", parent));
      if (parent.endsWith("%2F")) {
        parent = parent.substring(0, parent.length() - 3);
        ApplicationCtx.addLog(ctx, FILE_HELPER_TAG,
          String.format(Locale.US, "Parent after fix: '%s'", parent));
      }
      final String documentPrimary = "/document/primary%3A";
      ApplicationCtx.addLog(ctx, FILE_HELPER_TAG,
        String.format(Locale.US, "Document primary: '%s'", documentPrimary));
      if (parent.startsWith(documentPrimary)) {
        path = "/tree/primary%3A" + parent.substring(documentPrimary.length());
        ApplicationCtx.addLog(ctx, FILE_HELPER_TAG,
          String.format(Locale.US, "Document primary fixed: '%s'", path));
      } else {
        path = parent;
        ApplicationCtx.addLog(ctx, FILE_HELPER_TAG,
          String.format(Locale.US, "Parent without document primary '%s'", path));
      }
    } else {
      path = encoded;
      ApplicationCtx.addLog(ctx, FILE_HELPER_TAG,
        String.format(Locale.US, "Path encoded: '%s'", path));
    }
    Uri u = Uri.parse(uri.getScheme() + "://" + uri.getHost() + path);
    ApplicationCtx.addLog(ctx, FILE_HELPER_TAG,
      String.format(Locale.US, "Final parent uri: '%s'", u));
    return u;
  }
}
