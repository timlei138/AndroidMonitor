package com.lc.monitor;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.lc.mail.EmailMessage;
import com.lc.mail.tencent.TencentProtocolSmtp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    private String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.SEND_SMS,
    };

    private int PERMISSION_CODE = 1000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        TextView textView = findViewById(R.id.info);
        String info = getVersionInfo();
        if(!TextUtils.isEmpty(info)){
            textView.setVisibility(View.VISIBLE);
            textView.setText(info);
        }else{
            textView.setVisibility(View.GONE);
        }

        checkPermission();
    }



    private void checkPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            Log.e("demo","permisson request");
            boolean hasPermission = false;
            for (String permission : permissions){
                if(checkCallingPermission(permission) == PackageManager.PERMISSION_DENIED){
                    requestPermissions(permissions,PERMISSION_CODE);
                    return;
                }
                hasPermission = true;
            }

            if(hasPermission){
                timerHander.sendEmptyMessageDelayed(0,2000);
            }
        }else{
            timerHander.sendEmptyMessageDelayed(0,2000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i =0 ;i<grantResults.length;i++){
            if(grantResults[i] == PackageManager.PERMISSION_DENIED){
                showAlertDialog();
                break;
            }
        }

        timerHander.sendEmptyMessageDelayed(0,2000);

    }

    private String getVersionInfo(){
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(),0);
            String version = info.versionName;
            int code = info.versionCode;
            return "VersionName:"+version+"\n"+"VersionCode:"+code;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void showAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("警告");
        builder.setMessage("为保证应用正常运行，应用需要部分权限，请进去设置应用进行授权");
        builder.setPositiveButton("前往授权", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings"));
                startActivity(intent);
            }
        });
        builder.setNegativeButton("退出应用", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
    }


    private Handler timerHander = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Intent intent = new Intent();
            intent.setClass(getBaseContext(),MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(intent);
            finish();
        }
    };



}
