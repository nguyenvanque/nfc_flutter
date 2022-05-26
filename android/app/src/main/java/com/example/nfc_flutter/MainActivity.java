package com.example.nfc_flutter;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcB;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.BasicMessageChannel;
import io.flutter.plugin.common.MethodChannel;
import jp.co.osstech.libjeid.InvalidPinException;

public class MainActivity extends FlutterActivity implements TagDiscoveredListener{
    public static final String CHANNEL = "com.example.nfc_flutter.channel";
    public static final String KEY_NATIVE = "showNativeView";
    static String pin1 = "";
    static String pin2 = "";

    public static final String TAG = "JeidReader";
    protected NfcAdapter nfcAdapter;

    HashMap<String,String> hashMap=new HashMap<>();


    // NFC読み取りモード
    private final int NFC_AUTO_MODE = 0;
    private final int NFC_READER_MODE = 1;
    private final int NFC_FD_MODE = 2;
    protected int nfcMode;
    // ビューアーやメニュー画面ではNFC読み取りを無効化する
    // また、PIN間違いが発生してダイヤログを表示している間に
    // 連続読み取りが発生することを防ぐためのフラグ
    protected boolean enableNFC = false;

    String myData="";

    App app;

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);

//        Intent intentD=getIntent();
//        String data= intentD.getStringExtra("name");


        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(
                        (call, result) -> {
                            final Map<String, Object> arguments = call.arguments();
                            if (call.method.equals(KEY_NATIVE)) {
                                Intent intent = new Intent(this, MainNfcActivity.class);
                                startActivity(intent);
                                result.success(true);
                            } else if (call.method.equals("getPin")) {
//                                String from = (String) arguments.get("from");
                                pin1 = (String) arguments.get("edtP1");
                                pin2 = (String) arguments.get("edtP2");
                                app=new App();

                                Log.d("pin", "pin1 :" + pin1 + " Pin2 :" + pin2);
//                                String message = data;
//                                result.success(message);
                                result.success("Success");

                            } else if(call.method.equals("getData")){
                              result.success(myData);
                            }
                            else if(call.method.equals("getMapData")){
//                                hashMap2.put("name","nguyen van que");
//                                hashMap2.put("kana","HCM");
                                result.success(hashMap);
                            }
                            else {
                                result.notImplemented();
                            }

                        }


                );
    }


    public String getPin1() {
        return pin1;
    }

    public String getPin2() {
        return pin2;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, getClass().getSimpleName() + "#onNewIntent()");
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        this.onTagDiscovered(tag);
    }
    @Override
    public void onTagDiscovered(final Tag tag) {
        Log.d(TAG, getClass().getSimpleName() + "#onTagDiscovered()");
        if (!this.enableNFC) {
            Log.d(TAG, getClass().getSimpleName() + ": NFC disabled.");
            return;
        }
        DLReaderTask task = new DLReaderTask(app, tag);
        ExecutorService exec = Executors.newSingleThreadExecutor();
        exec.submit(task);
    }


    protected  void showInvalidPinDialog(String name, InvalidPinException e) {
        String title;
        String msg;
        if (e.isBlocked()) {
            title = name + "がブロックされています";
            msg = "警察署でブロック解除の申請をしてください。";
        } else {
            int counter = e.getCounter();
            title = name + "が間違っています";
            msg = name + "を正しく入力してください。";
            msg += "のこり" + counter + "回間違えるとブロックされます。";
        }
        app.print(title);
        app.print(msg);
        showDialog(title, msg);
    }



    protected void showDialog(String title, String msg) {
        Log.d(TAG, getClass().getSimpleName() + "#showDialog()");
        this.enableNFC = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setNeutralButton(
                "戻る",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        enableNFC = true;
                    }
                });

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }
}
