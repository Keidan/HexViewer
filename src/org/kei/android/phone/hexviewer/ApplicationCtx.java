package org.kei.android.phone.hexviewer;

import java.io.ByteArrayOutputStream;

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
  private ByteArrayOutputStream payload  = null;
  private String                filename = null;
  
  public ApplicationCtx() {
    super();
    payload = new ByteArrayOutputStream();
  }
  
  public ByteArrayOutputStream getPayload() {
    return payload;
  }

  public void setPayload(final ByteArrayOutputStream payload) {
    this.payload = payload;
  }
  
  public String getFilename() {
    return filename;
  }
  
  public void setFilename(final String filename) {
    Log.e("TAG", "Filename:'"+filename+"'");
    this.filename = filename;
  }
  
}
