package com.zosoftware.tb.ui.home;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zosoftware.tb.Config;
import com.zosoftware.tb.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GongGaoDetailActivity extends AppCompatActivity {

    TextView content,title,date;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gong_gao_detail);
        id = getIntent().getStringExtra("id");
        initview();
        initdata();
    }

    private void initview() {
        date = findViewById(R.id.date);
        title = findViewById(R.id.title);
        content=  findViewById(R.id.content);
    }

    private void initdata() {
        
        getGongGaoDetail();
    }

    private void getGongGaoDetail() {
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(Config.baseaddr+"/notice/get_obj?notice_id="+id)
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
                JSONObject contentjson = jsonObject.getJSONObject("result").getJSONObject("obj");
                GongGaoDetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        content.setText(contentjson.getString("content"));
                        title.setText(contentjson.getString("title"));
                        date.setText( new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(contentjson.getLong("create_time"))));
                    }
                });
            }
        });
    }
}