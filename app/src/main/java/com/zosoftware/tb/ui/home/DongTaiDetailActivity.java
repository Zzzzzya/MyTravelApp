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
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.bumptech.glide.Glide;
import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.zosoftware.tb.Config;
import com.zosoftware.tb.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DongTaiDetailActivity extends AppCompatActivity {
    MapView mMapView = null;
    EditText state,username,name,date;
    VideoView videos;
    TextView content,likes,clicks;
    ImageView image;
    Button likebtn,commentbtn2,favbtn;
    JSONObject dongtaidetail = new JSONObject();
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
    private AMap aMap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dong_tai_detail);
        initview(savedInstanceState);
        initdata(getIntent().getStringExtra("id" )+"");
        source_id = getIntent().getStringExtra("id" )+"";
    }


    private void initdata(String id) {

        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(Config.baseaddr+"/tourism_dynamics/get_obj?tourism_dynamics_id="+id)
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
                if( !_temp.containsKey("result"))
                    return;
                dongtaidetail = _temp.getJSONObject("result").getJSONObject("obj");


                Log.d(TAG, "onResponse: "+dongtaidetail);
                DongTaiDetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        name.setText(dongtaidetail.getString("dynamic_title"));
                        username.setText(dongtaidetail.getString("publish_users"));
                        state.setText(dongtaidetail.getString("mood_state"));
                        likes.setText(dongtaidetail.getString("praise_len"));
                        clicks.setText(dongtaidetail.getString("hits"));
                        content.setText(dongtaidetail.getString("dynamic_content"));
                        date.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(dongtaidetail.getLong("release_date"))));
                        if(dongtaidetail.containsKey("dynamic_images"))
                            Glide.with(DongTaiDetailActivity.this)
                                    .load(Config.basehttpaddr+dongtaidetail.getString("dynamic_images"))
                                    .centerCrop()
                                    .into( image);
                        if(dongtaidetail.containsKey("motion_video")){
                            videos.setVideoURI( Uri.parse(Config.baseaddr_port+dongtaidetail.getString("motion_video")));
                            videos.start();
                        }
                        getcommentslist(id);

                        Config.addhits(DongTaiDetailActivity.this,
                                "tourism_dynamics_id",
                                dongtaidetail.getInteger("tourism_dynamics_id") + "",
                                "tourism_dynamics");
                        Config.setHits(DongTaiDetailActivity.this,
                                "tourism_dynamics_id",
                                dongtaidetail.getInteger("tourism_dynamics_id") + "",
                                "tourism_dynamics",
                                dongtaidetail.getInteger("hits")+1);
                        if(dongtaidetail.containsKey("lat") && dongtaidetail.containsKey("lon")){

                            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(dongtaidetail.getDouble("lat"),
                                    dongtaidetail.getDouble("lon")), 19));
                            aMap.addMarker(new MarkerOptions().position(new LatLng(dongtaidetail.getDouble("lat"),
                                    dongtaidetail.getDouble("lon") )).title("地点") );
                        }
                    }
                });
            }
        });
    }

    private void getcommentslist(String id) {

        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(Config.baseaddr+"/comment/get_list?source_table=tourism_dynamics&source_field=tourism_dynamics_id&source_id="+id)
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
                Log.d(TAG, "onResponse: "+dongtaidetail);
                DongTaiDetailActivity.this.runOnUiThread(new Runnable() {
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

        username = findViewById(R.id.username);
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
        state = findViewById(R.id.state);
        comments_recycl = findViewById(R.id.comments_recycl);
        comments_recycl.setLayoutManager(new LinearLayoutManager(DongTaiDetailActivity.this));
        comments_recycl.setAdapter(commentadapter);
        commentbtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent( DongTaiDetailActivity.this ,AddCommentActivity.class);
                intent.putExtra("id",dongtaidetail.getInteger("tourism_dynamics_id")+"");
                intent.putExtra("source_field", "tourism_dynamics_id");
                intent.putExtra("source_table","tourism_dynamics");
                intent.putExtra("source_id",dongtaidetail.getInteger("tourism_dynamics_id")+"");
                startActivity(intent);
            }
        });
        favbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Config.getCollectState(DongTaiDetailActivity.this,
                        "tourism_dynamics_id",
                        dongtaidetail.getInteger("tourism_dynamics_id") + "",
                        "tourism_dynamics", new Callback() {
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
                                        DongTaiDetailActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(DongTaiDetailActivity.this, "收藏成功!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        dongtaidetail.put("praise_len", dongtaidetail.getInteger("praise_len") + 1);
                                        Config.addcollect(DongTaiDetailActivity.this,
                                                "tourism_dynamics_id",
                                                dongtaidetail.getInteger("tourism_dynamics_id") + "",
                                                "tourism_dynamics",
                                                dongtaidetail.getString("dynamic_title"),
                                                dongtaidetail.getString("dynamic_images"));
                                    } else {
                                        DongTaiDetailActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(DongTaiDetailActivity.this, "取消收藏成功!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        dongtaidetail.put("praise_len", dongtaidetail.getInteger("praise_len") - 1);
                                        Config.delcollect(DongTaiDetailActivity.this,
                                                "tourism_dynamics_id",
                                                dongtaidetail.getInteger("tourism_dynamics_id") + "",
                                                "tourism_dynamics");

                                    }
                            }
                        });
            }
        });
        likebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Config.getPraiseState(DongTaiDetailActivity.this,
                        "tourism_dynamics_id",
                        dongtaidetail.getInteger("tourism_dynamics_id") + "",
                        "tourism_dynamics", new Callback() {
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
                                        DongTaiDetailActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(DongTaiDetailActivity.this, "点赞成功!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        dongtaidetail.put("praise_len", dongtaidetail.getInteger("praise_len") + 1);
                                        Config.addpraise(DongTaiDetailActivity.this,
                                                "tourism_dynamics_id",
                                                dongtaidetail.getInteger("tourism_dynamics_id") + "",
                                                "tourism_dynamics");
                                        Config.setpraise(DongTaiDetailActivity.this,
                                                "tourism_dynamics_id",
                                                dongtaidetail.getInteger("tourism_dynamics_id") + "",
                                                "tourism_dynamics", dongtaidetail.getInteger("praise_len"));
                                    } else {
                                        DongTaiDetailActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(DongTaiDetailActivity.this, "取消点赞成功!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        dongtaidetail.put("praise_len", dongtaidetail.getInteger("praise_len") - 1);
                                        Config.delpraise(DongTaiDetailActivity.this,
                                                "tourism_dynamics_id",
                                                dongtaidetail.getInteger("tourism_dynamics_id") + "",
                                                "tourism_dynamics");
                                        Config.setpraise(DongTaiDetailActivity.this,
                                                "tourism_dynamics_id",
                                                dongtaidetail.getInteger("tourism_dynamics_id") + "",
                                                "tourism_dynamics", dongtaidetail.getInteger("praise_len"));
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
                Glide.with(DongTaiDetailActivity.this)
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
                    Intent intent = new Intent(DongTaiDetailActivity.this,AddCommentActivity.class);
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