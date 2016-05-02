package org.kei.android.phone.hexviewer.task;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.kei.android.atk.utils.Tools;
import org.kei.android.phone.hexviewer.ApplicationCtx;
import org.kei.android.phone.hexviewer.Helper;
import org.kei.android.phone.hexviewer.R;

import android.app.Activity;
import android.net.Uri;
import android.widget.ArrayAdapter;

/**
 *******************************************************************************
 * @file TaskOpen.java
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
public class TaskOpen extends ProgressTask<Uri, Void, List<String>> {
  private ArrayAdapter<String> adapter = null;
  
  public TaskOpen(final Activity activity, final ArrayAdapter<String> adapter) {
    super(activity);
    this.adapter = adapter;
  }
  
  @Override
  protected void onPreExecute() {
    super.onPreExecute();
    adapter.clear();
  }
  
  @Override
  protected void onPostExecute(final List<String> result) {
    adapter.addAll(result);
    super.onPostExecute(result);
  }

  @Override
  protected List<String> doInBackground(final Uri... uris) {
    InputStream is = null;
    try {
      final ApplicationCtx actx = (ApplicationCtx) activity.getApplication();
      if (uris.length != 0 && uris[0] != null)
        is = activity.getContentResolver().openInputStream(uris[0]);
      else
        is = new FileInputStream(actx.getFilename());
      final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      buffer.reset();
      int nRead;
      final byte[] data = new byte[1024];
      while ((nRead = is.read(data, 0, data.length)) != -1 && !isCancelled())
        buffer.write(data, 0, nRead);
      buffer.flush();
      ((ApplicationCtx) activity.getApplication()).setPayload(buffer
          .toByteArray());
      return Helper.formatBuffer(((ApplicationCtx) activity.getApplication())
          .toPayload());
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
      if (is != null)
        try {
          is.close();
        } catch (final IOException e) {
        }
    }
    return null;
  }

}
