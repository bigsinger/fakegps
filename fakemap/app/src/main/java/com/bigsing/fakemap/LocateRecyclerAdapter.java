package com.bigsing.fakemap;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Created by sing on 2018/3/29.
 */

public class LocateRecyclerAdapter extends RecyclerView.Adapter<LocateRecyclerAdapter.LocateViewHolder> implements View.OnClickListener {
    private Context mContext;
    private List<LocationInfo> mList;
    private OnLocationItemClick mLocationItemClick;
    private RecyclerView mRecyclerView;


    public LocateRecyclerAdapter(Context context, List<LocationInfo> list) {
        mContext = context;
        mList = list;
    }

    public void setLocationItemClick(OnLocationItemClick locationItemClick) {
        mLocationItemClick = locationItemClick;
    }

    @Override
    public LocateViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.locate_info_item, parent, false);
        view.setOnClickListener(this);
        return new LocateViewHolder(view);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;

    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerView = null;
    }

    @Override
    public void onBindViewHolder(LocateViewHolder holder, int position) {
        holder.mTextView.setText(mList.get(position).getAddress());

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public void onClick(View view) {
        int position = mRecyclerView.getChildAdapterPosition(view);
        mLocationItemClick.OnLocationClick(mRecyclerView, view, position, mList.get(position));
    }

    public interface OnLocationItemClick {
        void OnLocationClick(RecyclerView parent, View view, int position, LocationInfo locationInfo);
    }

    public static class LocateViewHolder extends RecyclerView.ViewHolder {

        private TextView mTextView;

        public LocateViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.locate_info_adress);
        }
    }
}