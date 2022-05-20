package com.example.nfc_flutter;

import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.Map;

import io.flutter.Log;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {
    public static final String CHANNEL = "com.example.nfc_flutter.channel";
    public static final String KEY_NATIVE = "showNativeView";

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        Intent intentD=getIntent();
        String data= intentD.getStringExtra("name");
//        if(data!=null){
//            Toast.makeText(this, data, Toast.LENGTH_SHORT).show();
//        }
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(
                        (call, result) -> {
                            final Map<String, Object> arguments = call.arguments();

                            if (call.method.equals(KEY_NATIVE) ) {
                                    Intent intent =new Intent(this, NativeAndroidActivity.class);
                                startActivity(intent);
                                result.success(true);
                            }else if(call.method.equals("getDataNfc")){

                                String from = (String) arguments.get("from");

                                String message = data;

                                result.success(message);
                            }
                            else {
                                result.notImplemented();
                            }

                        }
                );
    }
}
