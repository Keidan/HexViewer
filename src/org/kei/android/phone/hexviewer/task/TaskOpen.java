package org.kei.android.phone.hexviewer.task;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.kei.android.atk.utils.Tools;
import org.kei.android.phone.hexviewer.ApplicationCtx;
import org.kei.android.phone.hexviewer.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
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
public class TaskOpen extends AsyncTask<Uri, Void, List<String>> {
  private Activity             activity = null;
  private ArrayAdapter<String> adapter  = null;
  private ProgressDialog       dialog   = null;

  public TaskOpen(final Activity activity, final ArrayAdapter<String> adapter) {
    this.activity = activity;
    this.adapter = adapter;
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();
    dialog = ProgressDialog.show(activity, "", "Please wait");
    adapter.clear();
    dialog.show();
  }

  @Override
  protected void onPostExecute(final List<String> result) {
    adapter.addAll(result);
    dialog.dismiss();
  }
  
  @Override
  protected List<String> doInBackground(final Uri... uris) {
    InputStream is = null;
    try {
      ApplicationCtx actx = (ApplicationCtx)activity.getApplication();
      if(uris.length != 0 && uris[0] != null)
        is = activity.getContentResolver().openInputStream(uris[0]);
      else
        is = new FileInputStream(actx.getFilename());
      ByteArrayOutputStream buffer = ((ApplicationCtx)activity.getApplication()).getPayload();
      buffer.reset();
      int nRead;
      byte[] data = new byte[1024];
      while ((nRead = is.read(data, 0, data.length)) != -1)
        buffer.write(data, 0, nRead);
      buffer.flush();
      return formatBuffer(buffer.toByteArray());
    } catch(final Exception e) {
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          String m = e.getMessage();
          Tools.toast(activity, R.drawable.ic_launcher, 
              "Exception: " + (m == null ? e.toString() : m));
        }
      });
    } finally {
      if(is != null)
        try {
          is.close();
        } catch (IOException e) { }
    }
    return null;
  }
  
  private static List<String> formatBuffer(final byte[] buffer) {
    final int max = 16;
    int length = buffer.length;
    String line = "", eline = "";
    final List<String> lines = new ArrayList<String>();
    int i = 0, j = 0;
    while (length > 0) {
      final byte c = buffer[j++];
      line += String.format("%02x ", c);
      /* only the visibles char */
      if (c >= 0x20 && c <= 0x7e)
        eline += (char) c;
      else
        eline += (char) 0x2e; /* . */
      if (i == max - 1) {
        lines.add(line + " " + eline);
        line = eline = "";
        i = 0;
      } else
        i++;
      /* add a space in the midline */
      if (i == max / 2) {
        line += " ";
        eline += " ";
      }
      length--;
    }
    /* align 'line' */
    if (i != 0 && (i < max || i <= buffer.length)) {
      String off = "";
      while (i++ <= max)
        off += "   "; /* 3 spaces ex: "00 " */
      if (line.endsWith(" "))
        line = line.substring(0, line.length() - 1);
      lines.add(line + off + eline);
    }
    return lines;
  }
}
