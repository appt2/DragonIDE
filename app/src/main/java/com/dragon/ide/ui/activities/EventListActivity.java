package com.dragon.ide.ui.activities;

import static com.dragon.ide.utils.Environments.PROJECTS;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.MainThread;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.dragon.ide.R;
import com.dragon.ide.databinding.ActivityEventListBinding;
import com.dragon.ide.objects.Event;
import com.dragon.ide.objects.WebFile;
import com.dragon.ide.ui.adapters.EventListAdapter;
import com.dragon.ide.ui.dialogs.eventList.ShowSourceCodeDialog;
import editor.tsd.tools.Language;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class EventListActivity extends BaseActivity {
  private ActivityEventListBinding binding;
  private ArrayList<WebFile> fileList;
  private WebFile file;
  private ArrayList<Event> eventList;
  private String projectName;
  private String projectPath;
  private String fileName;
  private int fileType;
  private boolean isLoaded;

  @Override
  protected void onCreate(Bundle bundle) {
    super.onCreate(bundle);

    // Inflate and get instance of binding.
    binding = ActivityEventListBinding.inflate(getLayoutInflater());

    // set content view to binding's root.
    setContentView(binding.getRoot());

    // Initialize to avoid null error
    fileList = new ArrayList<WebFile>();
    eventList = new ArrayList<Event>();
    projectName = "";
    projectPath = "";
    fileName = "";
    fileType = 0;
    isLoaded = false;
    file = new WebFile();

    // Setup toolbar.
    binding.toolbar.setTitle(R.string.app_name);
    setSupportActionBar(binding.toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);
    binding.toolbar.setNavigationOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View arg0) {
            onBackPressed();
          }
        });

    if (getIntent().hasExtra("projectName")) {
      projectName = getIntent().getStringExtra("projectName");
      projectPath = getIntent().getStringExtra("projectPath");
      fileName = getIntent().getStringExtra("fileName");
      fileType = getIntent().getIntExtra("fileType", 1);
    } else {
      showSection(2);
      binding.tvInfo.setText(getString(R.string.error));
    }
    /*
     * Ask for storage permission if not granted.
     * Load events if storage permission is granted.
     */
    if (!MainActivity.isStoagePermissionGranted(this)) {
      showSection(3);
      MainActivity.showStoragePermissionDialog(this);
    } else {
      showEventList();
    }
  }

  private void showEventList() {
    // List is loading, so it shows loading view.
    showSection(1);

    // Load event list in a saparate thread to avoid UI freeze.
    Executor executor = Executors.newSingleThreadExecutor();
    executor.execute(
        () -> {
          if (PROJECTS.exists()) {
            if (!new File(projectPath).exists()) {
              showSection(2);
              binding.tvInfo.setText(getString(R.string.project_not_found));
            } else {
              if (new File(new File(projectPath), "Files.txt").exists()) {
                try {
                  FileInputStream fis =
                      new FileInputStream(new File(new File(projectPath), "Files.txt"));
                  ObjectInputStream ois = new ObjectInputStream(fis);
                  Object obj = ois.readObject();
                  if (obj instanceof ArrayList) {
                    fileList = (ArrayList<WebFile>) obj;
                  }
                  fis.close();
                  ois.close();
                  isLoaded = true;
                  for (int i = 0; i < fileList.size(); ++i) {
                    if (fileList
                        .get(i)
                        .getFilePath()
                        .toLowerCase()
                        .equals(fileName.toLowerCase())) {
                      if (fileList.get(i).getFileType() == fileType) {
                        file = fileList.get(i);
                        eventList = fileList.get(i).getEvents();
                      }
                    }
                  }
                  binding.list.setAdapter(
                      new EventListAdapter(
                          eventList,
                          EventListActivity.this,
                          projectName,
                          projectPath,
                          fileName,
                          fileType));
                  runOnUiThread(
                      () -> {
                        binding.list.setLayoutManager(
                            new LinearLayoutManager(EventListActivity.this));
                        showSection(3);
                      });
                } catch (Exception e) {
                  runOnUiThread(
                      () -> {
                        showSection(2);
                        binding.tvInfo.setText(e.getMessage());
                      });
                }
              } else {
                runOnUiThread(
                    () -> {
                      showSection(2);
                      binding.tvInfo.setText(getString(R.string.no_files_yet));
                    });
              }
            }
          } else {
            runOnUiThread(
                () -> {
                  showSection(2);
                  binding.tvInfo.setText(getString(R.string.project_not_found));
                });
          }
        });
  }

  public void showSection(int section) {
    binding.loading.setVisibility(View.GONE);
    binding.info.setVisibility(View.GONE);
    binding.eventList.setVisibility(View.GONE);
    switch (section) {
      case 1:
        binding.loading.setVisibility(View.VISIBLE);
        break;
      case 2:
        binding.info.setVisibility(View.VISIBLE);
        break;
      case 3:
        binding.eventList.setVisibility(View.VISIBLE);
        break;
    }
  }

  public void saveFileList() {
    Executor executor = Executors.newSingleThreadExecutor();
    executor.execute(
        () -> {
          try {
            FileOutputStream fos =
                new FileOutputStream(new File(new File(projectPath), "Files.txt"));
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(fileList);
            fos.close();
            oos.close();
          } catch (Exception e) {
          }
        });
  }

  @Override
  @MainThread
  public void onBackPressed() {
    super.onBackPressed();
    if (isLoaded) {
      saveFileList();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (isLoaded) {
      saveFileList();
    }
  }

  // Handle option menu
  @Override
  public boolean onCreateOptionsMenu(Menu arg0) {
    super.onCreateOptionsMenu(arg0);
    getMenuInflater().inflate(R.menu.activity_event_list_menu, arg0);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem arg0) {
    if (arg0.getItemId() == R.id.show_source_code) {
      if (isLoaded) {
        String language = "";
        switch (WebFile.getSupportedFileSuffix(file.getFileType())) {
          case ".html":
            language = Language.HTML;
            break;
          case ".css":
            language = Language.CSS;
            break;
          case ".js":
            language = Language.JavaScript;
            break;
        }
        ShowSourceCodeDialog showSourceCodeDialog =
            new ShowSourceCodeDialog(this, file.getCode(), language);
        showSourceCodeDialog.show();
      }
    }
    return super.onOptionsItemSelected(arg0);
  }

  @Override
  protected void onResume() {
    showEventList();
    super.onResume();
  }
}
