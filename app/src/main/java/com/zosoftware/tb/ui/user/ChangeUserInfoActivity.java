package com.zosoftware.tb.ui.user;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;
import com.zosoftware.tb.Config;
import com.zosoftware.tb.R;
import com.zosoftware.tb.ui.LoginActivity;
import com.zosoftware.tb.ui.RegistActivity;
import com.zosoftware.tb.ui.home.AddCommentActivity;
import com.zosoftware.tb.utils.GlideEngine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChangeUserInfoActivity extends AppCompatActivity {

    ImageView avator;
    EditText nickname,email,username,phone;
    Spinner gender;
    Button confirm,cancel;
    TextView account,password;
    JSONObject userinfojson = new JSONObject();
    List<String> genderlist = new ArrayList<>();
    private JSONObject regular_users_json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_user_info);
        initview();
        initdata();
    }

    private void initdata() {
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
                    Toast.makeText(ChangeUserInfoActivity.this,"登录超时了",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ChangeUserInfoActivity.this, LoginActivity.class));
                    finish();
                    return;
                }
                userinfojson = jsonObject.getJSONObject("result").getJSONObject("obj");
                Log.d(TAG, "onResponse: "+userinfojson);
                ChangeUserInfoActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if(userinfojson.containsKey("avatar")  &&userinfojson.getString("avatar") != null && userinfojson.getString("avatar") !="null"  ){
                            Log.d(TAG, "run: ===================================== has avatar");
                            Glide.with(ChangeUserInfoActivity.this)
                                    .load(Config.basehttpaddr+userinfojson.getString("avatar"))
                                    .centerCrop()
                                    .into( avator);
                        }else{
                            Log.d(TAG, "run: ===================================== no avatar");
                            avator.setImageResource(R.drawable.def2);
                        }
                        username.setText(userinfojson.getString("username"));
                        account.setText(userinfojson.getString("username"));
                        password.setText(userinfojson.getString("password"));
                        nickname.setText(userinfojson.getString("nickname"));
                        email.setText(userinfojson.getString("email"));
                    //  phone.setText(userinfojson.getString("phone"));


                        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
                        Request request = new Request.Builder().url(Config.baseaddr+"/regular_users/get_obj?user_id=" + userinfojson.getInteger("user_id") )
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
                                regular_users_json  = jsonObject.getJSONObject("result").getJSONObject("obj");
                                Log.d(TAG, "onResponse: "+userinfojson);
                                ChangeUserInfoActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        phone.setText(regular_users_json.getString("contact_phone_number"));
                                        if(regular_users_json.getString("user_gender").equals("男")){
                                            gender.setSelection(0);
                                        }else {
                                            gender.setSelection(1);
                                        }
//                phone.setText(userinfojson.getString("phone"));
                                    }
                                });

                            }
                        });
                    }
                });

            }
        });
    }

    private void initview() {
        avator = findViewById(R.id.avator);
        nickname = findViewById(R.id.nickname);
        email = findViewById(R.id.email);
        username = findViewById(R.id.username);
        phone = findViewById(R.id.phone);
        gender = findViewById(R.id.gender);
        confirm = findViewById(R.id.confirm);
        cancel = findViewById(R.id.cancel);
        account = findViewById(R.id.account);
        password = findViewById(R.id.password);
        genderlist.add("男");
        genderlist.add("女");
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genderlist);

        gender.setAdapter(adapter);
        avator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PictureSelector.create(ChangeUserInfoActivity.this)
                        .openGallery(SelectMimeType.ofImage())
                        .setImageEngine(GlideEngine.createGlideEngine())
                        .setMaxSelectNum(1)
                        .forResult(new OnResultCallbackListener<LocalMedia>() {
                            @Override
                            public void onResult(ArrayList<LocalMedia> result) {
                                uploadImage(new File(result.get(0).getRealPath()));
                            }

                            @Override
                            public void onCancel() {

                            }
                        });
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setuserinfo();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void setuserinfo() {
        OkHttpClient okHttpClient = new OkHttpClient();
        JSONObject addphonejson = new JSONObject();
        addphonejson.put("avatar",userinfojson.getString("avatar"));
        addphonejson.put("email", email.getText().toString().trim());
        addphonejson.put("email_state",0);
        addphonejson.put("nickname", nickname.getText().toString().trim());
        addphonejson.put("password", userinfojson.getString("password"));
        addphonejson.put("phone_state",0);
        addphonejson.put("state",1);
        addphonejson.put("user_group","普通用户");
        addphonejson.put("user_id",userinfojson.getInteger("user_id"));
        addphonejson.put("username",username.getText().toString().trim());
        addphonejson.put("vip_discount",0);
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        RequestBody requestBody = RequestBody.create(JSON,addphonejson.toJSONString());

        //Form表单格式的参数传递

        Request request = new Request
                .Builder()
                .addHeader("X-Auth-Token",ChangeUserInfoActivity.this.getSharedPreferences("app",MODE_PRIVATE).getString("token",""))
                .post(requestBody)//Post请求的参数传递
                .url(Config.baseaddr+"/user/set?user_id="+userinfojson.getInteger("user_id"))
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
                    OkHttpClient okHttpClient2 = new OkHttpClient();
                    JSONObject addphonejson2 = new JSONObject();
                    addphonejson2.put("contact_phone_number", phone.getText().toString());
                    addphonejson2.put("regular_users_id", regular_users_json.getString("regular_users_id"));
                    addphonejson2.put("user_gender", gender.getSelectedItem() );
                    addphonejson2.put("user_id", userinfojson.getString("user_id") );
                    addphonejson2.put("user_name",username.getText().toString());
                    MediaType JSON = MediaType.parse("application/json; charset=utf-8");

                    RequestBody requestBody = RequestBody.create(JSON,addphonejson2.toJSONString());

                    //Form表单格式的参数传递

                    Request request = new Request
                            .Builder()
                            .addHeader("X-Auth-Token",ChangeUserInfoActivity.this.getSharedPreferences("app",MODE_PRIVATE).getString("token",""))
                            .post(requestBody)//Post请求的参数传递
                            .url(Config.baseaddr+"/regular_users/set?regular_users_id=" +  regular_users_json.getString("regular_users_id"))
                            .build();
                    okHttpClient2.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                        }

                        @Override
                        public void onResponse(Call call, Response response2) throws IOException {
                            //此方法运行在子线程中，不能在此方法中进行UI操作。
                            String result = response2.body().string();
                            Log.d("androixx.cn", result);
                            JSONObject jsonObject1 = JSONObject.parseObject(result);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ChangeUserInfoActivity.this,"修改完成",Toast.LENGTH_SHORT).show();
                                }
                            });
                            finish();
                        }
                    });
                }
            }
        });
    }

    public  void uploadImage(File file) {

        OkHttpClient httpClient = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/octet-stream");//设置类型，类型为八位字节流
        RequestBody requestBody = RequestBody.create(mediaType, file);//把文件与类型放入请求体

        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), requestBody)//文件名
                .build();
        Request request = new Request.Builder()
                .url(Config.baseaddr+"/user/upload")
                .post(multipartBody)
                .build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //此方法运行在子线程中，不能在此方法中进行UI操作。
                String result = response.body().string();
                Log.d("upload file :", result);
                JSONObject jsonObject1 = JSONObject.parseObject(result);
                if (jsonObject1.getJSONObject("result")!= null) {
                    ChangeUserInfoActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            userinfojson.put("avatar",jsonObject1.getJSONObject("result").getString("url"));
                            Glide.with(ChangeUserInfoActivity.this)
                                    .load(Config.basehttpaddr+userinfojson.getString("avatar"))
                                    .centerCrop()
                                    .into( avator);
                        }
                    });
                }
            }
        });


    }
}