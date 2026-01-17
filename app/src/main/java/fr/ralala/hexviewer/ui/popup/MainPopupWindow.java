package fr.ralala.hexviewer.ui.popup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;

import androidx.appcompat.widget.AppCompatTextView;

import fr.ralala.hexviewer.application.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.ui.undoredo.UnDoRedo;
import fr.ralala.hexviewer.ui.utils.UIHelper;
import fr.ralala.hexviewer.utils.system.SysHelper;

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
  private final AppCompatTextView mGoTo;
  private final AppCompatTextView mSaveMenu;
  private final AppCompatTextView mSaveAsMenu;
  private final AppCompatTextView mCloseMenu;
  private final AppCompatTextView mRecentlyOpen;
  private final PopupCheckboxHelper mPlainText;
  private final PopupCheckboxHelper mLineNumbers;
  private final ApplicationCtx mApp;

  public interface ClickListener {
    void onClick(int id);
  }

  public MainPopupWindow(final Context ctx, UnDoRedo undoRedo, ClickListener clickListener) {
    mApp = (ApplicationCtx) ctx.getApplicationContext();

    LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    @SuppressLint("InflateParams") View popupView = inflater.inflate(R.layout.main_popup, null);
    popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
    int width = popupView.getMeasuredWidth();

    mPopup = new PopupWindow(popupView,
      width + 150,
      ViewGroup.LayoutParams.WRAP_CONTENT, true);

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

    View.OnClickListener click = v -> {
      mPopup.dismiss();
      if (clickListener != null)
        clickListener.onClick(v.getId());
    };
    popupView.findViewById(R.id.action_open).setOnClickListener(click);
    popupView.findViewById(R.id.action_open_sequential).setOnClickListener(click);
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
    mLineNumbers.setChecked(mApp.isLineNumber());

    refreshGoToName();
  }

  /**
   * Shows the popup
   *
   * @param more Button "more"
   */
  public void show(Activity activity, View more) {
    if (mPopup != null) {
      UIHelper.hideKeyboard(activity);
      mPopup.showAtLocation(more, Gravity.TOP | (SysHelper.isRTL(more) ? Gravity.START : Gravity.END), 12, 120);
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
  public AppCompatTextView getMenuRecentlyOpen() {
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
    setMenuEnabled(mRecentlyOpen, !mApp.getRecentlyOpened().list().isEmpty());
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
      if (mLineNumbers != null && mLineNumbers.isChecked())
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
  private void setMenuEnabled(final AppCompatTextView menu, final boolean enabled) {
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
