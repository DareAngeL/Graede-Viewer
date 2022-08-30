package com.tajos.graedeviewer;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.tajos.graedeviewer.databinding.SplashActivityBinding;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private SplashActivityBinding binding;
    private SharedPreferences s;

    @Override
    protected void onStart() {
        super.onStart();
        s = getSharedPreferences(getString(R.string.saved_content), AppCompatActivity.MODE_PRIVATE);
        if (!s.getString(getString(R.string.firs_app_init), "").isEmpty()) {
            Intent intent = new Intent();
            // if the workspace id is empty we will redirect the user to the
            // workspace code fill up activity
            if (s.getString(getString(R.string.workspace_id), "").isEmpty()) {
                intent.setClass(this, WorkspaceFillupActivity.class);
                startActivity(intent);
                finish();
                return;
            }
            // otherwise, we will redirect the user directly to the school id fill up activity.
            intent.setClass(this, SchoolIdActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = SplashActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        _initListeners(savedInstanceState);
    }

    private void _initListeners(Bundle bundle) {
        binding.okayBtn.setOnClickListener((view) -> {
            s.edit().putString("isFirstAppInit", "false").apply();
            Intent intent = new Intent();
            intent.setClass(this, WorkspaceFillupActivity.class);
            startActivity(intent, bundle);
            finish();
        });
    }
}