package com.zosoftware.tb;

import static android.content.ContentValues.TAG;
import static com.zosoftware.tb.ui.LoginActivity.encryptWithPublicKey;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zosoftware.tb.ui.LoginActivity;
import com.zosoftware.tb.ui.home.ZiliaoDetailActivity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Config {
    public static String baseaddr = "http://192.168.1.13:5000/api";
    public static String baseaddr_port = "http://192.168.1.13:5000";
    public static String basehttpaddr = "http://192.168.1.13:5000/";
    public static void getCollectState(Context context, String source_field, String source_id, String source_table,Callback callback){
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(Config.baseaddr+"/collect/count?source_field="+source_field +"&source_id="+source_id+"&source_table="+source_table + "&user_id="+
                        context.getSharedPreferences("app",Context.MODE_PRIVATE).getString("user_id",""))
                .get().build();
        Call call = client.newCall(request);
        call.enqueue( callback );
    }
    public static void getPraiseState(Context context, String source_field, String source_id, String source_table,Callback callback){
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(Config.baseaddr+"/praise/count?source_field="+source_field +"&source_id="+source_id+"&source_table="+source_table + "&user_id="+
                        context.getSharedPreferences("app",Context.MODE_PRIVATE).getString("user_id",""))
                .get().build();
        Call call = client.newCall(request);
        call.enqueue( callback );
    }
    public static void setpraise (Context context, String source_field, String source_id, String source_table,int praise_len){

        OkHttpClient okHttpClient = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("praise_len",praise_len);
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        RequestBody requestBody = RequestBody.create(JSON,jsonObject.toJSONString());

        //Form表单格式的参数传递

        Request request = new Request
                .Builder()
                .addHeader("X-Auth-Token",context.getSharedPreferences("app",Context.MODE_PRIVATE).getString("token",""))
                .post(requestBody)//Post请求的参数传递
                .url(Config.baseaddr+"/"+source_table+"/set?"+source_field+"="+source_id)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //此方法运行在子线程中，不能在此方法中进行UI操作。
                String result = response.body().string();
                Log.d("androixx.cn", result);

                response.body().close();
            }
        });
    }
    public static void setHits (Context context, String source_field, String source_id, String source_table,int hits){


        OkHttpClient okHttpClient = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("hits",hits);
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        RequestBody requestBody = RequestBody.create(JSON,jsonObject.toJSONString());

        //Form表单格式的参数传递

        Request request = new Request
                .Builder()
                .addHeader("X-Auth-Token",context.getSharedPreferences("app",Context.MODE_PRIVATE).getString("token",""))
                .post(requestBody)//Post请求的参数传递
                .url(Config.baseaddr+"/"+source_table+"/set?"+source_field+"="+source_id)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //此方法运行在子线程中，不能在此方法中进行UI操作。
                String result = response.body().string();
                Log.d("androixx.cn", result);

                response.body().close();
            }
        });
    }
    public static void addpraise (Context context, String source_field, String source_id, String source_table){
        OkHttpClient okHttpClient = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("source_field",source_field);
        jsonObject.put("source_table",source_table);
        jsonObject.put("source_id",source_id);
        jsonObject.put("user_id",context.getSharedPreferences("app",Context.MODE_PRIVATE).getString("user_id",""));
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        RequestBody requestBody = RequestBody.create(JSON,jsonObject.toJSONString());

        //Form表单格式的参数传递

        Request request = new Request
                .Builder()
                .addHeader("X-Auth-Token",context.getSharedPreferences("app",Context.MODE_PRIVATE).getString("token",""))
                .post(requestBody)//Post请求的参数传递
                .url(Config.baseaddr+"/praise/add")
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

                response.body().close();
            }
        });
    }
    public static void delpraise (Context context, String source_field, String source_id, String source_table){

        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(Config.baseaddr+"/praise/del?source_field="+source_field +"&source_id="+source_id+"&source_table="+source_table + "&user_id="+
                context.getSharedPreferences("app",Context.MODE_PRIVATE).getString("user_id",""))
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
                Log.d(TAG, "onResponse: unpraise str is "+resp_str);

            }
        });
    }
    public static void delcollect (Context context, String source_field, String source_id, String source_table){

        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(Config.baseaddr+"/collect/del?source_field="+source_field +"&source_id="+source_id+"&source_table="+source_table + "&user_id="+
                        context.getSharedPreferences("app",Context.MODE_PRIVATE).getString("user_id",""))
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
                Log.d(TAG, "onResponse: unpraise str is "+resp_str);

            }
        });
    }
    public static void addcollect  (Context context, String source_field, String source_id, String source_table,String title,String img){


        OkHttpClient okHttpClient = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("source_field",source_field);
        jsonObject.put("source_table",source_table);
        jsonObject.put("source_id",source_id);
        jsonObject.put("title",title);
        jsonObject.put("img",img);
        jsonObject.put("user_id",context.getSharedPreferences("app",Context.MODE_PRIVATE).getString("user_id",""));
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        RequestBody requestBody = RequestBody.create(JSON,jsonObject.toJSONString());

        //Form表单格式的参数传递

        Request request = new Request
                .Builder()
                .addHeader("X-Auth-Token",context.getSharedPreferences("app",Context.MODE_PRIVATE).getString("token",""))
                .post(requestBody)//Post请求的参数传递
                .url(Config.baseaddr+"/collect/add")
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //此方法运行在子线程中，不能在此方法中进行UI操作。
                String result = response.body().string();
                Log.d("androixx.cn", result);
                response.body().close();
            }
        });

    }
    public static void addhits  (Context context, String source_field, String source_id, String source_table){


                OkHttpClient okHttpClient = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("source_field",source_field);
        jsonObject.put("source_table",source_table);
        jsonObject.put("source_id",source_id);
        jsonObject.put("user_id",context.getSharedPreferences("app",Context.MODE_PRIVATE).getString("user_id",""));
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        RequestBody requestBody = RequestBody.create(JSON,jsonObject.toJSONString());

        //Form表单格式的参数传递

        Request request = new Request
                .Builder()
                .addHeader("X-Auth-Token",context.getSharedPreferences("app",Context.MODE_PRIVATE).getString("token",""))
                .post(requestBody)//Post请求的参数传递
                .url(Config.baseaddr+"/hits/add?")
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {}

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //此方法运行在子线程中，不能在此方法中进行UI操作。
                String result = response.body().string();
                Log.d("androixx.cn", result);
                response.body().close();
            }
        });
    }
}
