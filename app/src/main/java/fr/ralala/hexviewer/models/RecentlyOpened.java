package fr.ralala.hexviewer.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.ralala.hexviewer.ApplicationCtx;

/**
 * ******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Recently opened (settings)
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * </p>
 * ******************************************************************************
 */
public class RecentlyOpened {
  protected static final String SEQUENTIAL_MASK = "$^#*";
  private List<FileData> mList;
  private final ApplicationCtx mApp;

  public RecentlyOpened(ApplicationCtx app) {
    mApp = app;
    if (!migrate())
      mList = load();
  }

  private boolean migrate() {
    String content = mApp.getPref(mApp).getString(ApplicationCtx.CFG_RECENTLY_OPEN, "");
    if (content.isEmpty() || content.startsWith(SEQUENTIAL_MASK))
      return false;
    final List<FileData> uris = new ArrayList<>();
    String[] split = content.split("\\|");
    if (split.length != 0 && !split[0].equals(""))
      for (String s : split) {
        uris.add(decode(mApp, s));
      }
    setRecentlyOpened(uris);
    mList = uris;
    return true;
  }

  public static FileData decode(final Context ctx, final String str) {
    String[] split = str.split("\\" + FileData.SEQUENTIAL_SEP);
    if (split.length == 1)
      return new FileData(ctx, Uri.parse(split[0]), false);
    else if (split.length == 2) {
      try {
        return new FileData(ctx, Uri.parse(split[1]), false, 0L, Long.parseLong(split[0]));
      } catch (Exception e) {
        Log.e(RecentlyOpened.class.getName(), "RecentlyOpened Exception: " + e.getMessage(), e);
        return new FileData(ctx, Uri.parse(split[1]), false);
      }
    } else {
      try {
        return new FileData(ctx, Uri.parse(split[2]), false, Long.parseLong(split[0]), Long.parseLong(split[1]));
      } catch (Exception e) {
        Log.e(RecentlyOpened.class.getName(), "RecentlyOpened Exception: " + e.getMessage(), e);
        return new FileData(ctx, Uri.parse(split[2]), false);
      }
    }
  }

  /**
   * Adds a new element to the list.
   *
   * @param recent The new element
   */
  public void add(FileData recent) {
    removeElement(recent);
    mList.add(recent);
    setRecentlyOpened(mList);
  }

  /**
   * Removes an existing element from the list.
   *
   * @param recent The new element
   */
  public void remove(FileData recent) {
    removeElement(recent);
    setRecentlyOpened(mList);
  }

  private void removeElement(FileData recent) {
    for (Iterator<FileData> iterator = mList.iterator(); iterator.hasNext(); ) {
      FileData fd = iterator.next();
      if (fd.toString().equals(recent.toString())) {
        iterator.remove();
        break;
      }
    }
  }

  /**
   * Returns the list of recently opened files.
   *
   * @return List<FileData>
   */
  public List<FileData> list() {
    return mList;
  }

  /**
   * Sets the list of recently opened files.
   *
   * @param list The list
   */
  private void setRecentlyOpened(List<FileData> list) {
    StringBuilder sb = new StringBuilder();
    sb.append(SEQUENTIAL_MASK);
    final int size = list.size();
    for (int i = 0; i < size; i++) {
      sb.append(list.get(i).toString());
      if (i != size - 1)
        sb.append("|");
    }
    SharedPreferences.Editor e = mApp.getPref(mApp).edit();
    e.putString(ApplicationCtx.CFG_RECENTLY_OPEN, sb.toString());
    e.apply();
  }

  /**
   * Loads the list of recently opened files.
   *
   * @return List<FileData>
   */
  private List<FileData> load() {
    final List<FileData> uris = new ArrayList<>();
    String content = mApp.getPref(mApp).getString(ApplicationCtx.CFG_RECENTLY_OPEN, "");
    if (content.startsWith(SEQUENTIAL_MASK))
      content = content.substring(SEQUENTIAL_MASK.length());
    String[] split = content.split("\\|");
    if (split.length != 0 && !split[0].equals(""))
      for (String s : split) {
        uris.add(decode(mApp, s));
      }
    return uris;
  }
}
