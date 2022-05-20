package com.example.nfc_flutter;

import android.content.Intent;

import androidx.annotation.NonNull;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {
    public static final String CHANNEL = "com.example.nfc_flutter.channel";
    public static final String KEY_NATIVE = "showNativeView";

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(
                        (call, result) -> {
                            if (call.method.equals(KEY_NATIVE) ) {
                                    Intent intent =new Intent(this, MainNfcActivity.class);
                                startActivity(intent);
                                result.success(true);
                            } else {
                                result.notImplemented();
                            }
                        }
                );
    }
}
