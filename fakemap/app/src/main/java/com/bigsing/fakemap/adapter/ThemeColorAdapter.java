package com.bigsing.fakemap.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bigsing.fakemap.R;
import com.bigsing.fakemap.utils.ThemeColor;

/**
 * Created by sing on 2017/4/19.
 */

public class ThemeColorAdapter extends EasyRecyclerViewAdapter<ThemeColor> {


    private int position;

    public ThemeColorAdapter() {

    }

    @Override
    public RecyclerView.ViewHolder onCreate(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_theme_color, parent, false);
        return new ThemeColorViewHolder(view);
    }

    @Override
    public void onBind(final RecyclerView.ViewHolder viewHolder, final int RealPosition, ThemeColor data) {
        ((ThemeColorViewHolder) viewHolder).them_color.setImageResource(data.getColor());
        if (data.isChosen()) {
            ((ThemeColorViewHolder) viewHolder).chosen.setVisibility(View.VISIBLE);
            position = RealPosition;
        } else {
            ((ThemeColorViewHolder) viewHolder).chosen.setVisibility(View.GONE);
        }
    }

    public int getPosition() {
        return position;
    }

    class ThemeColorViewHolder extends EasyViewHolder {
        ImageView them_color;
        ImageView chosen;

        public ThemeColorViewHolder(View itemView) {
            super(itemView);
            them_color = (ImageView) itemView.findViewById(R.id.them_color);
            chosen = (ImageView) itemView.findViewById(R.id.choose);
        }
    }
}
