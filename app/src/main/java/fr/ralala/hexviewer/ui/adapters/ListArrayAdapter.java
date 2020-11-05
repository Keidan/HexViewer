package fr.ralala.hexviewer.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import fr.ralala.hexviewer.ApplicationCtx;
import fr.ralala.hexviewer.R;
import fr.ralala.hexviewer.utils.Helper;

/**
 *******************************************************************************
 * <p><b>Project HexViewer</b><br/>
 * Adapter used with the listview.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class ListArrayAdapter extends ArrayAdapter<String> {
  private static final int ID = R.layout.listview_simple_row;
  private final List<String> mItems;
  private final Context mContext;

  public ListArrayAdapter(final Context context, final List<String> objects) {
    super(context, ID, objects);
    mContext = context;
    mItems = objects;
  }

  @Override
  public String getItem(final int i) {
    return mItems.get(i);
  }

  public void setItem(final int i, final String t) {
    mItems.set(i, t);
    super.notifyDataSetChanged();
  }

  @Override
  public int getPosition(String item) {
    return super.getPosition(item);
  }

  @Override
  public int getCount() {
    return mItems.size();
  }

  @Override
  public long getItemId(int position) {
    return super.getItemId(position);
  }

  @Override
  public void clear() {
    mItems.clear();
    super.clear();
  }

  @Override
  public @NonNull View getView(final int position, final View convertView,
                      @NonNull final ViewGroup parent) {
    View v = convertView;
    if (v == null) {
      final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      if(inflater != null) {
        v = inflater.inflate(ID, null);
        final TextView label1 = v.findViewById(R.id.label1);
        v.setTag(label1);
      }
    }
    if(v != null && v.getTag() != null) {
      final TextView holder = (TextView) v.getTag();
      final String string = mItems.get(position);
      final ApplicationCtx app = (ApplicationCtx) mContext.getApplicationContext();
      String s;
      if(app.isPlainText()) {
        char array [] = Helper.extractString(string).replaceAll(" ", "").toCharArray();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < array.length; i+=2) {
          sb.append((char)Integer.parseInt("" + array[i] + array[i + 1], 16));
        }
        s = sb.toString();
      } else {
        s = string;
      }
      holder.setText(s);
    }
    return v == null ? new View(mContext) : v;
  }

}

