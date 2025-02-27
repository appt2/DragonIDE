package com.dragon.ide.ui.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.dragon.ide.databinding.LayoutProjectsManagerListItemBinding;
import com.dragon.ide.objects.Project;
import com.dragon.ide.ui.activities.FileManagerActivity;
import com.dragon.ide.utils.ProjectFileUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ProjectsManagerListAdapter
    extends RecyclerView.Adapter<ProjectsManagerListAdapter.ViewHolder> {

  public ArrayList<HashMap<String, Object>> _data;
  public Activity activity;

  public ProjectsManagerListAdapter(ArrayList<HashMap<String, Object>> _arr, Activity activity) {
    _data = _arr;
    this.activity = activity;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    LayoutProjectsManagerListItemBinding item =
        LayoutProjectsManagerListItemBinding.inflate(activity.getLayoutInflater());
    View _v = item.getRoot();
    RecyclerView.LayoutParams _lp =
        new RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    _v.setLayoutParams(_lp);
    return new ViewHolder(_v);
  }

  @Override
  public void onBindViewHolder(ViewHolder _holder, final int _position) {
    LayoutProjectsManagerListItemBinding binding =
        LayoutProjectsManagerListItemBinding.bind(_holder.itemView);
    binding.projectName.setText(((Project) _data.get(_position).get("Project")).getProjectName());
    binding
        .getRoot()
        .setOnClickListener(
            (view) -> {
              Intent i = new Intent();
              i.setClass(activity, FileManagerActivity.class);
              i.putExtra(
                  "projectName", ((Project) _data.get(_position).get("Project")).getProjectName());
              i.putExtra(
                  "projectPath", ((File) _data.get(_position).get("Path")).getAbsolutePath());
              i.putExtra(
                  "ListPath",
                  ProjectFileUtils.getProjectFilesDirectory(
                          ((File) _data.get(_position).get("Path")))
                      .getAbsolutePath());
              i.putExtra("outputDirectory", "");
              activity.startActivity(i);
            });
  }

  @Override
  public int getItemCount() {
    return _data.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    public ViewHolder(View v) {
      super(v);
    }
  }
}
