package com.zosoftware.tb.ui.dashboard;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.textservice.TextInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.zosoftware.tb.MainActivity;
import com.zosoftware.tb.R;
import com.zosoftware.tb.ui.home.AddCommentActivity;
import com.zosoftware.tb.ui.home.HuoDongDetailActivity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FormDetailActivity extends AppCompatActivity {

    JSONObject formdetail = new JSONObject();
    String id = "";
    TextView title,username,date,content,tag,likesbtn,favbtn,clicksandlikes;
    RecyclerView recyclerview;
    ImageView imageView;
    Button gocomment;
    TextView delbtn;
    JSONArray commentslist = new JSONArray();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_form_detail);

        id = getIntent().getStringExtra("id");
        initview ();
        initdata();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initdata();
    }

    private void initview() {
        title = findViewById(R.id.title);
        delbtn = findViewById(R.id.delbtn);
        delbtn.setVisibility(View.GONE);
        username = findViewById(R.id.username);
        date = findViewById(R.id.date);
        imageView = findViewById(R.id.image);
        content = findViewById(R.id.content);
        tag = findViewById(R.id.tag);
        likesbtn = findViewById(R.id.likesbtn);
        favbtn = findViewById(R.id.favbtn);
        clicksandlikes = findViewById(R.id.clicksandlikes);
        recyclerview = findViewById(R.id.recyclerview);
        gocomment = findViewById(R.id.gocomment);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        recyclerview.setAdapter(adapter);
        gocomment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( FormDetailActivity.this ,AddCommentActivity.class);
                intent.putExtra("id",id+"");
                intent.putExtra("source_field", "forum_id");
                intent.putExtra("source_table","forum");
                intent.putExtra("source_id",id+"");
                startActivity(intent);
            }
        });

        favbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Config.getCollectState(FormDetailActivity.this,
                        "forum_id",
                        formdetail.getInteger("forum_id") + "",
                        "forum", new Callback() {
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
                                        FormDetailActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(FormDetailActivity.this, "收藏成功!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        formdetail.put("praise_len", formdetail.getInteger("praise_len") + 1);
                                        Config.addcollect(FormDetailActivity.this,
                                                "forum_id",
                                                formdetail.getInteger("forum_id") + "",
                                                "forum",
                                                formdetail.getString("title"),
                                                formdetail.getString("img"));

                                    } else {
                                        FormDetailActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(FormDetailActivity.this, "取消收藏成功!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        formdetail.put("praise_len", formdetail.getInteger("praise_len") - 1);
                                        Config.delcollect(FormDetailActivity.this,
                                                "forum_id",
                                                formdetail.getInteger("forum_id") + "",
                                                "forum");

                                    }
                            }
                        });
            }
        });
        likesbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Config.getPraiseState(FormDetailActivity.this,
                        "forum_id",
                        formdetail.getInteger("forum_id") + "",
                        "forum", new Callback() {
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
                                        FormDetailActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(FormDetailActivity.this, "点赞成功!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        formdetail.put("praise_len", formdetail.getInteger("praise_len") + 1);
                                        Config.addpraise(FormDetailActivity.this,
                                                "forum_id",
                                                formdetail.getInteger("forum_id") + "",
                                                "forum");
                                        Config.setpraise(FormDetailActivity.this,
                                                "forum_id",
                                                formdetail.getInteger("forum_id") + "",
                                                "forum", formdetail.getInteger("praise_len"));
                                    } else {
                                        FormDetailActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(FormDetailActivity.this, "取消点赞成功!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        formdetail.put("praise_len", formdetail.getInteger("praise_len") - 1);
                                        Config.delpraise(FormDetailActivity.this,
                                                "forum_id",
                                                formdetail.getInteger("forum_id") + "",
                                                "forum");
                                        Config.setpraise(FormDetailActivity.this,
                                                "forum_id",
                                                formdetail.getInteger("forum_id") + "",
                                                "forum", formdetail.getInteger("praise_len"));
                                    }
                            }
                        });
            }
        });
    }

    private void initdata() {


        getformdetail();
        getcomments();

    }
    BaseQuickAdapter adapter = new BaseQuickAdapter<JSONObject, QuickViewHolder>() {
        @NonNull
        @Override
        protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
            return new QuickViewHolder(R.layout.form_comment_item, viewGroup);
        }

        @Override
        protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable JSONObject objects) {
//            ((TextView) quickViewHolder.getView(R.id.gonggaotv)).setText(objects);
            if(objects.containsKey("avatar")){
                Glide.with(FormDetailActivity.this)
                        .load(Config.basehttpaddr+objects.getString("avatar"))
                        .centerCrop()
                        .into( (ImageView) quickViewHolder.getView(R.id.image));
            }
            ((TextView)quickViewHolder.getView(R.id.comment)).setText( objects.getString("content"));
            ((TextView)quickViewHolder.getView(R.id.username)).setText( objects.getString("nickname") );
            ((TextView)quickViewHolder.getView(R.id.replay)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(FormDetailActivity.this, AddCommentActivity.class);
                    intent.putExtra("id",objects.getInteger("source_id")+"");
                    intent.putExtra("source_field",objects.getString("source_field"));
                    intent.putExtra("source_table",objects.getString("source_table"));
                    intent.putExtra("source_id",objects.getInteger("source_id")+"");
                    intent.putExtra("replay_user",objects.getString("nickname"));
                    intent.putExtra("reply_to_id",objects.getString("comment_id"));
                    startActivity(intent);
                }
            });
            ((TextView)quickViewHolder.getView(R.id.date)).setText( new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(objects.getLong("create_time"))));
            if(objects.containsKey("replay_list")){
                BaseQuickAdapter baseQuickAdapter = new BaseQuickAdapter<JSONObject, QuickViewHolder>(){

                    @NonNull
                    @Override
                    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
                        return new QuickViewHolder(R.layout.form_comment_item, viewGroup);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull QuickViewHolder viewHolder, int i, @Nullable JSONObject jsonObject) {
                        ((TextView) viewHolder.getView(R.id.username)).setText(jsonObject.getString("nickname"));
                        ((TextView) viewHolder.getView(R.id.comment)).setText(jsonObject.getString("content"));
                        ((TextView) viewHolder.getView(R.id.replay)).setVisibility(View.INVISIBLE);
                        ((TextView) viewHolder.getView(R.id.date)).  setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") .format(jsonObject.getLong("create_time")));
                        if(jsonObject.containsKey("avatar")){
                            Glide.with(FormDetailActivity.this)
                                    .load(Config.basehttpaddr+jsonObject.getString("avatar"))
                                    .centerCrop()
                                    .into( (ImageView) viewHolder.getView(R.id.image));
                        }
                    }
                };
                ((RecyclerView)quickViewHolder.getView(R.id.listview)).setLayoutManager(new LinearLayoutManager(FormDetailActivity.this));
                ((RecyclerView)quickViewHolder.getView(R.id.listview)).setAdapter(baseQuickAdapter);
                baseQuickAdapter.submitList(objects.getJSONArray("replay_list"));
            }
        }
    };



    private void getcomments() {

        String parm = "&reply_to_id=0&source_id=" +id;
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(Config.baseaddr+"/comment/get_list?source_table=forum&source_field=forum_id&source_id=1&orderby=create_time%20desc"+parm)
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
                Log.d(TAG, "onResponse: form str is "+resp_str);
                JSONObject _temp = JSONObject.parseObject(resp_str);
                commentslist = _temp.getJSONObject("result").getJSONArray("list");

                for (int i = 0 ; i< commentslist.size() ;i ++) {

                    String parm2 = "&reply_to_id=" + commentslist.getJSONObject(i).getInteger("comment_id");
                    OkHttpClient client2 = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
                    Request request2 = new Request.Builder().url(Config.baseaddr+"/comment/get_list?source_table=forum&source_field=forum_id&orderby=create_time%20desc"+parm2)
                            .get().build();
                    Call call2 = client2.newCall(request2);
                    int finalI = i;
                    call2.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.d(TAG, "onFailure: fail" + e.getMessage());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String resp_str = response.body().string();
                            Log.d(TAG, "onResponse: form comments replay  str is "+resp_str);
                            JSONObject _temp = JSONObject.parseObject(resp_str);
                            JSONArray replay_commentslist = _temp.getJSONObject("result").getJSONArray("list");
                            commentslist.getJSONObject(finalI).put("replay_list",replay_commentslist);
                        }
                    });
                }
                FormDetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.submitList(commentslist);

                    }
                });
            }
        });
    }

    private void getformdetail() {

        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        Log.d(TAG, "getformdetail: " + Config.baseaddr+"/forum/get_obj?forum_id="+id);
        Request request = new Request.Builder().url(Config.baseaddr+"/forum/get_obj?forum_id="+id)
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
                Log.d(TAG, "onResponse: form str is "+resp_str);
                JSONObject _temp = JSONObject.parseObject(resp_str);
                formdetail = _temp.getJSONObject("result").getJSONObject("obj");
                Config.addhits(FormDetailActivity.this,
                        "forum_id",
                        formdetail.getInteger("forum_id") + "",
                        "forum");
                Config.setHits(FormDetailActivity.this,
                        "forum_id",
                        formdetail.getInteger("forum_id") + "",
                        "forum",
                        formdetail.getInteger("hits")+1);
                FormDetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        title.setText(formdetail.getString("title"));
                        tag.setText(formdetail.getString("tag"));
                        username.setText(formdetail.getString("nickname"));
                        content.setText(formdetail.getString("content"));
                        clicksandlikes.setText(formdetail.getInteger("hits") +"点击   " + formdetail.getInteger("praise_len") + "点赞");
                        date.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(formdetail.getLong("create_time"))));
                        if(formdetail.containsKey("img")){
                            Glide.with(FormDetailActivity.this)
                                    .load(Config.basehttpaddr+formdetail.getString("img"))
                                    .centerCrop()
                                    .into(imageView);
                        }

                        if(getSharedPreferences("app",MODE_PRIVATE).getString("user_id","").equals(formdetail.getInteger("user_id")+"")){
                            delbtn.setVisibility(View.VISIBLE);
                            delbtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    removeitem(formdetail.getInteger("forum_id")+"");
                                    finish();
                                }
                            });
                        }
                    }
                });
            }
        });
    }
    private void removeitem(String tourismDynamicsId) {
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();

        Request request = new Request.Builder().url(Config.baseaddr+"/forum/del?forum_id=" + tourismDynamicsId )
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

            }
        });
    }
}