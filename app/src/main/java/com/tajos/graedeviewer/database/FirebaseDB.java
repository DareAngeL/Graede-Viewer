package com.tajos.graedeviewer.database;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tajos.graedeviewer.util.GraedeViewerUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FirebaseDB {

    private final DatabaseReference mDatabase;
    private final Context context;
    private String mWorskpaceID;
    private String mStudentID;
    private String mStudentName;
    private Workbooks mWorkbooks;

    private OnEnteredWorkspaceListener onEnteredWorkspaceListener;
    public interface OnEnteredWorkspaceListener {
        void onEnteringSuccess(String teacherName, Workbooks.GenderType genderType);
        void onEnteringFailed(String exceptionMsg);
    }

    private OnCheckingOwnershipListener onCheckingOwnershipListener;
    public interface OnCheckingOwnershipListener {
        void onResult(boolean hasOwner, String email);
    }

    public FirebaseDB(Context cn) {
        context = cn;
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * <p>Determines if the entered school ID is valid.
     * </p>
     * <p>This will search in the workbook to determine if
     * the school ID exist or not.</p>
     * <p>And will listen to the result</p>
     * @param id The school ID.
     */
    public void searchStudentID(String id, Workbooks.OnSearchingSchoolIDListener listener) {
        mWorkbooks.setOnSearchingSchoolIDListener(listener);
        mWorkbooks.searchSchoolId(id);
    }

    public String getStudentID() {
        return mStudentID;
    }

    public void setStudentID(String mStudentID) {
        this.mStudentID = mStudentID;
    }

    public String getStudentName() {
        return mStudentName;
    }

    public void setStudentName(String mStudentName) {
        this.mStudentName = mStudentName;
    }

    public Workbooks getWorkbooks() {
        return mWorkbooks;
    }

    /**
     * Attempt to enter the teacher's workspace
     * @param cn The application context.
     * @param id The workspace code.
     */
    public void enterWorkspace(Context cn, String id, Handler handler) {
        mWorskpaceID = id;
        mDatabase.child("users").child(mWorskpaceID).child("workbooks").get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().getValue() == null) {
                        GraedeViewerUtil.toastLong(cn, "Invalid Code!");
                        handler.post(() ->
                            onEnteredWorkspaceListener.onEnteringFailed("Invalid Code"));

                        return;
                    }

                    mWorkbooks = new Workbooks();

                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Map<String, Map<String, Map<String, Object>>>> workbookMap =
                            (Map<String, Map<String, Map<String, Map<String, Object>>>>) task.getResult().getValue();

                        final Map<String, Map<String, List<List<Object>>>> decodedWorkbook =
                                _decodeWorkbookMap(workbookMap);

                        ObjectMapper map = new ObjectMapper();
                        String json = map.writeValueAsString(decodedWorkbook);
                        Log.e("JSON", json);

                        mWorkbooks.setData(decodedWorkbook);
                        _getTeacherName(handler);
                    } catch (Exception e) {
                        handler.post(() ->
                            onEnteredWorkspaceListener.onEnteringFailed(e.getMessage()
                                    + "\n" + Arrays.toString(e.getStackTrace())));
                    }
                } else {
                    AlertDialog.Builder dialog=new AlertDialog.Builder(cn);
                    dialog.setTitle("Error getting data");
                    dialog.setMessage(Objects.requireNonNull(task.getException()).getMessage());
                    dialog.show();
                    handler.post(() ->
                        onEnteredWorkspaceListener.onEnteringFailed(task.getException().getMessage()));
                }
            });
    }

    @SuppressWarnings("unchecked")
    @NonNull
    private Map<String, Map<String, List<List<Object>>>> _decodeWorkbookMap(
            @NonNull Map<String, Map<String, Map<String, Map<String, Object>>>> map)
    {
        Map<String, Map<String, List<List<Object>>>> decodedWorkbook = new HashMap<>();

        //            <wrbookname,<sheetname, <rowname, <colname, cellcontent>>>>
        for (Map.Entry<String, Map<String, Map<String, Map<String, Object>>>> mapEntry : map.entrySet()) {
            String workbookName = mapEntry.getKey();
            Map<String, List<List<Object>>> sheetMap = new HashMap<>();

            //  sheetname,  rowname,    colname, cellcontent
            Map<String, Map<String, Map<String, Object>>> sheets = mapEntry.getValue();
            for (Map.Entry<String, Map<String, Map<String, Object>>> sheetsEntry : sheets.entrySet()) {
                String sheetName = sheetsEntry.getKey();
                List<List<Object>> sheetData = new ArrayList<>();
                // check if it's already an instance of arraylist before decoding
                // we don't need to decode an araylist bcuz that's what we are looking for.
                if (sheetsEntry.getValue() instanceof ArrayList) {
                    List<List<Object>> list = (List<List<Object>>) sheetsEntry.getValue();
                    sheetMap.put(sheetName, list);
                    continue;
                }

                Map<String, Map<String, Object>> rows = sheetsEntry.getValue();

                for (Map.Entry<String, Map<String, Object>> rowsEntry : rows.entrySet()) {
                    List<Object> columnsArr = new ArrayList<>();

                    if (rowsEntry.getValue() instanceof ArrayList) {
                        columnsArr = (List<Object>) rowsEntry.getValue();
                        sheetData.add(columnsArr);
                        continue;
                    }

                    Map<String, Object> columns = rowsEntry.getValue();
                    for (Map.Entry<String, Object> columnsEntry : columns.entrySet()) {
                        String cellContent = (String) columnsEntry.getValue();
                        columnsArr.add(cellContent);
                    }
                    sheetData.add(columnsArr);
                }
                sheetMap.put(sheetName, sheetData);
            }
            decodedWorkbook.put(workbookName, sheetMap);
        }

        return decodedWorkbook;
    }

    private void _getTeacherName(Handler handler) {
        mDatabase.child("users").child(mWorskpaceID).child("info")
            .get().addOnCompleteListener((task) -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> infos = (Map<String, Object>) task.getResult().getValue();
                assert infos != null;
                mWorkbooks.setTeacher(Objects.requireNonNull(infos.get("name")).toString());
                _determineGender(Objects.requireNonNull(infos.get("gender")).toString());

                handler.post(() ->
                    onEnteredWorkspaceListener.onEnteringSuccess(mWorkbooks.getTeacher(), mWorkbooks.getGender()));
            })
            .addOnFailureListener((e) -> {
                handler.post(() ->
                    onEnteredWorkspaceListener.onEnteringFailed(e.getMessage()));
                Log.e("ERROR", e.getMessage());
            });
    }

    public String getTeacherName() {
        return mWorkbooks.getTeacher();
    }

    public Workbooks.GenderType getGender() {
        return mWorkbooks.getGender();
    }

    public void setOnEnteredWorkspaceListener(OnEnteredWorkspaceListener onEnteredWorkspaceListener) {
        this.onEnteredWorkspaceListener = onEnteredWorkspaceListener;
    }

    private void _determineGender(@NonNull String str) {
        if ("Male".equals(str)) {
            mWorkbooks.setGender(Workbooks.GenderType.MALE);
        } else if ("Female".equals(str)) {
            mWorkbooks.setGender(Workbooks.GenderType.FEMALE);
        } else {
            mWorkbooks.setGender(Workbooks.GenderType.CUSTOM);
        }
    }
}