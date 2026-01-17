package fr.ralala.hexviewer.ui.adapters.holders;

import androidx.appcompat.widget.AppCompatTextView;

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
  private AppCompatTextView mTitleLineNumbers;
  private AppCompatTextView mTitleContent;

  public AppCompatTextView getTitleLineNumbers() {
    return mTitleLineNumbers;
  }

  public void setTitleLineNumbers(AppCompatTextView mTitleLineNumbers) {
    this.mTitleLineNumbers = mTitleLineNumbers;
  }

  public AppCompatTextView getTitleContent() {
    return mTitleContent;
  }

  public void setTitleContent(AppCompatTextView mTitleContent) {
    this.mTitleContent = mTitleContent;
  }
}
