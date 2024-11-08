package com.zosoftware.tb.ui.dashboard;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;
import com.zosoftware.tb.Config;
import com.zosoftware.tb.MainActivity;
import com.zosoftware.tb.R;
import com.zosoftware.tb.ui.LoginActivity;
import com.zosoftware.tb.ui.user.ChangeUserInfoActivity;
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

public class CreateFormActivity extends AppCompatActivity {

    ImageView upload;
    Button confirm;
    EditText content,tag,title;
    Spinner spinner;
    JSONArray form_type_list = new JSONArray();
    JSONObject userinfo = new JSONObject();
    List<String> type_list = new ArrayList<>();
    String image_url = "";
    ArrayAdapter type_adapter  = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_form);
        initview();
        initdata();
    }

    private void initdata() {
        getUserInfo(getSharedPreferences("app",MODE_PRIVATE).getString("token",""));
        getclassinfo();
    }

    private void getclassinfo() {

        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(Config.baseaddr+"/forum_type/get_list")
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
                form_type_list = _temp.getJSONObject("result").getJSONArray("list");

                CreateFormActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        type_list.clear();
                        for(int i =0 ; i <  form_type_list.size() ;i++){
                            type_list.add(form_type_list.getJSONObject(i).getString("name"));
                        }

                        type_adapter.notifyDataSetChanged();
                        spinner.setSelection(0);
                    }
                });
            }
        });
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
                    CreateFormActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CreateFormActivity.this,"登录超时了",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(CreateFormActivity.this, LoginActivity.class));
                            CreateFormActivity.this.finish();

                        }
                    });
                    return;
                }
                userinfo = jsonObject.getJSONObject("result").getJSONObject("obj");
            }
        });
    }

    private void initview() {
        spinner = findViewById(R.id.spinner);
        title = findViewById(R.id.title);
        tag = findViewById(R.id.tag);
        content = findViewById(R.id.content);
        confirm = findViewById(R.id.confirm);
        upload = findViewById(R.id.image);
        type_adapter  = new ArrayAdapter(CreateFormActivity.this, android.R.layout.simple_spinner_dropdown_item,type_list);
        spinner.setAdapter(type_adapter);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PictureSelector.create(CreateFormActivity.this)
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
                makeform();
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
                .url(Config.baseaddr + "/user/upload")
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
                if (jsonObject1.containsKey("result") ) {
                    CreateFormActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            image_url =jsonObject1.getJSONObject("result").getString("url");
                            Glide.with(CreateFormActivity.this)
                                    .load(Config.basehttpaddr +  image_url)
                                    .centerCrop()
                                    .into(upload);
                        }
                    });
                }
            }
        });

    }
    private void makeform() {

        OkHttpClient okHttpClient = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("avatar",userinfo.getString("avatar"));
        jsonObject.put("content",content.getText().toString());
        jsonObject.put("description","");
        jsonObject.put("display",0);
        jsonObject.put("forum_id",0);
        jsonObject.put("hits",0);
        jsonObject.put("img",image_url);
        jsonObject.put("keywords","");
        jsonObject.put("url","");
        jsonObject.put("user_id",userinfo.getString("user_id"));
        jsonObject.put("nickname",userinfo.getString("nickname"));
        jsonObject.put("tag",title.getText().toString());
        jsonObject.put("title",title.getText().toString());
        jsonObject.put("type", type_list.get(spinner.getSelectedItemPosition()));
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        RequestBody requestBody = RequestBody.create(JSON,jsonObject.toJSONString());

        //Form表单格式的参数传递

        Request request = new Request
                .Builder()
                .post(requestBody)//Post请求的参数传递
                .url(Config.baseaddr+"/forum/add")
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
                if(jsonObject1.getJSONObject("error") != null){
                    CreateFormActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(  CreateFormActivity.this,"发布失敗！",Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }else {
                    CreateFormActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(  CreateFormActivity.this,"发布成功！",Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
                response.body().close();
            }
        });

    }
}