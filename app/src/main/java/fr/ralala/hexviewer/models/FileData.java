package fr.ralala.hexviewer.models;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import java.util.Locale;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.utils.io.FileHelper;

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
  protected static final String SEQUENTIAL_SEP = "^";
  private final String mName;
  private final Uri mUri;
  private boolean mOpenFromAppIntent;
  private long mStartOffset;
  private long mEndOffset;
  private long mSize;
  private long mRealSize;
  private boolean mIsNotFound;
  private final boolean mIsAccessError;
  private int mShiftOffset;

  public FileData(final Context ctx, final Uri uri, boolean openFromAppIntent) {
    this(ctx, uri, openFromAppIntent, 0L, 0L);
  }

  public FileData(final Context ctx, final Uri uri, boolean openFromAppIntent, long startOffset, long endOffset) {
    mShiftOffset = 0;
    mName = FileHelper.getFileName(ctx, uri);
    mUri = FileHelper.adjustUri(ctx, uri);
    mStartOffset = startOffset;
    mEndOffset = endOffset;
    mOpenFromAppIntent = openFromAppIntent;
    mRealSize = FileHelper.getFileSize(ctx, ctx.getContentResolver(), mUri);
    if (isSequential())
      mSize = Math.abs(mEndOffset - mStartOffset);
    else
      mSize = mRealSize;

    /* We assume that if the file sizes are not <= 0, the file exists. */
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q && (mSize <= 0 || mRealSize <= 0)) {
      DocumentFile sourceFile = DocumentFile.fromSingleUri(ctx, mUri);
      mIsNotFound = (sourceFile == null || !sourceFile.exists());
    }
    if (mIsNotFound) {
      mIsAccessError = false;
      mRealSize = 0;
      mSize = 0;
    } else {
      if (mRealSize == -1) {
        mIsAccessError = false;
        mIsNotFound = true;
        mRealSize = 0;
        mSize = 0;
      } else if (mRealSize == -2) {
        mIsAccessError = true;
        mIsNotFound = false;
        mRealSize = 0;
        mSize = 0;
      } else {
        mIsAccessError = false;
        mIsNotFound = false;
      }
    }
    ApplicationCtx.addLog(ctx, "FileData", String.format(Locale.US,
      "%s size: %d, r_size: %d, s_off: %d, e_off: %d, shift: %d, fromIntent: %b, notFound: %b, accessError: %b",
      mName, mSize, mRealSize, mStartOffset, mEndOffset, mShiftOffset, mOpenFromAppIntent, mIsNotFound, mIsAccessError));
  }

  /**
   * Sets the offset used to shift the text to the end of the line.
   *
   * @param shiftOffset The new value.
   */
  public void setShiftOffset(int shiftOffset) {
    mShiftOffset = shiftOffset;
  }

  /**
   * Returns the offset used to shift the text to the end of the line.
   *
   * @return int
   */
  public int getShiftOffset() {
    return mShiftOffset;
  }

  /**
   * Returns true if a file access error was raised.
   *
   * @return boolean
   */
  public boolean isAccessError() {
    return mIsAccessError;
  }

  /**
   * Returns true if the file was not found.
   *
   * @return boolean
   */
  public boolean isNotFound() {
    return mIsNotFound;
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

  @NonNull
  public String toString() {
    String ret = mStartOffset + SEQUENTIAL_SEP + mEndOffset + SEQUENTIAL_SEP;
    ret += mUri.toString();
    return ret;
  }

  /**
   * Test if it's a sequential file or not.
   *
   * @return boolean
   */
  public boolean isSequential() {
    return mStartOffset != 0L || mEndOffset != 0L;
  }

  /**
   * Returns the file uri.
   *
   * @return Uri
   */
  public Uri getUri() {
    return mUri;
  }

  /**
   * Returns the start offset.
   *
   * @return long
   */
  public long getStartOffset() {
    return mStartOffset;
  }

  /**
   * Returns the end offset.
   *
   * @return long
   */
  public long getEndOffset() {
    return mEndOffset;
  }

  /**
   * Sets the start offset.
   *
   * @param startOffset Start offset
   * @param endOffset   End offset
   * @param refreshSize Refresh the size?
   */
  public void setOffsets(long startOffset, long endOffset, boolean refreshSize) {
    mStartOffset = startOffset;
    mEndOffset = endOffset;
    if (refreshSize)
      mSize = Math.abs(mEndOffset - mStartOffset);
  }

  /**
   * Returns the file size.
   *
   * @return long
   */
  public long getSize() {
    return mSize;
  }

  /**
   * Returns the real file size.
   *
   * @return long
   */
  public long getRealSize() {
    return mRealSize;
  }
}
