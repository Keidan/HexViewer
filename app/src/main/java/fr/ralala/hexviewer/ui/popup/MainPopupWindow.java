package fr.ralala.hexviewer.ui.popup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.undoredo.UnDoRedo;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * PopupWindow used with MainActivity.
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class MainPopupWindow {
  private final PopupWindow mPopup;
  private final TextView mGoTo;
  private final TextView mSaveMenu;
  private final TextView mSaveAsMenu;
  private final TextView mCloseMenu;
  private final TextView mRecentlyOpen;
  private final PopupCheckboxHelper mPlainText;
  private final PopupCheckboxHelper mLineNumbers;

  public interface ClickListener {
    void onClick(int id);
  }

  public MainPopupWindow(final Context ctx, UnDoRedo undoRedo, ClickListener clickListener) {
    ApplicationCtx app = ApplicationCtx.getInstance();

    LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.main_popup, null);
    popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
    int with = popupView.getMeasuredWidth();

    mPopup = new PopupWindow(popupView,
        with + 150,
        WindowManager.LayoutParams.WRAP_CONTENT, true);

    mPopup.setElevation(5.0f);
    mPopup.setOutsideTouchable(true);

    mPlainText = new PopupCheckboxHelper(popupView,
        R.id.action_plain_text_container,
        R.id.action_plain_text_tv,
        R.id.action_plain_text_cb);

    mLineNumbers = new PopupCheckboxHelper(popupView,
        R.id.action_line_numbers_container,
        R.id.action_line_numbers_tv,
        R.id.action_line_numbers_cb);
    mSaveAsMenu = popupView.findViewById(R.id.action_save_as);
    mSaveMenu = popupView.findViewById(R.id.action_save);
    mCloseMenu = popupView.findViewById(R.id.action_close);
    mRecentlyOpen = popupView.findViewById(R.id.action_recently_open);
    ImageView actionRedo = popupView.findViewById(R.id.action_redo);
    ImageView actionUndo = popupView.findViewById(R.id.action_undo);
    FrameLayout containerRedo = popupView.findViewById(R.id.containerRedo);
    FrameLayout containerUndo = popupView.findViewById(R.id.containerUndo);
    mGoTo = popupView.findViewById(R.id.action_go_to);

    View.OnClickListener click = (v) -> {
      mPopup.dismiss();
      if (clickListener != null)
        clickListener.onClick(v.getId());
    };
    popupView.findViewById(R.id.action_open).setOnClickListener(click);
    popupView.findViewById(R.id.action_settings).setOnClickListener(click);
    mPlainText.setOnClickListener(click);
    mLineNumbers.setOnClickListener(click);
    mSaveAsMenu.setOnClickListener(click);
    mSaveMenu.setOnClickListener(click);
    mCloseMenu.setOnClickListener(click);
    mRecentlyOpen.setOnClickListener(click);
    mGoTo.setOnClickListener(click);
    actionRedo.setOnClickListener(click);
    actionUndo.setOnClickListener(click);
    undoRedo.setControls(containerUndo, actionUndo, containerRedo, actionRedo);
    mLineNumbers.setChecked(app.isLineNumber());

    refreshGoToName();
  }

  /**
   * Shows the popup
   *
   * @param more Button "more"
   */
  public void show(View more) {
    if (mPopup != null) {

      mPopup.showAtLocation(more, Gravity.TOP | Gravity.END, 12, 120);
      //mPopup.showAsDropDown(findViewById(R.id.action_more));
    }
  }

  /**
   * Dismiss the popup.
   */
  public void dismiss() {
    if (mPopup != null && mPopup.isShowing())
      mPopup.dismiss();
  }

  /**
   * Returns the menu RecentlyOpen
   *
   * @return MenuItem
   */
  public TextView getMenuRecentlyOpen() {
    return mRecentlyOpen;
  }

  /**
   * Enables/Disables the save as menu.
   *
   * @param en boolean
   */
  public void setMenusEnable(boolean en) {
    setMenuEnabled(mGoTo, en);
    setMenuEnabled(mSaveAsMenu, en);
    setMenuEnabled(mCloseMenu, en);
    setMenuEnabled(mRecentlyOpen, !ApplicationCtx.getInstance().getRecentlyOpened().isEmpty());
    if (mLineNumbers != null)
      mLineNumbers.setEnable(en);
    if (!en && mPlainText != null) {
      mPlainText.setChecked(false);
      mPlainText.setEnable(false);
    }
  }

  /**
   * Refreshes go to menu.
   */
  public void refreshGoToName() {
    if (mGoTo != null) {
      if(mLineNumbers != null && mLineNumbers.isChecked())
        mGoTo.setText(R.string.action_go_to_address);
      else
        mGoTo.setText(R.string.action_go_to_line);
    }
  }

  /**
   * Enables/Disables the save menu.
   *
   * @param en boolean
   */
  public void setSaveMenuEnable(boolean en) {
    setMenuEnabled(mSaveMenu, en);
  }

  /**
   * Sets whether the menu item is enabled.
   *
   * @param menu    MenuItem
   * @param enabled If true then the item will be invokable; if false it is won't be invokable.
   */
  private void setMenuEnabled(final TextView menu, final boolean enabled) {
    if (menu != null)
      menu.setEnabled(enabled);
  }

  /**
   * Returns PopupCheckboxHelper for plain text.
   *
   * @return PopupCheckboxHelper
   */
  public PopupCheckboxHelper getPlainText() {
    return mPlainText;
  }

  /**
   * Returns PopupCheckboxHelper for line numbers.
   *
   * @return PopupCheckboxHelper
   */
  public PopupCheckboxHelper getLineNumbers() {
    return mLineNumbers;
  }
}
