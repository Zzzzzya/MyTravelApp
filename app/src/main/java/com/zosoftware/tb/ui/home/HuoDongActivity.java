package com.zosoftware.tb.ui.home;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.zosoftware.tb.Config;
import com.zosoftware.tb.R;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HuoDongActivity extends AppCompatActivity {
    EditText searchname ;
    ImageView search1;
    TextView likes,clicks,datetime ;
    JSONArray jsonArray =new JSONArray();

    String orderbysort = "hits%20desc";
    boolean likes_desc = true;
    boolean cliks_desc = true;
    boolean date_desc = true;
    private String searchval = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_huo_dong);
        initview();
        initdata();
    }

    private void initdata() {
        getHuoDong();
    }
    private void getHuoDong(){
        String parm = "";

        if(searchval != "") {
            parm = "&like=0&activity_name=" +searchval;
        }


        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(Config.baseaddr+"/tourism_activities/get_list?orderby=" + orderbysort + parm)
                .get().build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: fail" + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp_str = response.body().string();
                Log.d(TAG, "onResponse:  str is "+resp_str);
                JSONObject jsonObject = JSONObject.parseObject(resp_str);
                jsonArray = jsonObject.getJSONObject("result").getJSONArray("list");
                Log.d(TAG, "onResponse: "+jsonArray);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.submitList(jsonArray);
                    }
                });
            }
        });
    }
    private void initview() {
        search1 = findViewById(R.id.searchbtn2);
        likes = findViewById(R.id.likestv);
        clicks = findViewById(R.id.clickstv);
        datetime = findViewById(R.id.datetv);
        searchname = findViewById(R.id.named);
        likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(likes_desc){
                    likes_desc = false;
                    orderbysort = "praise_len%20asc";
                    likes.setText("点赞数倒叙");
                }else {
                    likes_desc = true;
                    orderbysort = "praise_len%20desc";
                    likes.setText("点赞数正叙");
                }
                getHuoDong();
            }
        });
        clicks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cliks_desc){
                    cliks_desc = false;
                    orderbysort = "hits%20asc";
                    clicks.setText("点击数倒叙");
                }else {
                    cliks_desc = true;
                    orderbysort = "hits%20desc";
                    clicks.setText("点击数正叙");
                }
                getHuoDong();
            }
        });
        datetime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(date_desc){
                    date_desc = false;
                    orderbysort = "create_time%20asc";
                    datetime.setText("时间倒叙");
                }else {
                    date_desc = true;
                    orderbysort = "create_time%20desc";
                    datetime.setText("时间正叙");
                }
                getHuoDong();
            }
        });
        search1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchval = searchname.getText().toString();
                getHuoDong();
            }
        });
        ((RecyclerView) findViewById(R.id.recyclerview)).setLayoutManager(new GridLayoutManager(this,2));
        ((RecyclerView) findViewById(R.id.recyclerview)).setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter baseQuickAdapter, @NonNull View view, int i) {
                Intent intent = new Intent( HuoDongActivity.this, HuoDongDetailActivity.class);
                intent.putExtra("id",jsonArray.getJSONObject(i).getInteger("tourism_activities_id")+"");
                startActivity(intent);
            }
        });
    }
    BaseQuickAdapter adapter = new BaseQuickAdapter<JSONObject, QuickViewHolder>() {
        @NonNull
        @Override
        protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
            return new QuickViewHolder(R.layout.huodong_item, viewGroup);
        }

        @Override
        protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable JSONObject objects) {
            ((TextView) quickViewHolder.getView(R.id.title)).setText(objects.getString("activity_name"));
            ((TextView) quickViewHolder.getView(R.id.place)).setText(objects.getString("event_location"));
            ((TextView) quickViewHolder.getView(R.id.datetime)).setText(objects.getString("release_date"));
            ((TextView) quickViewHolder.getView(R.id.fee)).setText(objects.getString("activity_expenses"));
            ((TextView) quickViewHolder.getView(R.id.likenum)).setText(String.valueOf(objects.getInteger("praise_len")));
            ((TextView) quickViewHolder.getView(R.id.clicknum)).setText(String.valueOf(objects.getInteger("hits")));
            Glide.with(getContext())
                    .load(Config.basehttpaddr+objects.getString("activity_images"))
                    .centerCrop()
                    .into( (ImageView) quickViewHolder.getView(R.id.imageView5));

        }
    };
}