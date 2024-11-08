package com.zosoftware.tb.ui;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    EditText uname,passed;
    Button loginbtn;
    TextView forget,goregist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        initview();

    }

    private void initview() {
        uname = findViewById(R.id.username);
        passed = findViewById(R.id.password);
        loginbtn = findViewById(R.id.login);
        forget = findViewById(R.id.forgetpass);
        goregist = findViewById(R.id.goregist);
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dologin();
            }
        });
        forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, ForgetActivity.class));
            }
        });
        goregist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegistActivity.class));
            }
        });
    }
    static String publickey_str =  "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC+mEDzYLbKNB9rbOuvGgwdBUpPaHryRGarxBQppkOzlj+ouep8MMq1Xg7NBkjLOV2vnn4E5AVvX0XVOmBg8W5eNQ1uS1HCG2fie8BpXGgl1pWj/HYIrA2d/U7xxvMO8UMhAGfMdaGrPrGdZTr95pzL/q+VJZOcqSAgux/YEdu11wIDAQAB"
 ;
    public static String encryptWithPublicKey(String text, String publicKeyBase64) {
        try {
            // 将公钥Base64编码转换为字节
            byte[] publicKeyBytes = Base64.decode(publicKeyBase64, Base64.DEFAULT);
            // 将公钥字节转换为Key对象
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            // 使用公钥加密
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(text.getBytes());

            // 将加密后的数据转换为Base64编码的字符串
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String encryptRSAToString(String text, String strPublicKey) {
        byte[] cipherText = null;
        String strEncryInfoData="";
        try {

            KeyFactory keyFac = KeyFactory.getInstance("RSA");
            KeySpec keySpec = new X509EncodedKeySpec(Base64.decode(strPublicKey.trim().getBytes(), Base64.DEFAULT));
            Key publicKey = keyFac.generatePublic(keySpec);

            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance("RSA");
            // encrypt the plain text using the public key
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            cipherText = cipher.doFinal(text.getBytes());
            strEncryInfoData = new String(Base64.encode(cipherText,Base64.DEFAULT));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return strEncryInfoData.replaceAll("(\\r|\\n)", "");
    }
    public static String encryptDataRSA(final String data)  {
        final byte[] dataToEncrypt = data.getBytes();
        byte[] encryptedData = null;

        try {

            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publickey_str.getBytes()));

            final Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            encryptedData = cipher.doFinal(dataToEncrypt);

            try {
                final String encryptedText = new String(Base64.encode(encryptedData, Base64.DEFAULT), "UTF-8");
                return encryptedText.toString();
            }
            catch (final UnsupportedEncodingException e1) { return null; }
        } catch (Exception e) { e.printStackTrace(); }

        return "ERROR";
    }
    private void dologin() {

        OkHttpClient okHttpClient = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username",uname.getText().toString().trim());
        jsonObject.put("password",encryptWithPublicKey(passed.getText().toString().trim(),publickey_str));
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        RequestBody requestBody = RequestBody.create(JSON,jsonObject.toJSONString());

        //Form表单格式的参数传递

        Request request = new Request
                .Builder()
                .post(requestBody)//Post请求的参数传递
                .url(Config.baseaddr+"/user/login")
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
                    LoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(  LoginActivity.this,"登录失败！",Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    LoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(  LoginActivity.this,"登录成功！",Toast.LENGTH_SHORT).show();
                            String token = jsonObject1.getJSONObject("result").getJSONObject("obj").getString("token");
                            SharedPreferences.Editor editor =  LoginActivity.this.getSharedPreferences("app",MODE_PRIVATE).edit();
                            editor.putString("token",token);
                            editor.putString("user_id",jsonObject1.getJSONObject("result").getJSONObject("obj").getString("user_id"));
                            editor.commit();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }
                    });
                }
                response.body().close();
            }
        });

    }


}