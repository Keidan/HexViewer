package fr.ralala.hexviewer.ui.adapters.holders;

import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import fr.ralala.hexviewer.R;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Holder used by the recycler view (recently open) adapter.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class HolderRecently extends RecyclerView.ViewHolder {
  private AppCompatTextView mIndex;
  private final AppCompatTextView mDetail;
  private AppCompatTextView mName;

  public HolderRecently(View view) {
    super(view);
    mIndex = view.findViewById(R.id.index);
    mDetail = view.findViewById(R.id.detail);
    mName = view.findViewById(R.id.name);
  }

  public AppCompatTextView getIndex() {
    return mIndex;
  }

  public void setIndex(AppCompatTextView mIndex) {
    this.mIndex = mIndex;
  }

  public AppCompatTextView getDetail() {
    return mDetail;
  }

  public AppCompatTextView getName() {
    return mName;
  }

  public void setName(AppCompatTextView mName) {
    this.mName = mName;
  }
}