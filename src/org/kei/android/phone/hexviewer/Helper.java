package org.kei.android.phone.hexviewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kei.android.atk.utils.Tools;
import org.kei.android.atk.view.IThemeActivity;
import org.kei.android.atk.view.chooser.FileChooser;
import org.kei.android.atk.view.chooser.FileChooserActivity;

import android.app.Activity;

/**
 *******************************************************************************
 * @file Helper.java
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
public class Helper {
  public static final int MAX_BY_ROW = 16;
  
  public static <T extends Activity & IThemeActivity> void actionOpen(
      final T activity) {
    final Map<String, String> extra = new HashMap<String, String>();
    extra.put(FileChooser.FILECHOOSER_TYPE_KEY, ""
        + FileChooser.FILECHOOSER_TYPE_FILE_ONLY);
    extra.put(FileChooser.FILECHOOSER_TITLE_KEY, "Open");
    extra.put(FileChooser.FILECHOOSER_MESSAGE_KEY, "Use this file:? ");
    extra.put(FileChooser.FILECHOOSER_SHOW_KEY, ""
        + FileChooser.FILECHOOSER_SHOW_FILE_AND_DIRECTORY);
    Tools.switchToForResult(activity, FileChooserActivity.class, extra,
        FileChooserActivity.FILECHOOSER_SELECTION_TYPE_FILE);
  }

  public static <T extends Activity & IThemeActivity> void actionSave(
      final T activity) {
    final Map<String, String> extra = new HashMap<String, String>();
    extra.put(FileChooser.FILECHOOSER_TYPE_KEY, ""
        + FileChooser.FILECHOOSER_TYPE_DIRECTORY_ONLY);
    extra.put(FileChooser.FILECHOOSER_TITLE_KEY, "Save");
    extra.put(FileChooser.FILECHOOSER_MESSAGE_KEY, "Use this folder:? ");
    extra.put(FileChooser.FILECHOOSER_SHOW_KEY, ""
        + FileChooser.FILECHOOSER_SHOW_DIRECTORY_ONLY);
    Tools.switchToForResult(activity, FileChooserActivity.class, extra,
        FileChooserActivity.FILECHOOSER_SELECTION_TYPE_DIRECTORY);
  }

  public static String basename(final String path) {
    String s = path;
    final int i = s.lastIndexOf('/');
    if (i != -1)
      s = s.substring(i + 1);
    return s;
  }

  public static byte[] hexStringToByteArray(final String s) {
    final int len = s.length();
    final byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
          .digit(s.charAt(i + 1), 16));
    }
    return data;
  }

  public static List<String> formatBuffer(final byte[] buffer) {
    final int max = MAX_BY_ROW;
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
