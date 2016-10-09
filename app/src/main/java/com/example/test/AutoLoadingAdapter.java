package com.example.test;

import android.content.Context;
import android.content.res.Configuration;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 06.10.2016.
 */
public class AutoLoadingAdapter extends RecyclerView.Adapter {

    private static final String TAG = "AutoLoadingAdapter";

    private static final int ITEM_TYPE_NEWS = 0;
    private static final int ITEM_TYPE_PROGRESS = 1;

    private List<NewsItem> mItems = new ArrayList<>();
    OnActionListener mListener;
    Context mContext;
    private int cur_selected_index = -1, last_selected_index = -1;

    AutoLoadingAdapter(Context context){
        mContext = context;
    }

    public void setContents(List<NewsItem> items){
        if(!items.equals(mItems)) {
            mItems.clear();
            mItems.addAll(items);
            notifyDataSetChanged();
        }
    }

    public void setListener(OnActionListener listener){
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        switch(viewType){
            case ITEM_TYPE_NEWS: {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
                return new NewsViewHolder(view);
            }
            case ITEM_TYPE_PROGRESS: {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.progress_item, parent, false);
                return new ProgressViewHolder(view);
            }
            default: return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if(getItemViewType(position) == ITEM_TYPE_NEWS){
            NewsViewHolder hldr = (NewsViewHolder)holder;

            hldr.bind(position);

            if(position == cur_selected_index) {
                hldr.setSelected(true);
                Log.d(TAG, "select " + position);
            } else hldr.setSelected(false);

            hldr.header.setText(mItems.get(position).title);
            hldr.date.setText(mItems.get(position).date);
            Glide.with(mContext).load(mItems.get(position).pic_url).placeholder(mContext.getResources().getDrawable(R.drawable.placeholder)).into(hldr.image);
        }

        if(position == mItems.size()  && position != 0){
            mListener.needMoreData();
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size() + 1;  // for progress
    }

    @Override
    public int getItemViewType(int position){
        if(position < mItems.size())
            return ITEM_TYPE_NEWS;
        else return ITEM_TYPE_PROGRESS;
    }

    public class NewsViewHolder extends RecyclerView.ViewHolder{

        TextView header,date;
        ImageView image;

        public NewsViewHolder(View itemView) {
            super(itemView);
            header = (TextView) itemView.findViewById(R.id.header);
            date = (TextView) itemView.findViewById(R.id.date);
            image = (ImageView) itemView.findViewById(R.id.image);
        }

        public void bind(final int index) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null)
                        mListener.onItemClicked(mItems.get(index).url,mItems.get(index).title);
                        last_selected_index = cur_selected_index;
                        cur_selected_index = getAdapterPosition();
                        /*notifyItemChanged(cur_selected_index);
                        notifyItemChanged(last_selected_index);*/
                        //Log.d(TAG, "clicked on "+cur_selected_index);
                        notifyDataSetChanged();
                }
            });
        }

        public void setSelected(boolean selected){
            if(mContext instanceof MainActivity) {
                if (((MainActivity) mContext).getOrientation() == Configuration.ORIENTATION_PORTRAIT)
                    itemView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.list_item_selector));

                else
                    itemView.setBackgroundDrawable(mContext.getResources().getDrawable((selected) ? R.color.light_blue_500 : R.drawable.list_item_selector));
            }
        }
    }

    public class ProgressViewHolder extends RecyclerView.ViewHolder{

        public ProgressViewHolder(View itemView) {
            super(itemView);
        }
    }

    public void setSelectedIndex(int index){
        cur_selected_index = index;
        notifyItemChanged(index);
    }

    public int getSelectedIndex(){
        return cur_selected_index;
    }

    public interface OnActionListener{
        void onItemClicked(String url, String title);
        void needMoreData();
    }


}
