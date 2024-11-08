package com.zosoftware.tb.ui.user;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.zosoftware.tb.Config;
import com.zosoftware.tb.R;
import com.zosoftware.tb.databinding.FragmentNotificationsBinding;
import com.zosoftware.tb.ui.LoginActivity;
import com.zosoftware.tb.ui.home.AddCommentActivity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    SharedPreferences sharedPreferences ;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        getloginstate();
        initview();
        initdata();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        getloginstate();
    }

    private void getloginstate() {
        sharedPreferences=  getActivity().getSharedPreferences("app", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");
        if(token.length()==0){
            binding.logout.setVisibility(View.INVISIBLE);
            binding.travelactivity.setVisibility(View.INVISIBLE);
            binding.linearLayout.setVisibility(View.INVISIBLE);
            binding.usernametv.setText("游客");
            binding.roletv.setVisibility(View.INVISIBLE);
            binding.login.setVisibility(View.VISIBLE);

            binding.login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getActivity(),LoginActivity.class));
                }
            });
        }else {
            binding.logout.setVisibility(View.VISIBLE);
            binding.travelactivity.setVisibility(View.VISIBLE);
            binding.linearLayout.setVisibility(View.VISIBLE);
            binding.roletv.setVisibility(View.VISIBLE);
            binding.login.setVisibility(View.INVISIBLE);
            getUserInfo(token);
        }
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
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(getActivity(),"登录超时了",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getActivity(), LoginActivity.class));
                            getActivity().finish();

                        }
                    });
                    return;
                }
                JSONObject userinfojson = jsonObject.getJSONObject("result").getJSONObject("obj");
                Log.d(TAG, "onResponse: "+userinfojson);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.usernametv.setText(userinfojson.getString("username"));
                        binding.roletv.setText(userinfojson.getString("user_group"));


                        if(userinfojson.containsKey("avatar") ){
                            Glide.with(getActivity())
                                    .load(Config.basehttpaddr+userinfojson.getString("avatar"))
                                    .centerCrop()
                                    .into(binding.avator);
                        }else{

                            binding.avator.setImageResource(R.drawable.def2);
                        }

                    }
                });
            }
        });
    }

    private void initview() {
        binding.baseinfotv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(),UserInfoActivity.class));
            }
        });
        binding.baseinfoim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(),UserInfoActivity.class));
            }
        });
        binding.favim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(),CollectActivity.class));
            }
        });
        binding.favtv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(),CollectActivity.class));
            }
        });
        binding.logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = getActivity().getSharedPreferences("app", Context.MODE_PRIVATE).edit();
                editor.putString("token","");
                editor.clear();
                editor.commit();
                startActivity(new Intent(getActivity(),LoginActivity.class));
//                getActivity().finish();
            }
        });
        binding.travelactivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), DongTaiListActivity.class));
            }
        });
    }
    private void initdata() {
        

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}