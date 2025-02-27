package com.dragon.ide.ui.dialogs;

import static com.dragon.ide.utils.Environments.PROJECTS;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;
import com.dragon.ide.R;
import com.dragon.ide.databinding.LayoutCreateProjectBinding;
import com.dragon.ide.listeners.ProjectCreationListener;
import com.dragon.ide.objects.Project;
import com.dragon.ide.utils.ProjectNameValidator;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class CreateProjectDialog {
  public CreateProjectDialog(
      Activity activity,
      ArrayList<HashMap<String, Object>> projectList,
      ProjectCreationListener listener) {
    MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(activity);
    dialog.setTitle(activity.getString(R.string.create_new_project));
    LayoutCreateProjectBinding binding =
        LayoutCreateProjectBinding.inflate(activity.getLayoutInflater());
    dialog.setView(binding.getRoot());
    dialog.setNegativeButton(activity.getString(R.string.cancel), null);
    dialog.setPositiveButton(
        activity.getString(R.string.create),
        (param1, param2) -> {
          if (ProjectNameValidator.isValidProjectName(binding.projectName.getText().toString())) {
            new File(PROJECTS, binding.projectName.getText().toString()).mkdirs();
            try {
              FileOutputStream fos =
                  new FileOutputStream(
                      new File(
                          new File(PROJECTS, binding.projectName.getText().toString()),
                          "Project.txt"));
              ObjectOutputStream oos = new ObjectOutputStream(fos);
              Project project = new Project();
              project.setProjectName(binding.projectName.getText().toString().trim());
              oos.writeObject(project);
              oos.close();
              fos.close();
              listener.onProjectCreated(project.getProjectName());
            } catch (Exception err) {
            }
          } else {
            if (!(ProjectNameValidator.isValidProjectName(
                binding.projectName.getText().toString()))) {
              Toast.makeText(
                      activity,
                      activity.getString(R.string.invalid_project_name),
                      Toast.LENGTH_LONG)
                  .show();
            } else if (isNameAlreadyInUse(projectList, binding.projectName.getText().toString())) {
              Toast.makeText(
                      activity, activity.getString(R.string.already_in_use), Toast.LENGTH_LONG)
                  .show();
            }
          }
        });
    binding.projectName.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

          @Override
          public void onTextChanged(CharSequence arg0, int start, int before, int count) {
            binding.TextInputLayout1.setError(activity.getString(R.string.invalid_project_name));
            if (ProjectNameValidator.isValidProjectName(arg0.toString())) {
              binding.TextInputLayout1.setErrorEnabled(false);
              if (isNameAlreadyInUse(projectList, binding.projectName.getText().toString())) {
                binding.TextInputLayout1.setErrorEnabled(true);
                binding.TextInputLayout1.setError(activity.getString(R.string.already_in_use));
              }
            } else {
              binding.TextInputLayout1.setErrorEnabled(true);
            }
          }

          @Override
          public void afterTextChanged(Editable arg0) {}
        });
    dialog.create().show();
  }

  public boolean isNameAlreadyInUse(ArrayList<HashMap<String, Object>> projectList, String name) {
    boolean isUsed = false;
    for (int i = 0; i < projectList.size(); ++i) {
      if (projectList.get(i).containsKey("Path")) {
        if (((File) projectList.get(i).get("Path"))
            .getAbsolutePath()
            .toLowerCase()
            .equals(new File(PROJECTS, name).getAbsolutePath().toLowerCase())) {
          isUsed = true;
        }
      }
      if (projectList.get(i).containsKey("Project")) {
        if (((Project) projectList.get(i).get("Project"))
            .getProjectName()
            .toLowerCase()
            .equals(name.toLowerCase())) {
          isUsed = true;
        }
      }
    }
    return isUsed;
  }
}
