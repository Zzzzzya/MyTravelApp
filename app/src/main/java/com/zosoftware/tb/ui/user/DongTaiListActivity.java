package com.zosoftware.tb.ui.user;

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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.zosoftware.tb.Config;
import com.zosoftware.tb.R;
import com.zosoftware.tb.ui.home.DongTaiDetailActivity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DongTaiListActivity extends AppCompatActivity {

    TextView searchbtn,resetbtn,createbtn;
    RecyclerView recyclerView;
    EditText searched;
    String searchval ="";
    private String user_id;
    private String token;
    private JSONArray formlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dong_tai_list);
        token = getSharedPreferences("app",MODE_PRIVATE).getString("token","");
        user_id = getSharedPreferences("app",MODE_PRIVATE).getString("user_id","");
        initview();
        initdata();
    }

    private void initdata() {
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        String parm = "";
        if(searchval.length()>0) {
            parm += "&dynamic_title=" + searchval;
        }
        Request request = new Request.Builder().url(Config.baseaddr+"/tourism_dynamics/get_list?like=0" + "&sqlwhere=(publish_users = " +user_id +")" +parm )
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
                formlist = _temp.getJSONObject("result").getJSONArray("list");
                Log.d(TAG, "onResponse: "+formlist);
                DongTaiListActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.submitList(formlist);
                    }
                });
            }
        });
        searchval = "";
    }

    private void initview() {
        searched = findViewById(R.id.searched);
        searchbtn = findViewById(R.id.searchbtn);
        createbtn = findViewById(R.id.createbtn);
        recyclerView = findViewById(R.id.recyclerview);
        resetbtn = findViewById(R.id.resetbtn);
        resetbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter baseQuickAdapter, @NonNull View view, int i) {
                Intent intent = new Intent(DongTaiListActivity.this, DongTaiDetailActivity.class);
                intent.putExtra("id",formlist.getJSONObject(i).getInteger("tourism_dynamics_id")+"");
                startActivity(intent);
            }
        });
        searchbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchval = searched.getText().toString();
                initdata();
            }
        });
        resetbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searched.setText("");
                searchval = "";
                initdata();
            }
        });
        createbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DongTaiListActivity.this,CreateDongTaiActivity.class);
                startActivity(intent);
            }
        });
    }
    BaseQuickAdapter adapter = new BaseQuickAdapter<JSONObject, QuickViewHolder>() {
        @NonNull
        @Override
        protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
            return new QuickViewHolder(R.layout.dongtai_item_s, viewGroup);
        }

        @Override
        protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable JSONObject objects) {
//            ((TextView) quickViewHolder.getView(R.id.gonggaotv)).setText(objects);
            if(objects.containsKey("dynamic_images")){
                Glide.with(DongTaiListActivity.this)
                        .load(Config.basehttpaddr+objects.getString("dynamic_images"))
                        .centerCrop()
                        .into( (ImageView) quickViewHolder.getView(R.id.image));
            }
            ((TextView)quickViewHolder.getView(R.id.name)).setText( objects.getString("dynamic_title"));
            ((TextView)quickViewHolder.getView(R.id.state)).setText( objects.getString("mood_state"));
            ((TextView)quickViewHolder.getView(R.id.releasetime)).setText( objects.getString("release_date"));
            ((TextView)quickViewHolder.getView(R.id.likes)).setText( objects.getString("praise_len"));
            ((TextView)quickViewHolder.getView(R.id.clicks)).setText( objects.getString("hits"));
            ((TextView) quickViewHolder.getView(R.id.date)).setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(objects.getLong("create_time"))));
            ((TextView) quickViewHolder.getView(R.id.detailbtn)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(DongTaiListActivity.this, DongTaiDetailActivity.class);
                    intent.putExtra("id",formlist.getJSONObject(i).getInteger("tourism_dynamics_id")+"");
                    startActivity(intent);
                }
            });
            ((TextView) quickViewHolder.getView(R.id.delbtn)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeitem(formlist.getJSONObject(i).getInteger("tourism_dynamics_id")+"");
                }
            });
        }
    };

    private void removeitem(String tourismDynamicsId) {
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();

        Request request = new Request.Builder().url(Config.baseaddr+"/tourism_dynamics/del?tourism_dynamics_id=" + tourismDynamicsId )
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        searchval="";
                        searched.setText("");
                        initdata();
                    }
                });
            }
        });
    }
}