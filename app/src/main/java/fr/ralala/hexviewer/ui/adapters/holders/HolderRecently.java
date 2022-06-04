package fr.ralala.hexviewer.ui.adapters.holders;

import android.view.View;
import android.widget.TextView;

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
  private TextView mIndex;
  private TextView mDetail;
  private TextView mName;

  public HolderRecently(View view) {
    super(view);
    mIndex = view.findViewById(R.id.index);
    mDetail = view.findViewById(R.id.detail);
    mName = view.findViewById(R.id.name);
  }

  public TextView getIndex() {
    return mIndex;
  }

  public void setIndex(TextView mIndex) {
    this.mIndex = mIndex;
  }

  public TextView getDetail() {
    return mDetail;
  }

  public void setDetail(TextView mDetail) {
    this.mDetail = mDetail;
  }

  public TextView getName() {
    return mName;
  }

  public void setName(TextView mName) {
    this.mName = mName;
  }
}