package org.kei.android.phone.hexviewer;

import java.io.File;
import java.util.ArrayList;

import org.kei.android.atk.utils.Tools;
import org.kei.android.atk.utils.changelog.ChangeLog;
import org.kei.android.atk.utils.changelog.ChangeLogIds;
import org.kei.android.atk.utils.fx.Fx;
import org.kei.android.atk.view.EffectActivity;
import org.kei.android.atk.view.chooser.FileChooserActivity;
import org.kei.android.atk.view.dialog.DialogHelper;
import org.kei.android.atk.view.dialog.DialogResult;
import org.kei.android.atk.view.dialog.IDialog;
import org.kei.android.phone.hexviewer.task.TaskOpen;
import org.kei.android.phone.hexviewer.task.TaskSave;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

/**
 *******************************************************************************
 * @file MainActivity.java
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
public class MainActivity extends EffectActivity implements IDialog {
  private ChangeLog      changeLog = null;
  private ApplicationCtx actx      = null;
  private ArrayAdapter<String> adapter = null;
  
  static {
    Fx.default_animation = Fx.ANIMATION_FADE;
  }
  
  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    String path = null;
    if (getIntent() != null && getIntent().getData() != null)
      path = getIntent().getData().getEncodedPath();

    setContentView(R.layout.activity_main);

    final ListView payloadLV = (ListView) findViewById(R.id.payloadView);
    adapter = new ArrayAdapter<String>(this,
        R.layout.listview_simple_row, R.id.label1, new ArrayList<String>());
    payloadLV.setAdapter(adapter);
    
    changeLog = new ChangeLog(new ChangeLogIds(R.raw.changelog,
        R.string.changelog_ok_button, R.string.background_color,
        R.string.changelog_title, R.string.changelog_full_title,
        R.string.changelog_show_full), this);
    if (changeLog.firstRun())
      changeLog.getLogDialog().show();
    actx = (ApplicationCtx) getApplication();
    if (path != null) {
      actx.setFilename(ActivityHelper.basename(path));
      TaskOpen to = new TaskOpen(this, adapter);
      to.execute(getIntent().getData());
    }
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode,
      final Intent data) {
    // Check which request we're responding to
    if (requestCode == FileChooserActivity.FILECHOOSER_SELECTION_TYPE_FILE) {
      if (resultCode == RESULT_OK) {
        actx.setFilename(data
            .getStringExtra(FileChooserActivity.FILECHOOSER_SELECTION_KEY));
        new TaskOpen(this, adapter).execute();
      }
    } else if (requestCode == FileChooserActivity.FILECHOOSER_SELECTION_TYPE_DIRECTORY) {
      if (resultCode == RESULT_OK) {
        final String dir = data.getStringExtra(FileChooserActivity.FILECHOOSER_SELECTION_KEY);
        DialogHelper.showCustomDialog(this, R.layout.dialog_save, "Save", this,
            dir, R.id.buttonOk, R.id.buttonCancel);
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    final int id = item.getItemId();
    if (id == R.id.action_changelog) {
      changeLog.getFullLogDialog().show();
      return true;
    } else if (id == R.id.action_open) {
      ActivityHelper.actionOpen(this);
      return true;
    } else if (id == R.id.action_save) {
      if (actx.getFilename() == null) {
        Tools.toast(this, R.drawable.ic_launcher, "Open a file before!");
        return true;
      }
      ActivityHelper.actionSave(this);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public DialogResult doAction(final View owner, final Object rootdir) {
    final EditText txtFilename = (EditText) owner
        .findViewById(R.id.txtFileName);
    final String file = txtFilename.getText().toString();
    if (file.trim().isEmpty()) {
      Tools.showAlertDialog(owner.getContext(), "Error", "Invalid file name");
      return DialogResult.ERROR;
    }
    String path = ""+rootdir;
    if (!path.endsWith("/"))
      path += "/";
    path += file;
    final String dir = path;
    if(new File(path).exists()) {
      Tools.showConfirmDialog(this, "Save", 
          "The file exists are you sure you want to overwrite it?", 
          new android.view.View.OnClickListener() {
            @Override
            public void onClick(View v) {
              actx.setFilename(dir);
              new TaskSave(MainActivity.this).execute();
            }
          }, null);
    } else {
      actx.setFilename(dir);
      new TaskSave(MainActivity.this).execute();
    }
    return DialogResult.SUCCESS;
  }

  @Override
  public void doLoad(final View owner, final Object model) {
    final EditText txtFilename = (EditText) owner
        .findViewById(R.id.txtFileName);
    txtFilename.setText(ActivityHelper.basename(actx.getFilename()));
  }
  
}
