package com.example.nfc_flutter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainNfcActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_nfc);
        findViewById(R.id.in_menu_button).setOnClickListener(this);
        findViewById(R.id.dl_reader_button).setOnClickListener(this);
//        findViewById(R.id.ep_reader_button).setOnClickListener(this);
        findViewById(R.id.rc_reader_button).setOnClickListener(this);
//        findViewById(R.id.pinstatus_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        int id = view.getId();
        if (id == R.id.dl_reader_button) {
            intent = new Intent(getApplication(), DLReaderActivity.class);
            startActivity(intent);
        } else if (id == R.id.in_menu_button) {
            intent = new Intent(getApplication(), INMenuActivity.class);
            startActivity(intent);
        }
//        else if (id == R.id.ep_reader_button) {
//            intent = new Intent(getApplication(), EPReaderActivity.class);
//            startActivity(intent);
//        }
        else if (id == R.id.rc_reader_button) {
            intent = new Intent(getApplication(), RCReaderActivity.class);
            startActivity(intent);
        }
//        else if (id == R.id.pinstatus_button) {
//            intent = new Intent(getApplication(), PinStatusActivity.class);
//            startActivity(intent);
//        }
    }

}