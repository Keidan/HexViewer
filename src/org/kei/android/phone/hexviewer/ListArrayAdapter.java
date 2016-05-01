package org.kei.android.phone.hexviewer;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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
public class ListArrayAdapter<T> extends ArrayAdapter<T> {
  private List<T> items = null;
  private Context            c                = null;
  private int                id               = 0;
  
  public ListArrayAdapter(final Context context, final int textViewResourceId,
      final List<T> objects) {
    super(context, textViewResourceId, objects);
    c = context;
    id = textViewResourceId;
    items = objects;
  }

  public List<T> getItems() {
    return items;
  }

  public boolean contains(final T p) {
    for (final T sm : items) {
      if (sm.equals(p))
        return true;
    }
    return false;
  }

  @Override
  public T getItem(final int i) {
    return items.get(i);
  }

  public void setItem(final int i, final T t) {
    items.set(i, t);
    super.notifyDataSetChanged();
  }

  public int getItemCount() {
    return items.size();
  }

  public void addItem(final T t) {
    items.add(t);
    super.notifyDataSetChanged();
  }

  public void removeItem(final T t) {
    items.remove(t);
    super.notifyDataSetChanged();
  }

  public void removeItem(final int i) {
    items.remove(i);
    super.notifyDataSetChanged();
  }
  
  @Override
  public void clear() {
    items.clear();
    super.clear();
  }
  
  @Override
  public View getView(final int position, final View convertView, final ViewGroup parent) {
    View v = convertView;
    if (v == null) {
      final LayoutInflater vi = (LayoutInflater) c
          .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      final LayoutInflater inflater = vi;
      v = inflater.inflate(id, null);
      TextView label1 = (TextView) v.findViewById(R.id.label1);
      v.setTag(label1);
    }
    
    final TextView holder = (TextView) v.getTag();
    holder.setText("" + items.get(position));
    return v;
  }

}
