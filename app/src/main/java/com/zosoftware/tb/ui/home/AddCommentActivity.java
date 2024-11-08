package com.zosoftware.tb.ui.home;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.zosoftware.tb.Config;
import com.zosoftware.tb.R;
import com.zosoftware.tb.ui.LoginActivity;
import com.zosoftware.tb.ui.RegistActivity;
import com.zosoftware.tb.ui.user.UserInfoActivity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddCommentActivity extends AppCompatActivity {

    EditText editTextTextMultiLine;
    Button confirm;
    int id = 1;
    JSONObject userinfojson;
    String source_field,source_table,source_id;
    String reply_to_id = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_comment);
        initview();
        initdata();
        
    }

    private void initdata() {
        id= getIntent().getIntExtra("id",1);
        source_field= getIntent().getStringExtra("source_field");
        source_table= getIntent().getStringExtra("source_table");
        source_id= getIntent().getStringExtra("source_id");
        String replay_user= getIntent().getStringExtra("replay_user");
        if(replay_user != null && replay_user.length()>0) {
            reply_to_id  = getIntent().getStringExtra("reply_to_id");
            ((TextView)  findViewById(R.id.textView4)).setText("对\""+replay_user+"\"进行回复");
        }else {
            findViewById(R.id.textView4).setVisibility(View.INVISIBLE);
        }
        getUserInfo(getSharedPreferences("app",MODE_PRIVATE).getString("token",""));
    }
    private void getUserInfo(String token) {


        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(Config.baseaddr+"/user/state")
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
                Log.d(TAG, "onResponse:  str is "+resp_str);
                JSONObject jsonObject = JSONObject.parseObject(resp_str);
                if (jsonObject.containsKey("error")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(AddCommentActivity.this,"登录超时了",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(AddCommentActivity.this, LoginActivity.class));
                            finish();
                            return;
                        }
                    });
                    return;
                }
                userinfojson = jsonObject.getJSONObject("result").getJSONObject("obj");
                Log.d(TAG, "onResponse: "+userinfojson);

            }
        });
    }
    private void initview() {
        editTextTextMultiLine = findViewById(R.id.editTextTextMultiLine);
        confirm = findViewById(R.id.button2);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                OkHttpClient okHttpClient = new OkHttpClient();
                JSONObject reqjson = new JSONObject();
                reqjson.put("avatar",userinfojson.getString("avatar"));
                reqjson.put("content", editTextTextMultiLine.getText().toString().trim());
                reqjson.put("nickname", userinfojson.getString("nickname"));
                reqjson.put("source_field", source_field);
                reqjson.put("source_id", source_id);
                reqjson.put("source_table", source_table);
                if(reply_to_id != "")
                    reqjson.put("reply_to_id", reply_to_id);

                reqjson.put("user_id", userinfojson.getString("user_id"));
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                Log.d(TAG, "onClick:  comment/add reqjson " + reqjson.toJSONString());
                RequestBody requestBody = RequestBody.create(JSON,reqjson.toJSONString());

                //Form表单格式的参数传递

                Request request = new Request
                        .Builder()
                        .post(requestBody)//Post请求的参数传递
                        .addHeader("X-Auth-Token",getSharedPreferences("app",MODE_PRIVATE).getString("token",""))
                        .url(Config.baseaddr+"/comment/add")
                        .build();
                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        //此方法运行在子线程中，不能在此方法中进行UI操作。
                        String result = response.body().string();
                        Log.d("androixx.cn", result);
                        JSONObject jsonObject1 = JSONObject.parseObject(result);
                        if (jsonObject1.containsKey("result")) {
                            AddCommentActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(AddCommentActivity.this,"评论成功！",Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                        }else {
                            AddCommentActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(AddCommentActivity.this,"评论失敗！",Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                        }
                    }
                });
            }
        });

    }
}