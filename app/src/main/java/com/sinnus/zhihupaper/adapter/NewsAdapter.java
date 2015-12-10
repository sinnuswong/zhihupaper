package com.sinnus.zhihupaper.adapter;

import android.content.Context;
import android.graphics.Color;
import android.system.StructPollfd;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.sinnus.zhihupaper.R;
import com.sinnus.zhihupaper.activity.MainActivity;
import com.sinnus.zhihupaper.model.Story;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sinnus on 2015/11/13.
 */
public class NewsAdapter extends BaseAdapter {
    private Context context;
    private List<Story> stories;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    public NewsAdapter(Context context) {
        this.context = context;
        this.stories = new ArrayList<>();
        this.imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        imageLoader.init(ImageLoaderConfiguration.createDefault(context));
    }

    @Override
    public int getCount() {
        return stories.size();
    }

    @Override
    public Object getItem(int position) {
        return stories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_news, parent, false);
            viewHolder.newsTitle = (TextView) convertView.findViewById(R.id.news_title);
            viewHolder.newsImage = (ImageView) convertView.findViewById(R.id.news_image);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Story story = new Story();
        if (stories.size() > position) {
            story = stories.get(position);
        }
//        if (position == 0) {  //新闻是 标题
//            ((RelativeLayout) (viewHolder.newsTitle.getParent())).setBackgroundColor(Color.TRANSPARENT);
//            viewHolder.newsImage.setVisibility(View.GONE);
//            viewHolder.newsTitle.setText("今日热闻");
//            viewHolder.newsTitle.setTextSize(13);
//            viewHolder.newsTitle.setTextColor(context.getResources().getColor(R.color.colorPrimary));
//        } else {
//            ((RelativeLayout) (viewHolder.newsTitle.getParent())).setBackgroundResource(R.drawable.list_selector);
//            viewHolder.newsTitle.setVisibility((View.VISIBLE));
//            viewHolder.newsImage.setVisibility(View.VISIBLE);
//            viewHolder.newsTitle.setText(R.string.news_title_sample);
//            viewHolder.newsTitle.setTextSize(18);
//            viewHolder.newsTitle.setTextColor(Color.BLACK);
//        }
        viewHolder.newsTitle.setVisibility(View.VISIBLE);
        viewHolder.newsImage.setVisibility(View.VISIBLE);
        viewHolder.newsTitle.setText(story.getTitle());
        if (story.getImages() != null && story.getImages().size() > 0 && story.getImages().get(0) != null) {
            imageLoader.displayImage(story.getImages().get(0), viewHolder.newsImage, options);
        }
        return convertView;
    }

    static class ViewHolder {
        TextView newsTitle;
        ImageView newsImage;
    }

    public void addList(List<Story> items) {
        this.stories = items;
        notifyDataSetChanged();
    }

}


