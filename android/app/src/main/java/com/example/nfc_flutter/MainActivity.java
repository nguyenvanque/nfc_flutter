package com.example.nfc_flutter;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {
    public static final String CHANNEL = "com.example.nfc_flutter.channel";
    public static final String KEY_NATIVE = "showNativeView";
    static String pin1="";
    static String pin2="";

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        Intent intentD=getIntent();
        String data= intentD.getStringExtra("name");

        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(
                        (call, result) -> {
                            final Map<String, Object> arguments = call.arguments();

                            if (call.method.equals(KEY_NATIVE) ) {
                                    Intent intent =new Intent(this, MainNfcActivity.class);
                                startActivity(intent);
                                result.success(true);
                            }else if(call.method.equals("getPin")){
//                                String from = (String) arguments.get("from");
                                pin1 = (String) arguments.get("edtP1");
                                pin2 = (String) arguments.get("edtP2");
                                Toast.makeText(this, pin1+"_"+pin2, Toast.LENGTH_SHORT).show();
                                String message = data;

                                result.success(message);
                            }
                            else {
                                result.notImplemented();
                            }

                        }
                );
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        this.onTagDiscovered(tag);
    }
    public void onTagDiscovered(final Tag tag) {
//
//        DLReaderTask task = new DLReaderTask(MainActivity.this, tag);
//        ExecutorService exec = Executors.newSingleThreadExecutor();
//        exec.submit(task);
    }

}
