package com.tajos.graedeviewer;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.tajos.graedeviewer.database.FirebaseDB;
import com.tajos.graedeviewer.database.Workbooks;
import com.tajos.graedeviewer.databinding.SchoolIdActivityBinding;
import com.tajos.graedeviewer.util.GraedeViewerUtil;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class SchoolIdActivity extends AppCompatActivity {

    private SchoolIdActivityBinding binding;

    private FirebaseAuth mAuth;
    private FirebaseDB.OnEnteredWorkspaceListener mEnteringWorkspaceListener;
    private SharedPreferences savedContent;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = SchoolIdActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        _initListeners();
        try {
            _init();
        } catch (JsonProcessingException e) {
            Log.e("ERROR", e.getLocation() + e.getMessage());
        }
    }

    @SuppressLint("SetTextI18n")
    private void _init() throws JsonProcessingException {
        binding.loadingSreen.setVisibility(View.VISIBLE);

        savedContent = getSharedPreferences(getString(R.string.saved_content), AppCompatActivity.MODE_PRIVATE);
        String infoStr = savedContent.getString(getString(R.string.teacher_info), "");
        String workspaceCode = savedContent.getString(getString(R.string.workspace_id), "");

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> infoMap = mapper.readValue(infoStr, new TypeReference<Map<String, Object>>() {});

        String gender = Objects.requireNonNull(infoMap.get("gender")).toString();
        if (gender.equals("MALE"))
            binding.teacherNameTxview.setText("Mr. " + infoMap.get("name"));
        else if (gender.equals("FEMALE"))
            binding.teacherNameTxview.setText("Ma'am, " + infoMap.get("name"));
        else
            binding.teacherNameTxview.setText(Objects.requireNonNull(infoMap.get("name")).toString());
        // attempt to enter workspace
        GraedeApp.instance().getDatabase().setOnEnteredWorkspaceListener(mEnteringWorkspaceListener);
        final Handler handler = GraedeApp.instance().getMainThreadHandler();
        GraedeApp.instance().getDatabase().enterWorkspace(this, workspaceCode, handler);
    }

    private void _initListeners() {
        mEnteringWorkspaceListener = new FirebaseDB.OnEnteredWorkspaceListener() {
            @Override
            public void onEnteringSuccess(String teacherName, Workbooks.GenderType genderType) {
                binding.loadingSreen.setVisibility(View.GONE);
            }

            @Override
            public void onEnteringFailed(String exceptionMsg) {
                _showAlert("Error Occured", "Unable to load credentials");
                binding.loadingSreen.setVisibility(View.GONE);
            }
        };
        // When switchworkspace is click
        binding.switchWorkspace.setOnClickListener((view0) -> {
            // clear the saved content in the cache
            savedContent.edit().putString(getString(R.string.workspace_id), "").apply();
            savedContent.edit().putString(getString(R.string.teacher_info), "").apply();
            // end
            // Now, go back to the activity where the user will fill-up new workspace code
            Intent intent = new Intent();
            intent.setClass(this, WorkspaceFillupActivity.class);
            startActivity(intent);
            finish();
            // end
        });

        binding.letsGoBtn.setOnClickListener((view) -> {
            binding.loadingSreen.setVisibility(View.VISIBLE);
            String id = binding.schoolIDEdittxt.getText().toString();

            // search the student ID.
            GraedeApp.instance().getDatabase().searchStudentID(id, (isExist, studentName) -> {
                // search result
                // check the id ownership if the school id exist.
                if (isExist) {
                    _showConfirmationDialog("Confirm",
                    "You are about to see your grades.\nAre you sure" +
                        " this is your school ID?",
                        (dialog1, whichi) -> { // positive btn
                            GraedeApp.instance().getDatabase().setStudentID(id);
                            GraedeApp.instance().getDatabase().setStudentName(studentName);
                            _startMainActivity();
                        }, (dialog2, which) -> {// cancel btn
                            binding.loadingSreen.setVisibility(View.GONE);
                        });
                } else {
                    binding.loadingSreen.setVisibility(View.GONE);
                    GraedeViewerUtil.toastLong(this, "School ID not found!");
                }
            });
        });
    }

    private void _startMainActivity() {
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void _showAlert(String title, String msg) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(title);
        alert.setMessage(msg);
        alert.show();
    }

    private void _showConfirmationDialog(String title, String msg,
                                         DialogInterface.OnClickListener positiveBtnListener,
                                         DialogInterface.OnClickListener negativeBtnListener)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(title);
        alert.setMessage(msg);
        alert.setPositiveButton("Confirm", positiveBtnListener);
        alert.setNegativeButton("Cancel", negativeBtnListener);
        alert.setCancelable(false);
        alert.show();
    }
}