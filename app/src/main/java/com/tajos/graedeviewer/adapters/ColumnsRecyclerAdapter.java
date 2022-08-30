package com.tajos.graedeviewer.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.poliveira.parallaxrecyclerview.ParallaxRecyclerAdapter;
import com.tajos.graedeviewer.R;
import com.tajos.graedeviewer.util.GraedeViewerUtil;

import java.util.List;
import java.util.Map;

public class ColumnsRecyclerAdapter extends ParallaxRecyclerAdapter<List<List<String>>> {

    private List<List<String>> data;
    private final Context context;

    public ColumnsRecyclerAdapter(Context cn, List<List<String>> data) {
        context = cn;
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolderImpl(ViewGroup viewGroup, ParallaxRecyclerAdapter<List<List<String>>> parallaxRecyclerAdapter, int i) {
        LayoutInflater inflater = (LayoutInflater) context.getApplicationContext().
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View v = inflater.inflate(R.layout.columns_fragment, null);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (int)GraedeViewerUtil.convertDpToPixel(150, context));

        params.setMargins(10,10,10,10);
        v.setLayoutParams(params);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolderImpl(@NonNull RecyclerView.ViewHolder viewHolder,
                                     ParallaxRecyclerAdapter<List<List<String>>> adapter, int position)
    {
        View view = viewHolder.itemView;

        final TextView colName = view.findViewById(R.id.col_name);
        final TextView colData = view.findViewById(R.id.col_data);

        colName.setText(data.get(position).get(0));
        colData.setText(data.get(position).get(1));
    }

    @Override
    public int getItemCountImpl(ParallaxRecyclerAdapter<List<List<String>>> parallaxRecyclerAdapter) {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
