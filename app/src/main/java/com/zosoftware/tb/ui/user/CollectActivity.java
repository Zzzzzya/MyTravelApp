package com.zosoftware.tb.ui.user;

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
import com.zosoftware.tb.ui.LoginActivity;
import com.zosoftware.tb.ui.dashboard.CreateFormActivity;
import com.zosoftware.tb.ui.home.AddCommentActivity;
import com.zosoftware.tb.ui.home.DongTaiDetailActivity;
import com.zosoftware.tb.ui.home.HuoDongDetailActivity;
import com.zosoftware.tb.ui.home.ZiliaoDetailActivity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CollectActivity extends AppCompatActivity {

    EditText searched;
    ImageView searchbtn;
    RecyclerView recyclerView;
    String user_id,token;
    String searchval="";
    JSONArray jsonArray = new JSONArray();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_collect);
        token = getSharedPreferences("app",MODE_PRIVATE).getString("token","");
        user_id = getSharedPreferences("app",MODE_PRIVATE).getString("user_id","");
        initview();
        initdata();
    }

    private void initdata() {
        String parm="";
        if(searchval != "")
            parm = "&title=" + searchval;
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        String url = Config.baseaddr+"/collect/get_list?user_id="+user_id + parm;
        Log.d(TAG, "initdata:  " + url);
        Request request = new Request.Builder().url(url)
                .addHeader("X-Auth-Token",token)
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
                Log.d(TAG, "onResponse: get collect str is "+resp_str);
                JSONObject jsonObject = JSONObject.parseObject(resp_str);
                if (jsonObject.containsKey("error")) {
                    return;
                } else {
                    CollectActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            jsonArray = jsonObject.getJSONObject("result").getJSONArray("list");
                            adapter.submitList(jsonArray);
                            searchval  ="";
                        }
                    });
                }
            }
        });
    }
    BaseQuickAdapter adapter = new BaseQuickAdapter<JSONObject, QuickViewHolder>() {
        @NonNull
        @Override
        protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
            return new QuickViewHolder(R.layout.collect_item, viewGroup);
        }

        @Override
        protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable JSONObject objects) {
            if(objects.containsKey("img")){
                Glide.with(CollectActivity.this)
                        .load(Config.basehttpaddr+objects.getString("img"))
                        .centerCrop()
                        .into( (ImageView) quickViewHolder.getView(R.id.image));
            }
            ((TextView)quickViewHolder.getView(R.id.name)).setText(objects.getString("title"));
            ((TextView)quickViewHolder.getView(R.id.date)).setText( new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(objects.getLong("create_time"))));
            ((Button) quickViewHolder.getView(R.id.delbtn)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
                    String url = Config.baseaddr+"/collect/del?collect_id="+jsonArray.getJSONObject(i).getInteger("collect_id") ;
                    Log.d(TAG, "initdata:  " + url);
                    Request request = new Request.Builder().url(url)
                            .addHeader("X-Auth-Token",token)
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
                            Log.d(TAG, "onResponse: get collect str is "+resp_str);
                            JSONObject jsonObject = JSONObject.parseObject(resp_str);
                            if (jsonObject.containsKey("error")) {
                                return;
                            } else {
                                initdata();
                            }
                        }
                    });
                }
            });
        }
    };
    private void initview() {
        searchbtn = findViewById(R.id.searchbtn);
        searched = findViewById(R.id.searched);
        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        searchbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchval = searched.getText().toString().trim();
                initdata();
            }
        });
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter baseQuickAdapter, @NonNull View view, int i) {
                Intent intent = null;
                if(jsonArray.getJSONObject(i).getString("source_table").equals("tourism_materials")){
                    intent = new Intent(CollectActivity.this,ZiliaoDetailActivity.class);
                    intent.putExtra("id",jsonArray.getJSONObject(i).getInteger("source_id")+"");
                }
                if(jsonArray.getJSONObject(i).getString("source_table").equals("tourism_activities")){
                    intent = new Intent(CollectActivity.this,HuoDongDetailActivity.class);
                    intent.putExtra("id",jsonArray.getJSONObject(i).getInteger("source_id")+"");
                }
                if(jsonArray.getJSONObject(i).getString("source_table").equals("tourism_dynamics")){
                    intent = new Intent(CollectActivity.this,DongTaiDetailActivity.class);
                    intent.putExtra("id",jsonArray.getJSONObject(i).getInteger("source_id")+"");
                }
                if(null != intent )
                startActivity(intent);
            }
        });
    }
}