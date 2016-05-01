package org.kei.android.phone.hexviewer;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.util.Log;

/**
 *******************************************************************************
 * @file ApplicationCtx.java
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
public class ApplicationCtx extends Application {
  private List<Byte> payload  = null;
  private String                filename = null;
  
  public ApplicationCtx() {
    super();
    payload = new ArrayList<Byte>();
  }
  
  public Byte[] getPayload() {
    Byte [] b = new Byte[payload.size()];
    return payload.toArray(b);
  }
  
  public byte[] toPayload() {
    byte [] b = new byte[payload.size()];
    for(int i = 0; i < payload.size(); ++i)
      b[i] = payload.get(i);
    return b;
  }

  public void setPayload(final byte[] payload) {
    this.payload.clear();
    for(int i = 0; i < payload.length; ++i)
      this.payload.add(payload[i]);
  }
  
  public void updatePayload(final int index, final byte[] payload) {
    int len = this.payload.size();
    Log.e("TAG", "index: " + index + ", len: " + len);
    for(int i = index + Helper.MAX_BY_ROW -  1; i >= index; --i)
      if(i < len) this.payload.remove(i);
    for(int i = index, j = 0; i < index + payload.length; ++i, ++j)
      this.payload.add(i, payload[j]);
  }
  
  public String getFilename() {
    return filename;
  }
  
  public void setFilename(final String filename) {
    //Log.e("TAG", "Filename:'"+filename+"'");
    this.filename = filename;
  }
  
}
