package com.tajos.graedeviewer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.tajos.graedeviewer.adapters.ColumnsRecyclerAdapter;
import com.tajos.graedeviewer.databinding.GradesViewerActivityBinding;
import com.tajos.graedeviewer.databinding.ParallaxHeaderBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GradesViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.tajos.graedeviewer.databinding.GradesViewerActivityBinding binding = GradesViewerActivityBinding.inflate(getLayoutInflater());
        com.tajos.graedeviewer.databinding.ParallaxHeaderBinding parallaxHeaderBinding = ParallaxHeaderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        parallaxHeaderBinding.studentName.setText(
                GraedeApp.instance().getDatabase().getStudentName());

        final List<List<String>> data = _extractStudentGrades();

        if (!(data.size() > 0)) {
            binding.emptyScreen.setVisibility(View.VISIBLE);
        }

        ColumnsRecyclerAdapter adapter = new ColumnsRecyclerAdapter(this, data);
        adapter.setShouldClipView(true);
        adapter.setParallaxHeader(parallaxHeaderBinding.getRoot(), binding.columnsRecycler);

        binding.columnsRecycler.setAdapter(adapter);
        binding.columnsRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.columnsRecycler.setHasFixedSize(true);
    }

    @NonNull
    private List<List<String>> _extractStudentGrades() {
        //  workbkname, sheetname, list of rows and cols
        Map<String, Map<String, List<List<Object>>>> data =
                GraedeApp.instance().getDatabase().getWorkbooks().getData();

        String wrkbookName = getIntent().getStringExtra("workbook-name");
        String sheetName = getIntent().getStringExtra("sheet-name");

        String studentId = GraedeApp.instance().getDatabase().getStudentID();

        List<List<String>> extractedGrades = new ArrayList<>();

        List<List<Object>> tableData = Objects.requireNonNull(data.get(wrkbookName)).get(sheetName);
        assert tableData != null;

        for (int i=0; i<tableData.size(); i++) {
            if (tableData.get(i) instanceof HashMap)
                continue;

            final List<Object> row = tableData.get(i);
            // if the id has been found this will set to true so we can loop
            // straight to the end of the row's columns
            boolean isFoundId = false;

            if (row == null)
                continue;

            for (int j=0; j<row.size(); j++) {
                String dataStr = (String) row.get(j);

                if (dataStr == null || dataStr.isEmpty()) {
                    dataStr = "Unavailable";
                }

                // let's check if the id has not been found yet,
                // cuz we only need to set this variable once in order
                // to loop straight in the columns, if an id has been found
                if (!isFoundId)
                    isFoundId = dataStr.equals(studentId);

                if (isFoundId) {
                    // now if the id has been found, let's now loop all the columns
                    if (j >= 2) {
                        List<String> colDataArr = new ArrayList<>();

                        String colName =
                                j >= tableData.get(0).size() ||
                                tableData.get(0).get(j) == null ||
                                tableData.get(0).get(j).toString().isEmpty() ?
                                "Untitled" : tableData.get(0).get(j).toString();
                        colDataArr.add(colName); // column name
                        colDataArr.add(dataStr); // column data or the grades of the student

                        extractedGrades.add(colDataArr); // store it to an array so the column adapter can access it.
                    }

                    continue;
                }

                break;
            }
            if (isFoundId)
                break;
        }

        return extractedGrades;
    }
}