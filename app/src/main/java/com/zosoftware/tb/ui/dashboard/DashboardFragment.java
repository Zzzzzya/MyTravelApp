package com.zosoftware.tb.ui.dashboard;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.zosoftware.tb.Config;
import com.zosoftware.tb.R;
import com.zosoftware.tb.databinding.FragmentDashboardBinding;
import com.zosoftware.tb.ui.home.AddCommentActivity;
import com.zosoftware.tb.ui.home.ZiliaoDetailActivity;

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

public class DashboardFragment extends Fragment {

    JSONArray formlist = new JSONArray();
    JSONArray form_type_list = new JSONArray();
    List<String> type_list = new ArrayList<>();
    private FragmentDashboardBinding binding;
    ArrayAdapter type_adapter  = null;
    String searchval = "";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        initview();
        initdata();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        initdata();
    }

    private void initdata() {
        getclassinfo();
        getformlist();
    }

    private void getformlist() {


        OkHttpClient client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
        String parm = "";
        if(searchval.length()>0) {
            parm += "&title=" + searchval;
        }else  if(form_type_list.size()>0 &&  binding.classspinner.getSelectedItemPosition() != 0) {
            parm+="&type=" + form_type_list.getJSONObject(binding.classspinner.getSelectedItemPosition()-1).getString("name");
        }
        Request request = new Request.Builder().url(Config.baseaddr+"/forum/get_list?like=0"+parm )
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
                formlist = _temp.getJSONObject("result").getJSONArray("list");

                Log.d(TAG, "onResponse: "+formlist);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.submitList(formlist);
                    }
                });
            }
        });
    }
    BaseQuickAdapter adapter = new BaseQuickAdapter<JSONObject, QuickViewHolder>() {
        @NonNull
        @Override
        protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup viewGroup, int i) {
            return new QuickViewHolder(R.layout.form_item, viewGroup);
        }

        @Override
        protected void onBindViewHolder(@NonNull QuickViewHolder quickViewHolder, int i, @Nullable JSONObject objects) {
//            ((TextView) quickViewHolder.getView(R.id.gonggaotv)).setText(objects);
            if(objects.containsKey("avatar")){
                Glide.with(getActivity())
                        .load(Config.basehttpaddr+objects.getString("img"))
                        .centerCrop()
                        .into( (ImageView) quickViewHolder.getView(R.id.ivPicture));
            }
            ((TextView)quickViewHolder.getView(R.id.title)).setText( objects.getString("title"));
            ((TextView)quickViewHolder.getView(R.id.likes)).setText( objects.getString("praise_len")+"点赞");
            ((TextView)quickViewHolder.getView(R.id.clicks)).setText( objects.getString("hits")+"点击");
            ((TextView)quickViewHolder.getView(R.id.username)).setText("昵称："+objects.getString("nickname"));
            ((TextView)quickViewHolder.getView(R.id.date)).setText( new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(objects.getLong("create_time"))));

        }
    };
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

                Log.d(TAG, "onResponse: "+formlist);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        type_list.clear();
                        type_list.add("全部");
                        for(int i =0 ; i <  form_type_list.size() ;i++){
                            type_list.add(form_type_list.getJSONObject(i).getString("name"));
                        }

                        type_adapter.notifyDataSetChanged();
                        binding.classspinner.setSelection(0);
                    }
                });
            }
        });
    }

    private void initview() {
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));

        binding.recyclerview.setAdapter(adapter);
        type_adapter  = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item,type_list);
        binding.classspinner.setAdapter(type_adapter);
        binding.classspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                searchval="";
                getformlist();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        binding.searchbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchval = binding.searched.getText().toString().trim();
                getformlist();
            }
        });
        binding.createbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), CreateFormActivity.class));
            }
        });
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onClick(@NonNull BaseQuickAdapter baseQuickAdapter, @NonNull View view, int i) {
                Intent intent = new Intent(getActivity(), FormDetailActivity.class);
                Log.d(TAG, "onClick: form item is " + formlist.getJSONObject(i).toJSONString());
                intent.putExtra("id",formlist.getJSONObject(i).getInteger("forum_id")+"");
                startActivity(intent);
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}