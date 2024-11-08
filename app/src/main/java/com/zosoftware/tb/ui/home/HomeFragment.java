package com.zosoftware.tb.ui.home;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.zosoftware.tb.Config;
import com.zosoftware.tb.R;
import com.zosoftware.tb.databinding.FragmentHomeBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment  {

    private FragmentHomeBinding binding;
    private JSONArray dongTaiJsonArray;
    private JSONArray huodongJsonArray;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        initview(root);
        getdata();
        return root;
    }

    private void getdata() {
        gettopbanner();
        getGongGao();
        getZiliao();
        getHuoDong();
        getDongTai();
    }


    private void initview(View root) {
        binding.topviewpager.setAdapter(bannerQuickAdapter);
        binding.gongaoviewpage.setAdapter(gongaoAdapter);
        binding.recyziliao.setLayoutManager(new GridLayoutManager(getActivity(),2));
        binding.recyziliao.setAdapter(ziliaoAdapter);

        binding.recydongtai.setLayoutManager(new GridLayoutManager(getActivity(),2));
        binding.recydongtai.setAdapter(dongtaiAdapter);

        binding.recyhuodong.setLayoutManager(new GridLayoutManager(getActivity(),2));
        binding.recyhuodong.setAdapter(huodongAdapter);
        gongaoAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter baseQuickAdapter, @NonNull View view, int i) {
                startActivity(new Intent(getActivity(),GongGaoListActivity.class));
            }
        });
        dongtaiAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter baseQuickAdapter, @NonNull View view, int i) {
                Intent intent = new Intent( getActivity() ,DongTaiDetailActivity.class);
                intent.putExtra("id",dongTaiJsonArray.getJSONObject(i).getInteger("tourism_dynamics_id")+"");
                startActivity(intent);
            }
        });
        huodongAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter baseQuickAdapter, @NonNull View view, int i) {
                Intent intent = new Intent( getActivity() ,HuoDongDetailActivity.class);
                intent.putExtra("id",huodongJsonArray.getJSONObject(i).getInteger("tourism_activities_id")+"");
                startActivity(intent);
            }
        });
        binding.ziliaoTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ZiliaoListActivity.class));
            }
        });
        binding.ziliaoIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ZiliaoListActivity.class));
            }
        });
        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ZiliaoListActivity.class));
            }
        });
        binding.dongtaiIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), DonTaiActivity.class));
            }
        });
        binding.dongtaiTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), DonTaiActivity.class));
            }
        });
        binding.more2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), DonTaiActivity.class));
            }
        });

        binding.huodongIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), HuoDongActivity.class));
            }
        });
        binding.huodongTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), HuoDongActivity.class));
            }
        });
        binding.more3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), HuoDongActivity.class));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getdata();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void gettopbanner(){
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(Config.baseaddr+"/slides/get_list")
                .get().build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: "+Config.baseaddr+"/slides/get_list");
                Log.d(TAG, "onFailure: fail" + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp_str = response.body().string();
                Log.d(TAG, "onResponse:  str is "+resp_str);
                JSONObject jsonObject = JSONObject.parseObject(resp_str);
                JSONArray jsonArray = jsonObject.getJSONObject("result").getJSONArray("list");
                List<String> reslist = new ArrayList<>();
                for (int i =0;i<jsonArray.size();i++){
                    reslist.add(jsonArray.getJSONObject(i).getString("img"));
                }
                Log.d(TAG, "onResponse: "+jsonArray);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bannerQuickAdapter.submitList(reslist);
//                        baseQuickAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }
    private void getGongGao(){
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(Config.baseaddr+"/notice/get_list")
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
                JSONArray jsonArray = jsonObject.getJSONObject("result").getJSONArray("list");
                List<String> reslist = new ArrayList<>();
                for (int i =0;i<jsonArray.size();i++){
                    reslist.add(jsonArray.getJSONObject(i).getString("title"));
                }
                Log.d(TAG, "onResponse: "+jsonArray);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gongaoAdapter.submitList(reslist);
                    }
                });
            }
        });
    }
    private void getZiliao(){
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(Config.baseaddr+"/tourism_materials/get_list?orderby=hits%20desc")
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
                JSONArray jsonArray = jsonObject.getJSONObject("result").getJSONArray("list");
                Log.d(TAG, "onResponse: "+jsonArray);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ziliaoAdapter.submitList(jsonArray);
                        ziliaoAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                            @Override
                            public void onClick(@NonNull BaseQuickAdapter baseQuickAdapter, @NonNull View view, int i) {
                                Intent intent = new Intent(getActivity(),ZiliaoDetailActivity.class);
                                intent.putExtra("id",jsonArray.getJSONObject(i).getInteger("tourism_materials_id"));
                                startActivity(intent);
                            }
                        });
                    }
                });
            }
        });
    }
    private void getDongTai(){
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(Config.baseaddr+"/tourism_dynamics/get_list?orderby=hits%20desc")
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
                dongTaiJsonArray = jsonObject.getJSONObject("result").getJSONArray("list");
                Log.d(TAG, "onResponse: "+dongTaiJsonArray);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dongtaiAdapter.submitList(dongTaiJsonArray);
                    }
                });
            }
        });
    }
    private void getHuoDong(){
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(Config.baseaddr+"/tourism_activities/get_list?orderby=hits%20desc")
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
                huodongJsonArray = jsonObject.getJSONObject("result").getJSONArray("list");
                Log.d(TAG, "onResponse: "+huodongJsonArray);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        huodongAdapter.submitList(huodongJsonArray);
                    }
                });
            }
        });
    }
    BaseQuickAdapter bannerQuickAdapter = new BaseQuickAdapter<String, QuickViewHolder>() {
        @NonNull
        @Override
        protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
            return new QuickViewHolder(R.layout.bannerlayout, viewGroup);
        }

        @Override
        protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable String objects) {
            quickViewHolder.getView(R.id.banenrimg);
            Glide.with(getActivity())
                    .load(Config.basehttpaddr+objects)
                    .centerCrop()
                    .into( (ImageView) quickViewHolder.getView(R.id.banenrimg));
        }
    };

    BaseQuickAdapter gongaoAdapter = new BaseQuickAdapter<String, QuickViewHolder>() {
        @NonNull
        @Override
        protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
            return new QuickViewHolder(R.layout.gonggaoitem, viewGroup);
        }

        @Override
        protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable String objects) {
            ((TextView) quickViewHolder.getView(R.id.gonggaotv)).setText(objects);
        }
    };

    BaseQuickAdapter ziliaoAdapter = new BaseQuickAdapter<JSONObject, QuickViewHolder>() {
        @NonNull
        @Override
        protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
            return new QuickViewHolder(R.layout.ziliao_item, viewGroup);
        }

        @Override
        protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable JSONObject objects) {
            ((TextView) quickViewHolder.getView(R.id.place)).setText(objects.getString("destination_location"));
            ((TextView) quickViewHolder.getView(R.id.name)).setText(objects.getString("attraction_name"));
            ((TextView) quickViewHolder.getView(R.id.datetime)).setText(objects.getString("release_date"));
            ((TextView) quickViewHolder.getView(R.id.likenum)).setText(String.valueOf(objects.getInteger("praise_len")));
            ((TextView) quickViewHolder.getView(R.id.clicknum)).setText(String.valueOf(objects.getInteger("hits")));
            Glide.with(getActivity())
                    .load(Config.basehttpaddr+objects.getString("scenic_spot_pictures"))
                    .centerCrop()
                    .into( (ImageView) quickViewHolder.getView(R.id.imageView5));

        }
    };

    BaseQuickAdapter dongtaiAdapter = new BaseQuickAdapter<JSONObject, QuickViewHolder>() {
        @NonNull
        @Override
        protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
            return new QuickViewHolder(R.layout.dongtai_item, viewGroup);
        }

        @Override
        protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable JSONObject objects) {
            ((TextView) quickViewHolder.getView(R.id.title)).setText(objects.getString("dynamic_title"));
            ((TextView) quickViewHolder.getView(R.id.heart)).setText(objects.getString("mood_state"));
            ((TextView) quickViewHolder.getView(R.id.datetime)).setText(objects.getString("release_date"));
            ((TextView) quickViewHolder.getView(R.id.likenum)).setText(String.valueOf(objects.getInteger("praise_len")));
            ((TextView) quickViewHolder.getView(R.id.clicknum)).setText(String.valueOf(objects.getInteger("hits")));
            Glide.with(getActivity())
                    .load(Config.basehttpaddr+objects.getString("dynamic_images"))
                    .centerCrop()
                    .into( (ImageView) quickViewHolder.getView(R.id.imageView5));
        }
    };
    BaseQuickAdapter huodongAdapter = new BaseQuickAdapter<JSONObject, QuickViewHolder>() {
        @NonNull
        @Override
        protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
            return new QuickViewHolder(R.layout.huodong_item, viewGroup);
        }

        @Override
        protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable JSONObject objects) {
            ((TextView) quickViewHolder.getView(R.id.title)).setText(objects.getString("activity_name"));
            ((TextView) quickViewHolder.getView(R.id.place)).setText(objects.getString("event_location"));
            ((TextView) quickViewHolder.getView(R.id.datetime)).setText(objects.getString("release_date"));
            ((TextView) quickViewHolder.getView(R.id.fee)).setText(objects.getString("activity_expenses"));
            ((TextView) quickViewHolder.getView(R.id.likenum)).setText(String.valueOf(objects.getInteger("praise_len")));
            ((TextView) quickViewHolder.getView(R.id.clicknum)).setText(String.valueOf(objects.getInteger("hits")));
            Glide.with(getActivity())
                    .load(Config.basehttpaddr+objects.getString("activity_images"))
                    .centerCrop()
                    .into( (ImageView) quickViewHolder.getView(R.id.imageView5));
        }
    };

}