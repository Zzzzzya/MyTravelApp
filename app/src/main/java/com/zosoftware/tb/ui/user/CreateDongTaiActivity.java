package com.zosoftware.tb.ui.user;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
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
import com.amap.api.interfaces.IMarker;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.bumptech.glide.Glide;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.SelectMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.interfaces.OnResultCallbackListener;
import com.zosoftware.tb.Config;
import com.zosoftware.tb.MainActivity;
import com.zosoftware.tb.R;
import com.zosoftware.tb.ui.LoginActivity;
import com.zosoftware.tb.ui.dashboard.CreateFormActivity;
import com.zosoftware.tb.utils.BasisTimesUtils;
import com.zosoftware.tb.utils.GlideEngine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateDongTaiActivity extends AppCompatActivity {

    EditText title,state,releasetime,content,username;
    ImageView image;
    Button confirm;
    TextView uploadvideo;
    Marker marker = null;
    String img = "";
    TextView videourltv ;
    MapView mMapView = null;
    Double lat=0d,lon=0d;

    private String videourl="";
    private SharedPreferences sharedPreferences;
    private JSONObject userinfojson = null;
    public AMapLocationClient mLocationClient = null;
    String source_id = "1";
    public AMapLocationClientOption mLocationOption = null;
    //初始化AMapLocationClientOption对象
    AMapLocationListener mAMapLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation amapLocation) {
            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                    //解析定位结果
                    Log.d(TAG, "onLocationChanged: "+amapLocation.getLongitude() + " : " + amapLocation.getLatitude());
                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_dong_tai);
        initmap(savedInstanceState);
        initview();
        initdata();
    }

    private void initmap(Bundle savedInstanceState) {

        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);

        try {

//初始化定位
            mLocationClient = new AMapLocationClient(getApplicationContext());


            AMap aMap ;
            aMap = mMapView.getMap();
            aMap.setOnMapClickListener(new AMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    Log.d(TAG, "onMapClick: " + latLng.latitude + " :" + latLng.longitude);
                    lat = latLng.latitude;
                    lon = latLng.longitude;
                    if(marker == null)
                    {
                        marker = aMap.addMarker(new MarkerOptions().position(latLng).title("地点") );

                    }else {
                        marker.setPosition(latLng);
//                        marker.notify();
                    }
                }
            });
            mLocationOption = new AMapLocationClientOption();
            AMapLocationClientOption option = new AMapLocationClientOption();
            /**
             * 设置定位场景，目前支持三种场景（签到、出行、运动，默认无场景）
             */
            option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
            if(null != mLocationClient){

                mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                mLocationOption.setOnceLocation(true);
                mLocationOption.setOnceLocationLatest(true);

                mLocationClient.setLocationOption(option);
                //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
                mLocationClient.setLocationListener(mAMapLocationListener);
//设置定位回调监听
//启动定位
                mLocationClient.startLocation();
//异步获取定位结果
            }
        }catch (Exception e){

        }

    }

    private void initview() {
        videourltv = findViewById(R.id.videourltv);
        confirm = findViewById(R.id.confirm);
        uploadvideo = findViewById(R.id.uploadvideo);
        image    = findViewById(R.id.image);
        username    = findViewById(R.id.username);
        releasetime    = findViewById(R.id.releasetime);
        state    = findViewById(R.id.state);
        title    = findViewById(R.id.title);
        content    = findViewById(R.id.content);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addDongTai();
            }
        });
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PictureSelector.create(CreateDongTaiActivity.this)
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
        uploadvideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PictureSelector.create(CreateDongTaiActivity.this)
                        .openGallery(SelectMimeType.ofVideo())
                        .setImageEngine(GlideEngine.createGlideEngine())
                        .setMaxSelectNum(1)
                        .forResult(new OnResultCallbackListener<LocalMedia>() {
                            @Override
                            public void onResult(ArrayList<LocalMedia> result) {
                                douploadVideo(new File(result.get(0).getRealPath()));
                            }
                            @Override
                            public void onCancel() {

                            }
                        });
            }
        });
        releasetime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BasisTimesUtils.showDatePickerDialog(CreateDongTaiActivity.this, BasisTimesUtils.THEME_HOLO_DARK, "请选择年月日", 2015, 1, 1, new BasisTimesUtils.OnDatePickerListener() {

                    @Override
                    public void onConfirm(int year, int month, int dayOfMonth) {
                        releasetime.setText(year + "-" + month + "-" + dayOfMonth);
                    }

                    @Override
                    public void onCancel() {
                    }
                });
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
                String result = response.body().string();
                Log.d("upload file :", result);
                JSONObject jsonObject1 = JSONObject.parseObject(result);
                if (jsonObject1.getJSONObject("result")!= null) {
                    CreateDongTaiActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            img =jsonObject1.getJSONObject("result").getString("url");
                            Glide.with(CreateDongTaiActivity.this)
                                    .load(Config.basehttpaddr+img)
                                    .centerCrop()
                                    .into( image);
                        }
                    });
                }
            }
        });


    }


    public  void douploadVideo(File file) {

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
                String result = response.body().string();
                Log.d("upload file :", result);
                JSONObject jsonObject1 = JSONObject.parseObject(result);
                if (jsonObject1.getJSONObject("result")!= null) {
                    CreateDongTaiActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            videourl =jsonObject1.getJSONObject("result").getString("url");
                            videourltv.setText(videourl);
                        }
                    });
                }
            }
        });


    }
    private void addDongTai() {
        if(lat == 0f || lon == 0f){
            toast("请选择位置" );
            return;
        }
        OkHttpClient okHttpClient = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("dynamic_content", content.getText().toString() );
        jsonObject.put("dynamic_images", img );
        jsonObject.put("dynamic_title", title.getText().toString() );
        jsonObject.put("mood_state", state.getText().toString() );
        jsonObject.put("motion_video",  videourl);
        jsonObject.put("lat",  lat);
        jsonObject.put("lon",  lon);
        jsonObject.put("tourism_dynamics_id",  0);
        jsonObject.put("release_date",  releasetime.getText().toString());
        jsonObject.put("publish_users",  userinfojson.getInteger("user_id"));
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        RequestBody requestBody = RequestBody.create(JSON,jsonObject.toJSONString());

        //Form表单格式的参数传递

        Request request = new Request
                .Builder()
                .post(requestBody)//Post请求的参数传递
                .url(Config.baseaddr+"/tourism_dynamics/add")
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
                if(jsonObject1.containsKey("error") || jsonObject1.containsKey("error")){
                    CreateDongTaiActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(  CreateDongTaiActivity.this,"创建失败！",Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    CreateDongTaiActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(  CreateDongTaiActivity.this,"创建成功！",Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
                response.body().close();
            }
        });
    }

    private void toast(String s) {
        CreateDongTaiActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CreateDongTaiActivity.this,s,Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();

    }

    private void initdata() {
        getloginstate();
    }

    private void getloginstate() {
        sharedPreferences=  getSharedPreferences("app", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");
        getUserInfo(token);

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
                    CreateDongTaiActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText( CreateDongTaiActivity.this,"登录超时了",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent( CreateDongTaiActivity.this, LoginActivity.class));
                             CreateDongTaiActivity.this.finish();

                        }
                    });
                    return;
                }
                userinfojson = jsonObject.getJSONObject("result").getJSONObject("obj");
                Log.d(TAG, "onResponse: "+userinfojson);
                CreateDongTaiActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        username.setText(userinfojson.getString("nickname"));
                    }
                });
            }
        });
    }
}