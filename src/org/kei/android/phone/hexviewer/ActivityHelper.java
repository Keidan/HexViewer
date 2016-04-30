package org.kei.android.phone.hexviewer;

import java.util.HashMap;
import java.util.Map;

import org.kei.android.atk.utils.Tools;
import org.kei.android.atk.view.IThemeActivity;
import org.kei.android.atk.view.chooser.FileChooser;
import org.kei.android.atk.view.chooser.FileChooserActivity;

import android.app.Activity;
import android.util.Log;

/**
 *******************************************************************************
 * @file ActivityHelper.java
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
public class ActivityHelper {
  
  public static <T extends Activity & IThemeActivity> void actionOpen(final T activity) {
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
  
  public static <T extends Activity & IThemeActivity> void actionSave(final T activity) {
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
    Log.e("TAG", "basename:'"+path+"'");
    String s = path;
    int i = s.lastIndexOf('/');
    if(i != -1) s = s.substring(i+1);
    return s;
  }
}
