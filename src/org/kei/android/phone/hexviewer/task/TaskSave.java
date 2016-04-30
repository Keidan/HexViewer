package org.kei.android.phone.hexviewer.task;

import java.io.FileOutputStream;
import java.io.IOException;

import org.kei.android.atk.utils.Tools;
import org.kei.android.phone.hexviewer.ApplicationCtx;
import org.kei.android.phone.hexviewer.R;

import android.app.Activity;

/**
 *******************************************************************************
 * @file TaskSave.java
 * @author Keidan
 * @date 23/04/2016
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
public class TaskSave extends ProgressTask<Void, Void, Void> {
  private FileOutputStream fos = null;
  
  public TaskSave(final Activity activity) {
    super(activity);
  }
  
  @Override
  protected void onPostExecute(final Void empty) {
    super.onPostExecute(empty);
    Tools.toast(activity, R.drawable.ic_launcher, "Save success.");
  }
  
  protected void onCancelled() {
    super.onCancelled();
    if (fos != null) {
      try {
        fos.close();
      } catch (final IOException e) {
      }
      fos = null;
    }
  }

  @Override
  protected Void doInBackground(final Void... empty) {
    final ApplicationCtx actx = (ApplicationCtx) activity.getApplication();
    final byte[] bytes = actx.getPayload().toByteArray();
    try {
      fos = new FileOutputStream(actx.getFilename());
      fos.write(bytes);
      fos.flush();
    } catch (final Exception e) {
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          final String m = e.getMessage();
          Tools.toast(activity, R.drawable.ic_launcher, "Exception: "
              + (m == null ? e.toString() : m));
        }
      });
    } finally {
      if (fos != null) {
        try {
          fos.close();
        } catch (final IOException e) {
        }
        fos = null;
      }
    }
    return null;
  }
}
