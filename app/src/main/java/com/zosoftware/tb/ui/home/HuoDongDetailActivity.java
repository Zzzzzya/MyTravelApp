package com.zosoftware.tb.ui.home;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.zosoftware.tb.Config;
import com.zosoftware.tb.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HuoDongDetailActivity extends AppCompatActivity {
    EditText name,date,plan,fee,place;
    TextView content,likes,clicks,requirements;
    ImageView image;
    Button likebtn,commentbtn2,favbtn;
    JSONObject huodongdetail = new JSONObject();
    RecyclerView comments_recycl;
    String source_id = "1";
    //初始化AMapLocationClientOption对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_huo_dong_detail);
        initview(savedInstanceState);
        initdata(getIntent().getStringExtra("id" )+"");
        source_id = getIntent().getStringExtra("id" )+"";
    }


    private void initdata(String id) {

        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(Config.baseaddr+"/tourism_activities/get_obj?tourism_activities_id="+id)
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
                JSONObject _temp = JSONObject.parseObject(resp_str);
                huodongdetail = _temp.getJSONObject("result").getJSONObject("obj");


                Log.d(TAG, "onResponse: "+huodongdetail);
                HuoDongDetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        name.setText(huodongdetail.getString("activity_name"));
                        fee.setText(huodongdetail.getString("activity_expenses"));
                        place.setText(huodongdetail.getString("event_location"));
                        plan.setText(huodongdetail.getString("schedule"));
                        likes.setText(huodongdetail.getInteger("praise_len")+"");
                        clicks.setText(huodongdetail.getInteger("hits")+1+"");
                        content.setText(huodongdetail.getString("activity_content"));
                        date.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(huodongdetail.getLong("create_time"))));
                        if(huodongdetail.containsKey("activity_images"))
                            Glide.with(HuoDongDetailActivity.this)
                                    .load(Config.basehttpaddr+huodongdetail.getString("activity_images"))
                                    .centerCrop()
                                    .into( image);

                        getcommentslist(id);

                        Config.addhits(HuoDongDetailActivity.this,
                                "tourism_activities_id",
                                huodongdetail.getInteger("tourism_activities_id") + "",
                                "tourism_activities");
                        Config.setHits(HuoDongDetailActivity.this,
                                "tourism_activities_id",
                                huodongdetail.getInteger("tourism_activities_id") + "",
                                "tourism_activities",
                                huodongdetail.getInteger("hits")+1);
                    }
                });
            }
        });
    }

    private void getcommentslist(String id) {

        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(Config.baseaddr+"/comment/get_list?source_table=tourism_activities&source_field=tourism_activities_id&source_id="+id)
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
                JSONObject _temp = JSONObject.parseObject(resp_str);
                JSONArray jsonArray = _temp.getJSONObject("result").getJSONArray("list");
                Log.d(TAG, "onResponse: "+huodongdetail);
                HuoDongDetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        commentadapter.submitList(jsonArray);
                    }
                });
            }
        });
    }

    private void initview(Bundle savedInstanceState) {
        requirements = findViewById(R.id.requirement);
        fee = findViewById(R.id.fee);
        plan = findViewById(R.id.plan);
        place = findViewById(R.id.place);
        favbtn = findViewById(R.id.favbtn);
        image = findViewById(R.id.ivPicture);
        commentbtn2 = findViewById(R.id.commentbtn2);
        likebtn = findViewById(R.id.likebtn);
        clicks = findViewById(R.id.clicks);
        likes = findViewById(R.id.likes);
        content = findViewById(R.id.content);
        date = findViewById(R.id.date);
        name = findViewById(R.id.name);
        comments_recycl = findViewById(R.id.comments_recycl);
        comments_recycl.setLayoutManager(new LinearLayoutManager(HuoDongDetailActivity.this));
        comments_recycl.setAdapter(commentadapter);
        commentbtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( HuoDongDetailActivity.this ,AddCommentActivity.class);
                intent.putExtra("id",huodongdetail.getInteger("tourism_activities_id")+"");
                intent.putExtra("source_field", "tourism_activities_id");
                intent.putExtra("source_table","tourism_activities");
                intent.putExtra("source_id",huodongdetail.getInteger("tourism_activities_id")+"");
                startActivity(intent);
            }
        });
        favbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Config.getCollectState(HuoDongDetailActivity.this,
                        "tourism_activities_id",
                        huodongdetail.getInteger("tourism_activities_id") + "",
                        "tourism_activities", new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {

                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                String resp = response.body().string();
                                Log.d(TAG, "onResponse: " +resp);
                                JSONObject jsonObject =JSONObject.parseObject(resp);
                                if(jsonObject.containsKey("result"))
                                    if (jsonObject.getInteger("result") == 0) {
                                        HuoDongDetailActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(HuoDongDetailActivity.this, "收藏成功!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        huodongdetail.put("praise_len", huodongdetail.getInteger("praise_len") + 1);
                                        Config.addcollect(HuoDongDetailActivity.this,
                                                "tourism_activities_id",
                                                huodongdetail.getInteger("tourism_activities_id") + "",
                                                "tourism_activities",
                                                huodongdetail.getString("activity_name"),
                                                huodongdetail.getString("activity_images"));

                                    } else {
                                        HuoDongDetailActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(HuoDongDetailActivity.this, "取消收藏成功!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        huodongdetail.put("praise_len", huodongdetail.getInteger("praise_len") - 1);
                                        Config.delcollect(HuoDongDetailActivity.this,
                                                "tourism_activities_id",
                                                huodongdetail.getInteger("tourism_activities_id") + "",
                                                "tourism_activities");

                                    }
                            }
                        });
            }
        });
        likebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Config.getPraiseState(HuoDongDetailActivity.this,
                        "tourism_activities_id",
                        huodongdetail.getInteger("tourism_activities_id") + "",
                        "tourism_activities", new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {

                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                String resp = response.body().string();
                                Log.d(TAG, "onResponse: " +resp);
                                JSONObject jsonObject =JSONObject.parseObject(resp);
                                if(jsonObject.containsKey("result"))
                                    if (jsonObject.getInteger("result") == 0) {
                                        HuoDongDetailActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(HuoDongDetailActivity.this, "点赞成功!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        huodongdetail.put("praise_len", huodongdetail.getInteger("praise_len") + 1);
                                        Config.addpraise(HuoDongDetailActivity.this,
                                                "tourism_activities_id",
                                                huodongdetail.getInteger("tourism_activities_id") + "",
                                                "tourism_activities");
                                        Config.setpraise(HuoDongDetailActivity.this,
                                                "tourism_activities_id",
                                                huodongdetail.getInteger("tourism_activities_id") + "",
                                                "tourism_activities", huodongdetail.getInteger("praise_len"));
                                    } else {
                                        HuoDongDetailActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(HuoDongDetailActivity.this, "取消点赞成功!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        huodongdetail.put("praise_len", huodongdetail.getInteger("praise_len") - 1);
                                        Config.delpraise(HuoDongDetailActivity.this,
                                                "tourism_activities_id",
                                                huodongdetail.getInteger("tourism_activities_id") + "",
                                                "tourism_activities");
                                        Config.setpraise(HuoDongDetailActivity.this,
                                                "tourism_activities_id",
                                                huodongdetail.getInteger("tourism_activities_id") + "",
                                                "tourism_activities", huodongdetail.getInteger("praise_len"));
                                    }
                            }
                        });
            }
        });
    }

    BaseQuickAdapter commentadapter = new BaseQuickAdapter<JSONObject, QuickViewHolder>() {
        @NonNull
        @Override
        protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
            return new QuickViewHolder(R.layout.comment_item, viewGroup);
        }

        @Override
        protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable JSONObject objects) {
//            ((TextView) quickViewHolder.getView(R.id.gonggaotv)).setText(objects);
            if(objects.containsKey("avatar")){
                Glide.with(HuoDongDetailActivity.this)
                        .load(Config.basehttpaddr+objects.getString("avatar"))
                        .centerCrop()
                        .into( (ImageView) quickViewHolder.getView(R.id.avatar));
            }
            ((TextView)quickViewHolder.getView(R.id.comment)).setText("评论："+objects.getString("content"));
            ((TextView)quickViewHolder.getView(R.id.nickname)).setText("昵称："+objects.getString("nickname"));
            ((TextView)quickViewHolder.getView(R.id.date)).setText( new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(objects.getLong("create_time"))));
            ((TextView)quickViewHolder.getView(R.id.replay)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(HuoDongDetailActivity.this,AddCommentActivity.class);
                    intent.putExtra("id",objects.getInteger("source_id")+"");
                    intent.putExtra("source_field",objects.getString("source_field"));
                    intent.putExtra("source_table",objects.getString("source_table"));
                    intent.putExtra("source_id",objects.getInteger("source_id")+"");
                    intent.putExtra("replay_user",objects.getString("nickname"));
                    intent.putExtra("reply_to_id",objects.getString("user_id"));
                    startActivity(intent);
                }
            });
        }
    };



    @Override
    protected void onResume() {
        super.onResume();
        initdata(source_id);
    }

}