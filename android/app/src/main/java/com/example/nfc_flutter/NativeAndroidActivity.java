package com.example.nfc_flutter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import io.flutter.embedding.android.FlutterActivity;

public class NativeAndroidActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_android);
        findViewById(R.id.btnClick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NativeAndroidActivity.this, FlutterActivity.class);
                intent.setAction(Intent.ACTION_RUN);
                intent.putExtra("route", "/readerdata");
                startActivity(intent);
            }
        });
    }
}