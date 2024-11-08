package com.zosoftware.tb.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alibaba.fastjson.JSONObject;
import com.zosoftware.tb.Config;
import com.zosoftware.tb.MainActivity;
import com.zosoftware.tb.R;

import java.io.IOException;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ForgetActivity extends AppCompatActivity {

    Button findpass,sendcap;
    ImageView imageView;
    EditText pass1,pass2,username,email,capture;
    TextView regist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forget);
        initview();
    }

    private void initview() {
        username = findViewById(R.id.username);
        regist = findViewById(R.id.createnewuser);
        findpass = findViewById(R.id.findpass);
        imageView = findViewById(R.id.imageView);
        pass1 = findViewById(R.id.password);
        pass2 = findViewById(R.id.password2);
        email = findViewById(R.id.email);
        capture = findViewById(R.id.capture);
        sendcap = findViewById(R.id.sendcap);

        sendcap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                capture.setText(getRandomString(6));
            }
        });
        findpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OkHttpClient okHttpClient = new OkHttpClient();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username",username.getText().toString().trim());
                jsonObject.put("password",pass1.getText().toString().trim());
                jsonObject.put("confirm_password",pass2.getText().toString().trim());
                jsonObject.put("email",email.getText().toString().trim());
                jsonObject.put("code",capture.getText().toString().trim());
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody requestBody = RequestBody.create(JSON,jsonObject.toJSONString());
                //Form表单格式的参数传递
                Request request = new Request
                        .Builder()
                        .post(requestBody)//Post请求的参数传递
                        .url(Config.baseaddr+"/user/forget_password")
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
                        if (jsonObject1.getJSONObject("error") != null) {
                            ForgetActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ForgetActivity.this, jsonObject1.getJSONObject("error").getString("message"), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            ForgetActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ForgetActivity.this, "修改成功！", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(ForgetActivity.this, LoginActivity.class));
                                    finish();
                                }
                            });
                        }
                        response.body().close();
                    }
                });
            }
        });
        regist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ForgetActivity.this, RegistActivity.class));
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
    public static String getRandomString(int length){
        String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random=new Random();
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<length;i++){
            int number=random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }
}