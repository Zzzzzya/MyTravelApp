package com.zosoftware.tb.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alibaba.fastjson.JSONObject;
import com.zosoftware.tb.Config;
import com.zosoftware.tb.R;
import com.zosoftware.tb.ui.LoginActivity;
import com.zosoftware.tb.ui.RegistActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChangePassActivity extends AppCompatActivity {

    Button resetpass;
    EditText originpass,newpass1,newpass2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_pass);
        initview();
        initdata();
    }

    private void initdata() {

    }

    private void initview() {
        String token = getSharedPreferences("app",MODE_PRIVATE).getString("token","");
        resetpass=findViewById(R.id.resetpass);
        originpass=findViewById(R.id.originpass);
        newpass1=findViewById(R.id.newpass1);
        newpass2=findViewById(R.id.newpass2);
        resetpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!newpass1.getText().toString().trim().equals(newpass2.getText().toString().trim())){

                }else {
                    OkHttpClient okHttpClient = new OkHttpClient();
                    JSONObject addphonejson = new JSONObject();
                    addphonejson.put("o_password",originpass.getText().toString());
                    addphonejson.put("password", newpass1.getText().toString());
                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");

                    RequestBody requestBody = RequestBody.create(JSON,addphonejson.toJSONString());

                    //Form表单格式的参数传递

                    Request request = new Request
                            .Builder()
                            .addHeader("X-Auth-Token",token)
                            .post(requestBody)//Post请求的参数传递
                            .url(Config.baseaddr+"/user/change_password")
                            .build();
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Toast.makeText(ChangePassActivity.this,"修改密码失败！",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            //此方法运行在子线程中，不能在此方法中进行UI操作。
                            String result = response.body().string();
                            Log.d("androixx.cn", result);
                            JSONObject jsonObject1 = JSONObject.parseObject(result);
                            if (jsonObject1.containsKey("result")) {
                                ChangePassActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(ChangePassActivity.this,"修改密码成功！",Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(ChangePassActivity.this, LoginActivity.class));
                                        finish();
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }
}