package com.sinnus.zhihupaper.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.loopj.android.http.TextHttpResponseHandler;
import com.sinnus.zhihupaper.R;
import com.sinnus.zhihupaper.activity.MainActivity;
import com.sinnus.zhihupaper.activity.NewsDetailActivity;
import com.sinnus.zhihupaper.adapter.NewsAdapter;
import com.sinnus.zhihupaper.model.Latest;
import com.sinnus.zhihupaper.model.Story;
import com.sinnus.zhihupaper.model.TopStory;
import com.sinnus.zhihupaper.util.Constant;
import com.sinnus.zhihupaper.util.HttpUtil;
import com.sinnus.zhihupaper.view.Kanner;

import org.apache.http.Header;

import java.util.List;

public class MainFragment extends Fragment {
    private static final String ARG_DATE = "date";

    private String date;
    private ListView newsListView;
    private NewsAdapter newsAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private OnFragmentInteractionListener mListener;
    private SwipeRefreshLayout.OnRefreshListener onRefreshListener;
    private Latest latest;
    private Kanner kanner;
    public static MainFragment newInstance(String date) {
        MainFragment fragment = new MainFragment();
        Bundle arg = new Bundle();
        arg.putString(ARG_DATE, date);
        fragment.setArguments(arg);
        return fragment;
    }

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            date = getArguments().getString(ARG_DATE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root =  inflater.inflate(R.layout.fragment_main, container, false);
        newsListView = (ListView) root.findViewById(R.id.news_list_view);
        newsAdapter = new NewsAdapter(getActivity());
        newsListView.setAdapter(newsAdapter);
        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Story story = (Story)newsAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), NewsDetailActivity.class);
                intent.putExtra("story", story);
                startActivity(intent);
                //点击以后要设置 这个listview的item的颜色为已经点击过了得颜色
            }
        });

        View header = inflater.inflate(R.layout.kanner, newsListView, false);
        kanner = (Kanner) header.findViewById(R.id.kanner);
        kanner.setOnItemClickListener(new Kanner.OnItemClickListener() {
            @Override
            public void click(View v, TopStory entity) {
                int[] startingLocation = new int[2];
                v.getLocationOnScreen(startingLocation);
                startingLocation[0] += v.getWidth() / 2;
                Story story = new Story();
                story.setId(entity.getId());
                story.setTitle(entity.getTitle());
                Intent intent = new Intent(getActivity(), NewsDetailActivity.class);
                intent.putExtra(Constant.START_LOCATION, startingLocation);
                intent.putExtra("story", story);
                startActivity(intent);
                ((MainActivity)mListener).overridePendingTransition(0, 0);
            }
        });
        newsListView.addHeaderView(header);
        swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.sr);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadFirst();
            }
        };
        swipeRefreshLayout.setOnRefreshListener(onRefreshListener);
//        swipeRefreshLayout.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                swipeRefreshLayout.setRefreshing(true);
//            }
//        }, 100);
        setSwipeRefreshLayoutRefreshing(root);
        return root;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        loadFirst();
    }

    private void loadFirst() {
        if (HttpUtil.netWorkConnected(getActivity())) {
            HttpUtil.get(Constant.LATESTNEWS, new TextHttpResponseHandler() {
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    System.out.println(responseString);
                    SQLiteDatabase db = ((MainActivity) getActivity()).getDbHelper().getWritableDatabase();
                    db.execSQL("replace into CacheList(date,json) values(" + Constant.LATEST_COLUMN + ",' " + responseString + "')");
                    db.close();
                    parseLatestJson(responseString);
                    swipeRefreshLayout.setRefreshing(false);
                }

            });
        } else {
            SQLiteDatabase db = ((MainActivity) getActivity()).getDbHelper().getReadableDatabase();
            Cursor cursor = db.rawQuery("select * from CacheList where date = " + Constant.LATEST_COLUMN, null);
            if (cursor.moveToFirst()) {
                String json = cursor.getString(cursor.getColumnIndex("json"));
                parseLatestJson(json);
            } else {
            }
            cursor.close();
            db.close();
            swipeRefreshLayout.setRefreshing(false);
        }

    }

    private void parseLatestJson(String responseString) {
        Gson gson = new Gson();
        latest = gson.fromJson(responseString, Latest.class);
        date = latest.getDate();

        kanner.setTopStories(latest.getTop_stories());
        List<Story> stories = latest.getStories();
        System.out.println("stories size" + stories.size());
        Story topic = new Story();
//        topic.setType(Constant.TOPIC);
//        topic.setTitle("今日热闻");
//        stories.add(0, topic);
        newsAdapter.addList(stories);
    }

//    private void loadMore(final String url) {
//        isLoading = true;
//        if (HttpUtils.isNetworkConnected(mActivity)) {
//            HttpUtils.get(url, new TextHttpResponseHandler() {
//                @Override
//                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                }
//
//                @Override
//                public void onSuccess(int statusCode, Header[] headers, String responseString) {
////                    PreUtils.putStringTo(Constant.CACHE, mActivity, url, responseString);
//                    SQLiteDatabase db = ((MainActivity) mActivity).getCacheDbHelper().getWritableDatabase();
//                    db.execSQL("replace into CacheList(date,json) values(" + date + ",' " + responseString + "')");
//                    db.close();
//                    parseBeforeJson(responseString);
//
//                }
//
//            });
//        } else {
//            SQLiteDatabase db = ((MainActivity) mActivity).getCacheDbHelper().getReadableDatabase();
//            Cursor cursor = db.rawQuery("select * from CacheList where date = " + date, null);
//            if (cursor.moveToFirst()) {
//                String json = cursor.getString(cursor.getColumnIndex("json"));
//                parseBeforeJson(json);
//            } else {
//                db.delete("CacheList", "date < " + date, null);
//                isLoading = false;
//                Snackbar sb = Snackbar.make(lv_news, "没有更多的离线内容了~", Snackbar.LENGTH_SHORT);
//                sb.getView().setBackgroundColor(getResources().getColor(((MainActivity) mActivity).isLight() ? android.R.color.holo_blue_dark : android.R.color.black));
//                sb.show();
//            }
//            cursor.close();
//            db.close();
//        }
//    }


    public void setSwipeRefreshLayoutRefreshing(final View root){
        root.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        root.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        swipeRefreshLayout.setRefreshing(true);
                    }
                }
        );
    }

}
