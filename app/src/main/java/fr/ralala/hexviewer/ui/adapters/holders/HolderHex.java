package fr.ralala.hexviewer.ui.adapters.holders;

import androidx.appcompat.widget.AppCompatTextView;

import org.jspecify.annotations.NonNull;

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
  private final AppCompatTextView mContent;

  public HolderHex(@NonNull AppCompatTextView content)
  {
    mContent = content;
  }
  public void setLineNumbers(AppCompatTextView tv) {
    mLineNumbers = tv;
  }

  public AppCompatTextView getLineNumbers() {
    return mLineNumbers;
  }

  public @NonNull AppCompatTextView getContent() {
    return mContent;
  }
}
