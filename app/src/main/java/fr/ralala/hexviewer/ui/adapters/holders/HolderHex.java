package fr.ralala.hexviewer.ui.adapters.holders;

import android.widget.TextView;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Holder used by the hex text list view adapter.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class HolderHex {
  private TextView mLineNumbers;
  private TextView mContent;

  public void setLineNumbers(TextView tv) {
    mLineNumbers = tv;
  }

  public void setContent(TextView tv) {
    mContent = tv;
  }

  public TextView getLineNumbers() {
    return mLineNumbers;
  }

  public TextView getContent() {
    return mContent;
  }
}
