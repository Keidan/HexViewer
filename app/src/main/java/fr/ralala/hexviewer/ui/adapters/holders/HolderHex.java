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
public class HolderHex {
  private AppCompatTextView mLineNumbers;
  private AppCompatTextView mContent;

  public void setLineNumbers(AppCompatTextView tv) {
    mLineNumbers = tv;
  }

  public void setContent(AppCompatTextView tv) {
    mContent = tv;
  }

  public AppCompatTextView getLineNumbers() {
    return mLineNumbers;
  }

  public AppCompatTextView getContent() {
    return mContent;
  }
}
