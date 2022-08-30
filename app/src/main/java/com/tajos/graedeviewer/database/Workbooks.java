package com.tajos.graedeviewer.database;

import android.os.Handler;
import android.util.Log;

import com.tajos.graedeviewer.GraedeApp;
import com.tajos.graedeviewer.task.BackgroundTaskExecutor;
import com.tajos.graedeviewer.util.GraedeViewerUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public class Workbooks {
    // workbook name, sheet name, List of rows and columns
    Map<String, Map<String, List<List<Object>>>> workbooks = new HashMap<>();
    private static Workbooks instance;
    private final Executor executor;

    private String teacher;
    private GenderType gender;

    public enum GenderType {
        MALE, FEMALE, CUSTOM
    }

    private OnSearchingSchoolIDListener onSearchingSchoolIDListener;
    public interface OnSearchingSchoolIDListener {
        void searchResult(boolean isExist, String studentName);
    }

    public Workbooks() {
        executor = GraedeApp.instance().getExecutorService();
    }

    public Workbooks instance() {
        if (instance == null) {
            instance = new Workbooks();
        }

        return instance;
    }

    public void setData(Map<String, Map<String, List<List<Object>>>> workbooks) {
        this.workbooks = workbooks;
    }

    public String getTeacher() {
        return teacher;
    }

    public Map<String, Map<String, List<List<Object>>>> getData() {
        return workbooks;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public GenderType getGender() {
        return gender;
    }

    public void setGender(GenderType gender) {
        this.gender = gender;
    }

    /**
     * <p> Search the school ID. </p>
     * <p> Search in every first column of the table, because the first column is where
     * we can find the School ID </p><p>We will also return the name of the student</p>
     * @param id The school ID to be search.
     */
    public void searchSchoolId(String id) {
        final Handler resultHandler = GraedeApp.instance().getMainThreadHandler();
        boolean[] isExist = new boolean[] {false};
        // needs to search it in background thread to avoid screen freezing when there's
        // a huge list of workbooks, sheets and table rows.
        executor.execute(() -> {
            for (Map.Entry<String, Map<String, List<List<Object>>>> frstEntry : workbooks.entrySet()) {
                Map<String, List<List<Object>>> sheetsEntry = frstEntry.getValue();

                for (Map.Entry<String, List<List<Object>>> secondEntry : sheetsEntry.entrySet()) {
                    //  sheetname, columname, cellcontent
                    List<List<Object>> sheetData = secondEntry.getValue();
                    int rowIndex = 0;
                    for (int i=0; i<sheetData.size(); i++) {
                        if (sheetData.get(i) instanceof HashMap) {
                            HashMap map = (HashMap) sheetData.get(i);

                            continue;
                        }
                        final List<Object> row = sheetData.get(i);
                        if (row == null)
                            continue;

                        for (int j=0; j<row.size(); j++) {
                            final Object column = row.get(j);

                            if (column == null)
                                break;

                            if (column.toString().equals(id)) {
                                isExist[0] = true;
                                String studentName = sheetData.get(i).get(1).toString();
                                resultHandler.post(() ->
                                        onSearchingSchoolIDListener.searchResult(true, studentName));
                            }
                            // we will break at first loop because we only need to check the
                            // School ID in the first column which is the school id.
                            break;
                        }
                        if (isExist[0])
                            break;
                    }
                    /*for (List<Object> row : sheetData) {
                        for (Object column : row) {
                            if (column == null)
                                break;

                            if (column.toString().equals(id)) {
                                isExist[0] = true;
                                String studentName = sheetData.get(rowIndex).get(1).toString();
                                resultHandler.post(() ->
                                        onSearchingSchoolIDListener.searchResult(true, studentName));
                            }
                            // we will break at first loop because we only need to check the
                            // School ID in the first column which is the school id.
                            break;
                        }
                        if (isExist[0])
                            break;

                        rowIndex++;
                    }*/
                    if (isExist[0])
                        break;
                }
                if (isExist[0])
                    break;
            }

            if (isExist[0])
                return;

            resultHandler.post(() -> {
                onSearchingSchoolIDListener.searchResult(false, null);
            });
        });
    }

    public void setOnSearchingSchoolIDListener(OnSearchingSchoolIDListener onSearchingSchoolIDListener) {
        this.onSearchingSchoolIDListener = onSearchingSchoolIDListener;
    }
}
