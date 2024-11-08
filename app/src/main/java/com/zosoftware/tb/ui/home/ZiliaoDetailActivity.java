package com.zosoftware.tb.ui.home;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.MapView;
import com.bumptech.glide.Glide;
import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
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

public class ZiliaoDetailActivity extends AppCompatActivity {
    MapView mMapView = null;
    EditText place,name,date;
    VideoView videos;
    TextView content,likes,clicks;
    ImageView image;
    Button likebtn,commentbtn2,favbtn;
    JSONObject ziliaodetail = new JSONObject();
    RecyclerView comments_recycl;
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
        setContentView(R.layout.activity_ziliao_detail);
        initview(savedInstanceState);
        initdata(getIntent().getIntExtra("id",1)+"");
        source_id = getIntent().getIntExtra("id",1)+"";

    }


    private void initdata(String id) {

        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(Config.baseaddr+"/tourism_materials/get_obj?tourism_materials_id="+id)
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
                ziliaodetail = _temp.getJSONObject("result").getJSONObject("obj");


                Log.d(TAG, "onResponse: "+ziliaodetail);
                ZiliaoDetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        name.setText(ziliaodetail.getString("attraction_name"));
                        place.setText(ziliaodetail.getString("destination_location"));
                        likes.setText(ziliaodetail.getString("praise_len"));
                        clicks.setText(ziliaodetail.getString("hits"));
                        content.setText(ziliaodetail.getString("information_content"));
                        date.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(ziliaodetail.getLong("release_date"))));
                        if(ziliaodetail.containsKey("scenic_spot_pictures"))
                            Glide.with(ZiliaoDetailActivity.this)
                                    .load(Config.basehttpaddr+ziliaodetail.getString("scenic_spot_pictures"))
                                    .centerCrop()
                                    .into( image);
                        if(ziliaodetail.containsKey("scenic_spot_videos")){
                            videos.setVideoURI( Uri.parse(Config.basehttpaddr+ziliaodetail.getString("scenic_spot_videos")));
                            videos.start();
                        }
                        getcommentslist(id);

                        Config.addhits(ZiliaoDetailActivity.this,
                                "tourism_materials_id",
                                ziliaodetail.getInteger("tourism_materials_id") + "",
                                "tourism_materials");
                        Config.setHits(ZiliaoDetailActivity.this,
                                "tourism_materials_id",
                                ziliaodetail.getInteger("tourism_materials_id") + "",
                                "tourism_materials",
                                ziliaodetail.getInteger("hits")+1);
                    }
                });
            }
        });
    }

    private void getcommentslist(String id) {

        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(Config.baseaddr+"/comment/get_list?source_table=tourism_materials&source_field=tourism_materials_id&source_id="+id)
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
                JSONArray jsonArray = _temp.getJSONObject("result").getJSONArray("list");
                Log.d(TAG, "onResponse: "+ziliaodetail);
                ZiliaoDetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        commentadapter.submitList(jsonArray);
                    }
                });
            }
        });
    }

    private void initview(Bundle savedInstanceState) {
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        initmap();

        favbtn = findViewById(R.id.favbtn);
        image = findViewById(R.id.ivPicture);
        commentbtn2 = findViewById(R.id.commentbtn2);
        likebtn = findViewById(R.id.likebtn);
        clicks = findViewById(R.id.clicks);
        likes = findViewById(R.id.likes);
        content = findViewById(R.id.content);
        videos = findViewById(R.id.videos);
        date = findViewById(R.id.date);
        name = findViewById(R.id.name);
        place = findViewById(R.id.place);
        comments_recycl = findViewById(R.id.comments_recycl);
        comments_recycl.setLayoutManager(new LinearLayoutManager(ZiliaoDetailActivity.this));
        comments_recycl.setAdapter(commentadapter);
        commentbtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( ZiliaoDetailActivity.this ,AddCommentActivity.class);
                intent.putExtra("id",ziliaodetail.getInteger("tourism_materials_id")+"");
                intent.putExtra("source_field", "tourism_materials_id");
                intent.putExtra("source_table","tourism_materials");
                intent.putExtra("source_id",ziliaodetail.getInteger("tourism_materials_id")+"");
                startActivity(intent);
            }
        });
        favbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Config.getCollectState(ZiliaoDetailActivity.this,
                        "tourism_materials_id",
                        ziliaodetail.getInteger("tourism_materials_id") + "",
                        "tourism_materials", new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {

                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                String resp = response.body().string();
                                Log.d(TAG, "onResponse: ziliao getCollectState " +resp);
                                JSONObject jsonObject =JSONObject.parseObject(resp);
                                if(jsonObject.containsKey("result"))
                                    if (jsonObject.getInteger("result") == 0) {
                                        ZiliaoDetailActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(ZiliaoDetailActivity.this, "收藏成功!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        ziliaodetail.put("praise_len", ziliaodetail.getInteger("praise_len") + 1);
                                        Config.addcollect(ZiliaoDetailActivity.this,
                                                "tourism_materials_id",
                                                ziliaodetail.getInteger("tourism_materials_id") + "",
                                                "tourism_materials",
                                                ziliaodetail.getString("attraction_name"),
                                                ziliaodetail.getString("scenic_spot_pictures"));

                                    } else {
                                        ZiliaoDetailActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(ZiliaoDetailActivity.this, "取消收藏成功!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        ziliaodetail.put("praise_len", ziliaodetail.getInteger("praise_len") - 1);
                                        Config.delcollect(ZiliaoDetailActivity.this,
                                                "tourism_materials_id",
                                                ziliaodetail.getInteger("tourism_materials_id") + "",
                                                "tourism_materials");

                                    }
                            }
                        });
            }
        });
        likebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Config.getPraiseState(ZiliaoDetailActivity.this,
                        "tourism_materials_id",
                        ziliaodetail.getInteger("tourism_materials_id") + "",
                        "tourism_materials", new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {

                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                String resp = response.body().string();
                                Log.d(TAG, "onResponse: " +resp);
                                JSONObject jsonObject =JSONObject.parseObject(resp);
                                if(jsonObject.containsKey("result"))
                                if (jsonObject.getInteger("result") == 0) {
                                    ZiliaoDetailActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(ZiliaoDetailActivity.this, "点赞成功!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    ziliaodetail.put("praise_len", ziliaodetail.getInteger("praise_len") + 1);
                                    Config.addpraise(ZiliaoDetailActivity.this,
                                            "tourism_materials_id",
                                            ziliaodetail.getInteger("tourism_materials_id") + "",
                                            "tourism_materials");
                                    Config.setpraise(ZiliaoDetailActivity.this,
                                            "tourism_materials_id",
                                            ziliaodetail.getInteger("tourism_materials_id") + "",
                                            "tourism_materials", ziliaodetail.getInteger("praise_len"));
                                } else {
                                    ZiliaoDetailActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(ZiliaoDetailActivity.this, "取消点赞成功!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    ziliaodetail.put("praise_len", ziliaodetail.getInteger("praise_len") - 1);
                                    Config.delpraise(ZiliaoDetailActivity.this,
                                            "tourism_materials_id",
                                            ziliaodetail.getInteger("tourism_materials_id") + "",
                                            "tourism_materials");
                                    Config.setpraise(ZiliaoDetailActivity.this,
                                            "tourism_materials_id",
                                            ziliaodetail.getInteger("tourism_materials_id") + "",
                                            "tourism_materials", ziliaodetail.getInteger("praise_len"));
                                }
                            }
                        });
            }
        });
    }

    private void initmap() {
        try {

//初始化定位
            mLocationClient = new AMapLocationClient(getApplicationContext());


            AMap aMap ;
            aMap = mMapView.getMap();
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
    BaseQuickAdapter commentadapter = new BaseQuickAdapter<JSONObject, QuickViewHolder>() {
        @NonNull
        @Override
        protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
            return new QuickViewHolder(R.layout.comment_item, viewGroup);
        }

        @Override
        protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable JSONObject objects) {
//            ((TextView) quickViewHolder.getView(R.id.gonggaotv)).setText(objects);
            if(objects.containsKey("avatar")){
                Glide.with(ZiliaoDetailActivity.this)
                        .load(Config.basehttpaddr+objects.getString("avatar"))
                        .centerCrop()
                        .into( (ImageView) quickViewHolder.getView(R.id.avatar));
            }
            ((TextView)quickViewHolder.getView(R.id.comment)).setText("评论："+objects.getString("content"));
            ((TextView)quickViewHolder.getView(R.id.nickname)).setText("昵称："+objects.getString("nickname"));
            ((TextView)quickViewHolder.getView(R.id.date)).setText( new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(objects.getLong("create_time"))));
            ((TextView)quickViewHolder.getView(R.id.replay)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ZiliaoDetailActivity.this,AddCommentActivity.class);
                    intent.putExtra("id",objects.getInteger("source_id")+"");
                    intent.putExtra("source_field",objects.getString("source_field"));
                    intent.putExtra("source_table",objects.getString("source_table"));
                    intent.putExtra("source_id",objects.getInteger("source_id")+"");
                    intent.putExtra("replay_user",objects.getString("nickname"));
                    intent.putExtra("reply_to_id",objects.getString("user_id"));
                    startActivity(intent);
                }
            });
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        initdata(source_id);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mMapView.onSaveInstanceState(outState);

    }
}