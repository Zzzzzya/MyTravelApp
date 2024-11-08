package com.zosoftware.tb.ui.user;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.zosoftware.tb.Config;
import com.zosoftware.tb.R;
import com.zosoftware.tb.ui.LoginActivity;
import com.zosoftware.tb.ui.home.AddCommentActivity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserInfoActivity extends AppCompatActivity {

    TextView nickname;
    ImageView avator;
    LinearLayout changepass,changeinfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_info);
        initview();
        initdata();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initdata();
    }

    private void initdata() {

        SharedPreferences sharedPreferences=  getSharedPreferences("app", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");
        if(token.length()==0 )
        {
            startActivity(new Intent(UserInfoActivity.this, LoginActivity.class));
            finish();
            return;
        }else {
            getUserInfo(token);
        }

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
                    Toast.makeText(UserInfoActivity.this,"登录超时了",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(UserInfoActivity.this, LoginActivity.class));
                    finish();
                    return;
                }
                JSONObject userinfojson = jsonObject.getJSONObject("result").getJSONObject("obj");
                Log.d(TAG, "onResponse: "+userinfojson);
                UserInfoActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        nickname.setText(userinfojson.getString("nickname"));
                        if(userinfojson.containsKey("avatar"))
                            Glide.with(UserInfoActivity.this)
                                    .load(Config.basehttpaddr+userinfojson.getString("avatar"))
                                    .centerCrop()
                                    .into(avator);

                    }
                });
            }
        });
    }
    private void initview() {
        nickname = findViewById(R.id.nickname);
        avator= findViewById(R.id.avator);
        changepass = findViewById(R.id.changepass);
        changeinfo = findViewById(R.id.changeinfo);

        changepass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserInfoActivity.this,ChangePassActivity.class));
            }
        });
        changeinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(UserInfoActivity.this,ChangeUserInfoActivity.class));
            }
        });
    }
}