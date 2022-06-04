package fr.ralala.hexviewer.models;

import android.content.Context;

import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.utils.SysHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Uri data representation (used by RecentlyOpenRecyclerAdapter)
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class UriData {
  public final Context mCtx;
  public final FileData mFd;
  public final int mIndex;
  public final int mMaxLength;
  public String mDetail;
  public boolean mError;
  public boolean mClickable;
  public boolean mSizeChanged;

  public UriData(final Context ctx, int index, int maxLength, FileData fd) {
    mCtx = ctx;
    mMaxLength = maxLength;
    mIndex = index;
    mFd = fd;
    mSizeChanged = false;
    String labelSize = ctx.getString(R.string.size) + ": ";
    String labelStart = ctx.getString(R.string.start_offset) + " ";
    String labelEnd = ctx.getString(R.string.end_offset) + " ";
    if (fd.isNotFound()) {
      mDetail = ctx.getString(R.string.error_no_file);
      mError = true;
      mClickable = false;
    } else if (fd.isAccessError()) {
      mDetail = ctx.getString(R.string.error_no_file_access);
      mError = true;
      mClickable = false;
    } else {
      long size = fd.getSize();
      String detail;
      if (fd.isSequential()) {
        if(fd.getEndOffset() > fd.getRealSize()) {
          mDetail = ctx.getString(R.string.error_size_changed);
          mError = true;
          mClickable = true;
          mSizeChanged = true;
          return;
        }
        else {
          detail = labelStart + SysHelper.sizeToHuman(ctx, fd.getStartOffset(), true, true) + ", ";
          detail += labelEnd + SysHelper.sizeToHuman(ctx, fd.getEndOffset(), true, true) + ", ";
          detail += labelSize + SysHelper.sizeToHuman(ctx, Math.abs(fd.getEndOffset() - fd.getStartOffset()));
        }
      } else
        detail = labelSize + SysHelper.sizeToHuman(ctx, size);
      mDetail = detail;
      mError = false;
      mClickable = true;
    }
  }

  public Context getCtx() {
    return mCtx;
  }

  public FileData getFd() {
    return mFd;
  }

  public int getIndex() {
    return mIndex;
  }

  public int getMaxLength() {
    return mMaxLength;
  }

  public String getDetail() {
    return mDetail;
  }

  public boolean isError() {
    return mError;
  }

  public void setError(boolean mError) {
    this.mError = mError;
  }

  public boolean isClickable() {
    return mClickable;
  }

  public boolean isSizeChanged() {
    return mSizeChanged;
  }
}
