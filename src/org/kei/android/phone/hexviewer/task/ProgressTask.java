package org.kei.android.phone.hexviewer.task;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

/**
 *******************************************************************************
 * @file ProgressTask.java
 * @author Keidan
 * @date 30/04/2016
 * @par Project HexViewer
 *
 * @par Copyright 2016 Keidan, all right reserved
 *
 *      This software is distributed in the hope that it will be useful, but
 *      WITHOUT ANY WARRANTY.
 *
 *      License summary : You can modify and redistribute the sources code and
 *      binaries. You can send me the bug-fix
 *
 *      Term of the license in in the file license.txt.
 *
 *******************************************************************************
 */
public abstract class ProgressTask<Params, Progress, Result> extends
    AsyncTask<Params, Progress, Result> {
  
  protected ProgressDialog dialog   = null;
  protected Activity       activity = null;

  public ProgressTask(final Activity activity) {
    this.activity = activity;
  }
  
  @Override
  protected void onPreExecute() {
    super.onPreExecute();
    dialog = ProgressDialog.show(activity, "", "Please wait");
    dialog.setCancelable(true);
    dialog.show();
  }

  @Override
  protected void onPostExecute(final Result result) {
    dialog.dismiss();
  }

}
