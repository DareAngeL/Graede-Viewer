package com.tajos.graedeviewer.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tajos.graedeviewer.GraedeApp;
import com.tajos.graedeviewer.R;
import com.tajos.graedeviewer.animator.SceneAnimator;
import com.tajos.graedeviewer.animator.WorkbookFragmentAnimator;
import com.tajos.graedeviewer.database.Workbooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorkspaceRecyclerAdapter extends RecyclerView.Adapter<WorkspaceRecyclerAdapter.ViewHolder> {

    private final Context context;
    private final List<String> data;

    private final WorkspaceRecyclerItemListener itemListener;

    public interface WorkspaceRecyclerItemListener {
        void onItemClick(int position);
    }

    public WorkspaceRecyclerAdapter(Context cn, List<String> workbooks,
                                    WorkspaceRecyclerItemListener listener) {
        context = cn;
        data = workbooks;
        itemListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getApplicationContext().
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View v = inflater.inflate(R.layout.workbook_fragment, null);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        params.setMargins(10,10,10,10);
        v.setLayoutParams(params);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        View view = holder.itemView;

        final ViewGroup sceneRoot = (ViewGroup) view.findViewById(R.id.scene_root);
        final TextView wrkBookTitle = (TextView) view.findViewById(R.id.workbook_title);

        WorkbookFragmentAnimator animator = new WorkbookFragmentAnimator(context, sceneRoot, R.layout.workbook_fragment_scene1,
                                    R.layout.workbook_fragment_scene2);

        // update UI
        _updateScene1UI(wrkBookTitle, position);
        // end

        final int[] _triggerer = new int[] {0};
        sceneRoot.setOnClickListener((view1) -> _triggerAnimation(animator, _triggerer));

        animator.setListener(new WorkbookFragmentAnimator.OnAnimatingListener() {
            @Override
            public void onStartedAnimating() {
                /*
                  We shall set the title in advance also so the rendering is good
                 */
                View sceneRootChild = sceneRoot.getChildAt(0);

                TextView workbookTitle = sceneRootChild.findViewById(R.id.workbook_title);
                workbookTitle.setText(wrkBookTitle.getText().toString());
            }

            @Override
            public void onFinishedAnimating(boolean fromStartScene) {
                // on animation finished
                if (fromStartScene)
                    _initScene2Views(sceneRoot, wrkBookTitle, animator, _triggerer, position);
                else {
                    _updateScene1UI(wrkBookTitle, position);
                }

                itemListener.onItemClick(position);
            }
        });
    }

    private void _updateScene1UI(@NonNull TextView view, int position) {
        view.setText(data.get(position));
    }

    private void _initScene2Views(@NonNull ViewGroup sceneRoot, @NonNull TextView workbookTitle,
                                  SceneAnimator animator, int[] _triggerer, int pos)
    {
        View sceneRootChild = sceneRoot.getChildAt(0);

        final RecyclerView sheetRecycler = sceneRootChild.findViewById(R.id.sheets_recycler);
        final TextView workbookTitleTxView = sceneRootChild.findViewById(R.id.workbook_title);

        final Workbooks wrkbooks = GraedeApp.instance().getDatabase().getWorkbooks();
        final String workbookTitleStr = workbookTitle.getText().toString();
        final Map<String, List<List<Object>>> map = wrkbooks.getData().get(workbookTitleStr);
        assert map != null;
        final List<String> sheetNames = _extractSheetNames(map);
        // update title
        workbookTitleTxView.setText(workbookTitleStr);
        // end
        // init sheet recycler
        sheetRecycler.setAdapter(new SheetRecyclerAdapter(context, sheetNames, workbookTitleStr));
        sheetRecycler.setLayoutManager(new LinearLayoutManager(context));
        sheetRecycler.setHasFixedSize(true);
        // end

        sceneRoot.setOnClickListener((view1) -> _triggerAnimation(animator, _triggerer));
    }

    @NonNull
    private List<String> _extractSheetNames(@NonNull Map<String, List<List<Object>>> sheets) {
        List<String> sheetNames = new ArrayList<>();
        for (Map.Entry<String, List<List<Object>>> entry : sheets.entrySet()) {
            sheetNames.add(entry.getKey());
        }

        return sheetNames;
    }

    private void _triggerAnimation(SceneAnimator animator, @NonNull int[] _triggerer) {
        if (_triggerer[0] == 0) {
            animator.animateToEndScene();
            _triggerer[0] = 1;
        } else {
            animator.animateToStartScene();
            _triggerer[0] = 0;
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View view) {
            super(view);
        }
    }
}
