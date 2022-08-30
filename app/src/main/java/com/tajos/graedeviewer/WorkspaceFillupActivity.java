package com.tajos.graedeviewer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tajos.graedeviewer.database.FirebaseDB;
import com.tajos.graedeviewer.database.Workbooks;
import com.tajos.graedeviewer.databinding.WorkspaceFillupActivityBinding;

import java.util.HashMap;
import java.util.Map;

public class WorkspaceFillupActivity extends AppCompatActivity {

    private WorkspaceFillupActivityBinding binding;
    private EditText mWorkspaceCode;
    private final Handler mainThreadHandler = GraedeApp.instance().getMainThreadHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workspace_fillup_activity);
        binding = WorkspaceFillupActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        _initListeners();
        _init();

        mWorkspaceCode = binding.wrkspceCodeEdittxt;
    }

    private boolean isFirstAppInit() {
        SharedPreferences s = getSharedPreferences(getString(R.string.saved_content),
                                AppCompatActivity.MODE_PRIVATE);

        return s.getString(getString(R.string.firs_app_init), "").isEmpty();
    }

    private void _init() {
        if (isFirstAppInit()) {
            binding.almostThereTxView.setVisibility(View.INVISIBLE);
        }
    }

    private void _initListeners() {
        binding.proceedBtn.setOnClickListener((view) -> {
            binding.loadingSreen.setVisibility(View.VISIBLE);
            // listens for entering the workspace.
            GraedeApp.instance().getDatabase().setOnEnteredWorkspaceListener(
                new FirebaseDB.OnEnteredWorkspaceListener() {
                      @Override
                      public void onEnteringSuccess(String teacherName, Workbooks.GenderType genderType) {

                          SharedPreferences s = getSharedPreferences(getString(R.string.saved_content),
                                                AppCompatActivity.MODE_PRIVATE);

                          s.edit().putString(getString(R.string.workspace_id),
                                  binding.wrkspceCodeEdittxt.getText().toString())
                                  .apply();

                          ObjectMapper mapper = new ObjectMapper();
                          try {
                              String infoStr = mapper.writeValueAsString(
                                      _saveTeacherInfo(teacherName, genderType));

                              s.edit().putString(getString(R.string.teacher_info), infoStr).apply();

                              Intent intent = new Intent();
                              intent.setClass(WorkspaceFillupActivity.this,
                                      SchoolIdActivity.class);
                              startActivity(intent);
                              finish();
                          } catch (JsonProcessingException e) {
                              Log.e("ERROR", e.getLocation() + e.getMessage());
                          }
                      }

                      @Override
                      public void onEnteringFailed(String exceptionMsg) {
                            binding.loadingSreen.setVisibility(View.GONE);
                            AlertDialog.Builder alert = new AlertDialog.Builder(WorkspaceFillupActivity.this);
                            alert.setTitle("Oops");
                            alert.setMessage("Unable to enter this workspace.");
                            alert.show();
                      }
                });
            // attempt to enter the workspace
            GraedeApp.instance().getExecutorService().execute(() ->
                GraedeApp.instance().getDatabase().enterWorkspace(this,
                        mWorkspaceCode.getText().toString(), mainThreadHandler));
        });
    }

    @NonNull
    private Map<String, Object> _saveTeacherInfo(String name, Workbooks.GenderType gender) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("gender", gender);

        return map;
    }
}