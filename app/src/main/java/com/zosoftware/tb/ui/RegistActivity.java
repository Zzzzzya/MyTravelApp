package com.zosoftware.tb.ui;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zosoftware.tb.Config;
import com.zosoftware.tb.MainActivity;
import com.zosoftware.tb.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegistActivity extends AppCompatActivity {

    EditText username,password,password2,nickname,email,name,phone;
    Spinner gender;
    Button registbtn;
    TextView login,forgetpass;
    List<String> genderlist = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_regist);
        initview();
    }

    private void initview() {
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        password2 = findViewById(R.id.password2);
        nickname = findViewById(R.id.nickname);
        email = findViewById(R.id.email);
        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        gender = findViewById(R.id.gender);
        registbtn = findViewById(R.id.registbtn);
        login = findViewById(R.id.login);
        forgetpass = findViewById(R.id.forgetpass);
        forgetpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegistActivity.this, ForgetActivity.class));
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegistActivity.this, LoginActivity.class));
            }
        });
        registbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doregist();
            }
        });
        genderlist.add("男");
        genderlist.add("女");
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genderlist);

        gender.setAdapter(adapter);

    }

    private void doregist() {
        if(!password.getText().toString().trim().equals(password2.getText().toString().trim())){
            Toast.makeText(this,"两次输入密码不一致",Toast.LENGTH_SHORT).show();
            return;
        }
        if(
                username.getText().toString().trim().length() ==0 ||
                nickname.getText().toString().trim().length() ==0 ||
                password.getText().toString().trim().length() ==0 ||
                phone.getText().toString().trim().length() ==0 ||
                password2.getText().toString().trim().length() ==0 ||
                email.getText().toString().trim().length() ==0
        ){
            Toast.makeText(this,"请完整填写信息",Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient okHttpClient = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username",username.getText().toString().trim());
        jsonObject.put("user_group","普通用户");
        jsonObject.put("nickname",nickname.getText().toString().trim());
        jsonObject.put("email",email.getText().toString().trim());
        jsonObject.put("password",password.getText().toString().trim());
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        RequestBody requestBody = RequestBody.create(JSON,jsonObject.toJSONString());

        //Form表单格式的参数传递

        Request request = new Request
                .Builder()
                .post(requestBody)//Post请求的参数传递
                .url(Config.baseaddr+"/user/register?")
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //此方法运行在子线程中，不能在此方法中进行UI操作。
                String result = response.body().string();
                Log.d("androixx.cn", result);
                JSONObject jsonObject1 = JSONObject.parseObject(result);
                if(jsonObject1.getInteger("result") !=1){
                    RegistActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(  RegistActivity.this,"注册失败！",Toast.LENGTH_SHORT).show();

                        }
                    });
                }else {

                    OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
                    Request request = new Request.Builder().url(Config.baseaddr+"/user/get_obj?"
                                            + "nickname=" +  nickname.getText().toString().trim()
                                            + "&username=" +username.getText().toString().trim()
                                            + "&email=" +email.getText().toString().trim()

                                    )
                            .get().build();
                    Call call2 = client.newCall(request);
                    call2.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.d(TAG, "onFailure: fail" + e.getMessage());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String resp_str = response.body().string();
                            Log.d(TAG, "onResponse:  str is "+resp_str);
                            JSONObject jsonObject = JSONObject.parseObject(resp_str);
                            JSONObject userinfojsonObject = jsonObject.getJSONObject("result").getJSONObject("obj");

                            if(userinfojsonObject != null) {
                                OkHttpClient okHttpClient = new OkHttpClient();
                                JSONObject addphonejson = new JSONObject();
                                addphonejson.put("contact_phone_number",phone.getText().toString().trim());
                                addphonejson.put("user_gender", (String) gender.getSelectedItem());
                                addphonejson.put("user_id", userinfojsonObject.getInteger("user_id"));
                                addphonejson.put("user_name", userinfojsonObject.getString("user_name"));
                                MediaType JSON = MediaType.parse("application/json; charset=utf-8");

                                RequestBody requestBody = RequestBody.create(JSON,addphonejson.toJSONString());

                                //Form表单格式的参数传递

                                Request request = new Request
                                        .Builder()
                                        .post(requestBody)//Post请求的参数传递
                                        .url(Config.baseaddr+"/regular_users/add")
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
                                            RegistActivity.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(RegistActivity.this,"注册成功！",Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(RegistActivity.this, LoginActivity.class));
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
                response.body().close();
            }
        });

    }
}