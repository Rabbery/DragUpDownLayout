package com.mx;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mx.dragscrolllayout.R;
import com.mx.dragscrolllayout.callback.OnInterceptCallBack;
import com.mx.dragscrolllayout.callback.OnScrollChangeTopListenr;
import com.mx.dragscrolllayout.dragscrolllayout.DragScrollVerticalLayout;
import com.mx.dragscrolllayout.util.DisplayUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnInterceptCallBack {

    private static final String TAG = "MainActivity";

    private DragScrollVerticalLayout mScrollLayout;
    private RecyclerView mDataRecyclerView;
    private RecyclerView leftrecyclerview;
    private List<String> mDatas = new ArrayList<>();
    private DataAdapter mDataAdapter;
    private DataAdapter mDataAdapter2;
    private LinearLayout ll_search_head;
    private int mLeftWitdh;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mScrollLayout.setMiddleHeight(DisplayUtil.dip2px(getApplicationContext(), 133));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        setContentView(R.layout.activity_main);
        initData();
        initViews();
    }

    private void initData() {
        mDatas.clear();
        for (int i = 0; i < 20; i++) {
            mDatas.add(i + "");
        }
        mLeftWitdh = DisplayUtil.dip2px(getApplicationContext(), 80);
    }

    private void initViews() {
        mScrollLayout = (DragScrollVerticalLayout) findViewById(R.id.slide_layout);
        ll_search_head = (LinearLayout) findViewById(R.id.ll_search_head);
//        设置事件拦截器
        mScrollLayout.setInterceptCallBack(this);
        mDataRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_data_list);
        leftrecyclerview = (RecyclerView) findViewById(R.id.leftrecyclerview);
        mDataAdapter = new DataAdapter(0);
        mDataRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mDataRecyclerView.setAdapter(mDataAdapter);
        mDataAdapter2 = new DataAdapter(1);
        leftrecyclerview.setLayoutManager(new LinearLayoutManager(this));
        leftrecyclerview.setAdapter(mDataAdapter2);

        mScrollLayout.post(new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessage(1);
            }
        });

        mScrollLayout.setOnScrollChangeTopListenr(new OnScrollChangeTopListenr() {
            @Override
            public void onScrollChange(int x, int y) {
                if (y >= mScrollLayout.getMiddleHeight()) {
                    ll_search_head.setBackgroundColor(Color.argb((int) 255, 81, 83, 92));
                } else if (y <= 0) {
                    ll_search_head.setBackgroundColor(Color.argb((int) 255, 255, 255, 255));
                } else {
                    float scale = (float) (mScrollLayout.getMiddleHeight() - y + 50) / mScrollLayout.getMiddleHeight();
                    if (scale > 1) {
                        scale = 1;
                    }
                    int alpha = (int) (255 * scale);
                    ll_search_head.setBackgroundColor(Color.argb(alpha, 255, 255, 255));
                }

            }
        });
    }

    @Override
    public boolean checkIfIntercept(MotionEvent ev,boolean downDirect) {
        View firstChild = mDataRecyclerView.getChildAt(0);
        View firstLeftChild = leftrecyclerview.getChildAt(0);
        boolean shouldIntercept;
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mDataRecyclerView.getLayoutManager();
        LinearLayoutManager leftManager = (LinearLayoutManager) leftrecyclerview.getLayoutManager();
        int firstVisiblePosition = linearLayoutManager.findFirstVisibleItemPosition();
        int firstLeftVisiblePosition = leftManager.findFirstVisibleItemPosition();
        if (ev.getX() > mLeftWitdh ) {
            if(downDirect && firstVisiblePosition == 0 && firstChild.getTop() == 0){
                shouldIntercept = true;
            }else{
                shouldIntercept = false;
            }
        } else {
            if(downDirect && firstLeftVisiblePosition ==0 && firstLeftChild.getTop() ==0){
                shouldIntercept = true;
            }else{
                shouldIntercept = false;
            }
        }
        return shouldIntercept;
    }


    private class DataAdapter extends RecyclerView.Adapter<DataAdapter.DataViewHolder> {
        private int type;

        public DataAdapter(int type) {
            this.type = type;
        }

        @Override
        public DataAdapter.DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View layout = LayoutInflater.from(parent.getContext()).inflate(type == 0 ? R.layout.data_list_item_layout : R.layout.left_list_item_layout, parent, false);
            DataViewHolder dataViewHolder = new DataViewHolder(layout);
            return dataViewHolder;
        }

        @Override
        public void onBindViewHolder(DataAdapter.DataViewHolder holder, final int position) {
            Log.i(TAG, "onBindViewHolder position:" + position);
            Log.i(TAG, "onBindViewHolder getItemCount:" + getItemCount());

            holder.contentTV.setText(mDatas.get(position));
            holder.contentTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "click:" + position, Toast.LENGTH_SHORT).show();
//                    leftrecyclerview.scrollToPosition(0);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mDatas == null ? 0 : mDatas.size();
        }

        class DataViewHolder extends RecyclerView.ViewHolder {

            TextView contentTV;

            public DataViewHolder(View itemView) {
                super(itemView);
                contentTV = (TextView) itemView.findViewById(R.id.tv_data_list_item_content);
            }

        }
    }

}
