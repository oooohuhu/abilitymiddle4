package api.amap.com.mylibrary;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tbruyelle.rxpermissions.RxPermissions;
//import com.ustcinfo.mobile.platform.ability.apicallback.HttpCallbackListener;
//import com.ustcinfo.mobile.platform.ability.jsbridge.JsMethodAdapter;
//import com.ustcinfo.mobile.platform.ability.jsbridge.JsWebView;
//import com.ustcinfo.mobile.platform.ability.utils.CheckJsUpdateUtils;
//import com.ustcinfo.mobile.platform.ability.utils.HttpUtil;
//import com.ustcinfo.mobile.platform.ability.utils.MConfig;

import api.amap.com.mylibrary.widgets.MAlertDialog;
import rx.functions.Action1;

/**
 * Created by xueqili on 2018/5/9.
 */

public class MainActivity extends AppCompatActivity {
    JsWebView webView;
    //创建属于主线程的handler
    Handler handler = new Handler();
    private String result;
    private int httpversion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webview);
        checkPermissions();
    }

    @SuppressLint("NewApi")
    private void checkPermissions() {

        RxPermissions rxPermissions = RxPermissions.getInstance(this);
        rxPermissions.request
                (Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.
                                RECORD_AUDIO, Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.CALL_PHONE)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        if (!aBoolean) {
                            final MAlertDialog alertDialog = new MAlertDialog(MainActivity.this, false, true);
                            alertDialog.setCancelable(true).setDialogCanceledOnTouchOutside(false).setTitle("应用权限").setContent("请务必给予应用相应的权限")
                                    .setCancelClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            finish();
                                        }
                                    }).setConfirmClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    Intent localIntent = new Intent();
                                    localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    if (Build.VERSION.SDK_INT >= 9) {
                                        localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                        localIntent.setData(Uri.fromParts("package", getApplication().getPackageName(), null));
                                    } else if (Build.VERSION.SDK_INT <= 8) {
                                        localIntent.setAction(Intent.ACTION_VIEW);
                                        localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
                                        localIntent.putExtra("com.android.settings.ApplicationPkgName", getApplicationContext().getPackageName());
                                    }
                                    startActivity(localIntent);
                                    alertDialog.dismiss();
                                }
                            }).show();
                        } else {
                            try {
//                                webView.loadUrl("file:///android_asset/demo/index.html");
                                                            getupdate();
//                                AmapLocation.get().lunch(MainActivity.this);
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), "请放入h5文件", Toast.LENGTH_LONG).show();
                            }

                        }
                    }
                });
    }

    private void getupdate() {
        HttpUtil.startGet(MConfig.get(MainActivity.this, "GetUpdate"), null, new HttpCallbackListener() {
            @Override
            public void onSucess(String s) {
                JSONObject object = JSON.parseObject(s);
                httpversion = object.getIntValue("verson");
                handler.post(runnableupdate);
            }

            @Override
            public void onError(Exception e) {
                Log.e("请求错误", e.toString());
            }
        });
    }

    // 构建Runnable对象，在runnable中更新界面
    Runnable runnableupdate = new Runnable() {
        @Override
        public void run() {
            try {
                CheckJsUpdateUtils.CheckJsUpdate(MainActivity.this, webView, httpversion);
            } catch (Exception e) {
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        JsMethodAdapter.getmInstance().onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        JsMethodAdapter.unRegister();
    }

    @Override
    public void onBackPressed() {
        if (webView.canBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
