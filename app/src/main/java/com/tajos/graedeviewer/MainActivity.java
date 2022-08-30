package com.tajos.graedeviewer;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.tajos.graedeviewer.adapters.WorkspaceRecyclerAdapter;
import com.tajos.graedeviewer.database.FirebaseDB;
import com.tajos.graedeviewer.databinding.MainActivityBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private MainActivityBinding binding;

    private WorkspaceRecyclerAdapter.WorkspaceRecyclerItemListener mWorkspaceItemListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        _initListeners();
        _initTeacherName();
        _init();
    }

    private void _initTeacherName() {
        binding.teacherNameTxview.setText(GraedeApp.instance().getDatabase().getTeacherName());
    }

    private void _init() {
        FirebaseDB db = GraedeApp.instance().getDatabase();
        List<String> workbookNames = _extractWorkbookNames(db.getWorkbooks().getData());

        if (!(workbookNames.size() > 0))
            binding.emptyScreen.setVisibility(View.GONE);

        binding.workspaceRecycler.setAdapter(new WorkspaceRecyclerAdapter(this,
                workbookNames, mWorkspaceItemListener));
        binding.workspaceRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.workspaceRecycler.setHasFixedSize(true);
    }

    @NonNull
    private List<String> _extractWorkbookNames(@NonNull Map<String, Map<String, List<List<Object>>>> data) {
        List<String> workbookNames = new ArrayList<>();

        for (Map.Entry<String, Map<String, List<List<Object>>>> entry : data.entrySet()) {
            workbookNames.add(entry.getKey());
        }

        return workbookNames;
    }

    private void _initListeners() {
        mWorkspaceItemListener = (position) -> {
            // on item click
            binding.workspaceRecycler.smoothScrollToPosition(position);
        };
    }
}