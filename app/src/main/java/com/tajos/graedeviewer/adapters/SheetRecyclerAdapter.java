package com.tajos.graedeviewer.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tajos.graedeviewer.GradesViewerActivity;
import com.tajos.graedeviewer.R;

import java.util.List;

public class SheetRecyclerAdapter extends RecyclerView.Adapter<SheetRecyclerAdapter.ViewHolder> {

    private final Context context;
    private final List<String> data;
    private final String workbookName;

    public SheetRecyclerAdapter(Context cn, List<String> sheets, String workbookName) {
        context = cn;
        data = sheets;
        this.workbookName = workbookName;
    }

    @NonNull
    @Override
    public SheetRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getApplicationContext().
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View v = inflater.inflate(R.layout.sheet_fragment, null);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        params.setMargins(10,10,10,10);
        v.setLayoutParams(params);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        View view = holder.itemView;

        final TextView sheetTitle = view.findViewById(R.id.sheetTitle);
        final LinearLayout sheetRoot = view.findViewById(R.id.sheetFragmentRoot);

        sheetTitle.setText(data.get(position));

        // listen for click
        sheetRoot.setOnClickListener((view1) -> {
            Intent intent = new Intent();
            intent.setClass(context, GradesViewerActivity.class);
            intent.putExtra("workbook-name", workbookName);
            intent.putExtra("sheet-name", sheetTitle.getText().toString());
            context.startActivity(intent);
        });
        // end
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
