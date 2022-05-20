package com.example.nfc_flutter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import io.flutter.embedding.android.FlutterActivity;

public class NativeAndroidActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_android);
        EditText edtP1=findViewById(R.id.edtp1);

        findViewById(R.id.btnClick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(FlutterActivity.createDefaultIntent(view.getContext())); //this navigate to flutter page
               String data=  edtP1.getText().toString();
                Intent intent = new Intent(NativeAndroidActivity.this, MainActivity.class);
                 intent.putExtra("name",data);
//                intent.setAction(Intent.ACTION_RUN);
//                intent.putExtra("route", "/reader_page");
                startActivity(intent);
            }
        });
    }
}