package com.zosoftware.tb.ui.home;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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

public class GongGaoListActivity extends AppCompatActivity {

    String searchval = "";
    EditText searched ;
    ImageView searchbtn ;
    RecyclerView recyclerView;
    JSONArray jsonArray = new JSONArray();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gong_gao_list);
        initview();
        initdata();
    }

    private void initdata() {
        getGonggaolist();
    }

    private void getGonggaolist() {
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        String parm = "";
        if(searchval.length()>0) {
            parm += "&title=" + searchval;
        }
        Request request = new Request.Builder().url(Config.baseaddr+"/notice/get_list?like=0"+parm )
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
                jsonArray = _temp.getJSONObject("result").getJSONArray("list");
                searchval = "";
                GongGaoListActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.submitList(jsonArray);
                    }
                });
            }
        });
    }
    BaseQuickAdapter adapter = new BaseQuickAdapter<JSONObject, QuickViewHolder>() {
        @NonNull
        @Override
        protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
            return new QuickViewHolder(R.layout.gonggao_list_item, viewGroup);
        }

        @Override
        protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable JSONObject objects) {
            ((TextView)quickViewHolder.getView(R.id.title)).setText(objects.getString("title"));
            ((TextView)quickViewHolder.getView(R.id.date)).setText( new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(objects.getLong("create_time"))));
        }
    };
    private void initview() {
        recyclerView = findViewById(R.id.recyclerview);
        searchbtn = findViewById(R.id.searchbtn);
        searched = findViewById(R.id.searched);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        searchbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchval= searched.getText().toString().trim();
                getGonggaolist();
            }
        });
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter baseQuickAdapter, @NonNull View view, int i) {
                Intent intent = new Intent(GongGaoListActivity.this,GongGaoDetailActivity.class);
                intent.putExtra("id",jsonArray.getJSONObject(i).getInteger("notice_id")+"");
                startActivity(intent);
            }
        });
    }
}