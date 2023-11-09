package fr.ralala.hexviewer.ui.adapters;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.models.UriData;
import fr.ralala.hexviewer.ui.adapters.holders.HolderRecently;
import fr.ralala.hexviewer.utils.SysHelper;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Adapter used with the recycler view (recently open).
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class RecentlyOpenRecyclerAdapter extends RecyclerView.Adapter<HolderRecently> {
  private static final int ID = R.layout.recyclerview_recently_open;
  private final List<UriData> mItems;
  private final OnEventListener mListener;
  private final SwipeToDeleteCallback mSwipeToDeleteCallback;
  private final Context mContext;

  public interface OnEventListener {
    /**
     * Called when a click is captured.
     *
     * @param ud The associated item.
     */
    void onClick(@NonNull UriData ud);

    /**
     * Called when a click is captured.
     *
     * @param ud The associated item.
     */
    void onDelete(@NonNull UriData ud);
  }

  public RecentlyOpenRecyclerAdapter(final Context context,
                                     final List<UriData> objects,
                                     final OnEventListener listener) {
    mContext = context;
    mItems = objects;
    mListener = listener;
    mSwipeToDeleteCallback = new SwipeToDeleteCallback(context);
  }

  /**
   * Returns the SwipeToDeleteCallback.
   *
   * @return SwipeToDeleteCallback
   */
  public SwipeToDeleteCallback getSwipeToDeleteCallback() {
    return mSwipeToDeleteCallback;
  }

  /**
   * Called when the view is created.
   *
   * @param viewGroup The view group.
   * @param i         The position
   * @return ViewHolder
   */
  @Override
  public @NonNull
  HolderRecently onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
    View view = LayoutInflater.from(viewGroup.getContext()).inflate(ID, viewGroup, false);
    return new HolderRecently(view);
  }

  /**
   * Called on Binding the view holder.
   *
   * @param viewHolder The view holder.
   * @param i          The position.
   */
  @Override
  public void onBindViewHolder(@NonNull HolderRecently viewHolder, int i) {
    if (mItems.isEmpty()) return;
    int idx = i;
    if (idx > mItems.size())
      idx = 0;
    final UriData ud = mItems.get(idx);
    if (ud != null) {
      // Set item views based on the views and data model
      TextView name = viewHolder.getName();
      TextView index = viewHolder.getIndex();
      TextView detail = viewHolder.getDetail();
      if (mListener != null) {
        final View.OnClickListener l = v -> {
          if (ud.isClickable())
            mListener.onClick(ud);
        };
        name.setOnClickListener(l);
        index.setOnClickListener(l);
        detail.setOnClickListener(l);
      }
      String fmt = "%0" + (String.valueOf(ud.getMaxLength()).length() + 1) + "d - ";
      index.setText(String.format(Locale.US, fmt, ud.getIndex()));
      name.setText(ud.getFd().getName());
      detail.setText(ud.getDetail());
      detail.setTextColor(ContextCompat.getColor(ud.getCtx(), ud.isError() ? R.color.colorResultError : R.color.textColor));
      name.setGravity(SysHelper.isRTL(mContext) ? android.view.Gravity.END : android.view.Gravity.START);
    }
  }

  /**
   * Returns the total count of items in the list.
   *
   * @return int
   */
  @Override
  public int getItemCount() {
    return mItems.size();
  }


  /* -------------------------------------------------------------- */
  class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
    private final ColorDrawable mBackground;


    public SwipeToDeleteCallback(final Context context) {
      super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
      mBackground = new ColorDrawable(ContextCompat.getColor(context, R.color.colorResultError));
    }


    /**
     * Called by ItemTouchHelper on RecyclerView's onDraw callback.
     *
     * @param c                 The canvas which RecyclerView is drawing its children
     * @param recyclerView      The recycler view.
     * @param viewHolder        The ViewHolder which is being interacted by the User or it was interacted and simply animating to its original position
     * @param dX                The amount of horizontal displacement caused by user's action
     * @param dY                The amount of vertical displacement caused by user's action
     * @param actionState       The type of interaction on the View. Is either ACTION_STATE_DRAG or ACTION_STATE_SWIPE.
     * @param isCurrentlyActive True if this view is currently being controlled by the user or false it is simply animating back to its original state.
     */
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
      super.onChildDraw(c, recyclerView, viewHolder, dX,
        dY, actionState, isCurrentlyActive);
      View itemView = viewHolder.itemView;
      int backgroundCornerOffset = 20;

      if (dX > 0) { // Swiping to the right

        mBackground.setBounds(itemView.getLeft(), itemView.getTop(),
          itemView.getLeft() + ((int) dX) + backgroundCornerOffset,
          itemView.getBottom());
      } else if (dX < 0) { // Swiping to the left
        mBackground.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
          itemView.getTop(), itemView.getRight(), itemView.getBottom());
      } else { // view is unSwiped
        mBackground.setBounds(0, 0, 0, 0);
      }

      mBackground.draw(c);
    }

    /**
     * Called when ItemTouchHelper wants to move the dragged item from its old position to the new position.
     *
     * @param recyclerView The RecyclerView to which ItemTouchHelper is attached to.
     * @param viewHolder   The ViewHolder which is being dragged by the user.
     * @param target       The ViewHolder over which the currently active item is being dragged.
     * @return True if the viewHolder has been moved to the adapter position of target.
     */
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
      /* nothing */
      return false;
    }

    /**
     * Called when a ViewHolder is swiped by the user.
     *
     * @param viewHolder The ViewHolder which has been swiped by the user.
     * @param direction  The direction to which the ViewHolder is swiped. It is one of UP, DOWN, LEFT or RIGHT.
     */
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
      final int position = viewHolder.getAdapterPosition();
      UriData ud = mItems.get(position);
      mItems.remove(position);
      notifyItemRemoved(position);
      if (mListener != null)
        mListener.onDelete(ud);
    }
  }

}

