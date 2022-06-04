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
public class LineNumbersTitle {
  private TextView mTitleLineNumbers;
  private TextView mTitleContent;

  public TextView getTitleLineNumbers() {
    return mTitleLineNumbers;
  }

  public void setTitleLineNumbers(TextView mTitleLineNumbers) {
    this.mTitleLineNumbers = mTitleLineNumbers;
  }

  public TextView getTitleContent() {
    return mTitleContent;
  }

  public void setTitleContent(TextView mTitleContent) {
    this.mTitleContent = mTitleContent;
  }
}
