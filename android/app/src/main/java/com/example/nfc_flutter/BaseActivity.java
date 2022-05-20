package com.example.nfc_flutter;

import android.annotation.SuppressLint;
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

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


public abstract class BaseActivity extends AppCompatActivity implements TagDiscoveredListener
{
    public static final String TAG = "JeidReader";
    protected NfcAdapter nfcAdapter;


    // NFC読み取りモード
    private final int NFC_AUTO_MODE = 0;
    private final int NFC_READER_MODE = 1;
    private final int NFC_FD_MODE = 2;
    protected int nfcMode;
    // ビューアーやメニュー画面ではNFC読み取りを無効化する
    // また、PIN間違いが発生してダイヤログを表示している間に
    // 連続読み取りが発生することを防ぐためのフラグ
    protected boolean enableNFC = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, getClass().getSimpleName() +
              "#onCreate(" + savedInstanceState + ")");
        super.onCreate(savedInstanceState);

        // NFC読み取りモードの設定値を取得
        SharedPreferences prefs = getSharedPreferences("settings", Context.MODE_PRIVATE);
        this.nfcMode = prefs.getInt("nfc_mode", NFC_AUTO_MODE);
        if (this.nfcMode == NFC_AUTO_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0以上はReaderModeを利用
                this.nfcMode = NFC_READER_MODE;
            } else {
                // Android 8.0未満はForegroundDispatchを利用
                this.nfcMode = NFC_FD_MODE;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onResume() {
        Log.d(TAG, getClass().getSimpleName() + "#onResume()");
        super.onResume();

        invalidateOptionsMenu();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            return;
        }

        if (this.nfcMode == NFC_READER_MODE) {
            Log.d(TAG, "NFC mode: ReaderMode");
            if(!this.enableNFC) {
                // メニュー画面やビューアーでNFC読み取りを無効化します
                // これを行わないと通常モード(OS標準)の読み取りが有効になるからです
                nfcAdapter.enableReaderMode(this, null, NfcAdapter.STATE_OFF, null);
                return;
            }
            Bundle options = new Bundle();
            //options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 500);
            nfcAdapter.enableReaderMode(this,
                                         new NfcAdapter.ReaderCallback() {
                                             @Override
                                             public void onTagDiscovered(Tag tag) {
                                                 BaseActivity.this.onTagDiscovered(tag);
                                             }
                                         },
                                         NfcAdapter.FLAG_READER_NFC_B | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                                         options);
        } else {
            Log.d(TAG, "NFC mode: ForegroundDispatch");
            if(!this.enableNFC) {
                return;
            }
            Intent intent = new Intent(this, this.getClass());
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
            String[][] techLists = new String[][] {
                new String[] { NfcB.class.getName() },
                new String[] { IsoDep.class.getName() }
            };
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, techLists);
        }
    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        Log.d(TAG, getClass().getSimpleName() + "#onPause()");
        super.onPause();
        if (nfcAdapter == null) {
            return;
        }
        if (nfcMode == NFC_READER_MODE) {
            nfcAdapter.disableReaderMode(this);
        } else {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    // ビューアーやメニューのActivityでこれが呼ばれます
    // サブクラスの**ReaderActivityでは適時overrideします
    public void onTagDiscovered(final Tag tag) {
        Log.d(TAG, getClass().getSimpleName() + "#onTagDiscovered()");
        Toast.makeText(this, "ビューアを閉じてください", Toast.LENGTH_LONG).show();
    }


    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    protected void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }
        View view = getCurrentFocus();
        if (view == null) {
            return;
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }



    protected void addMessage(String message) {
        TextView text = findViewById(R.id.message);
        text.setText(text.getText().toString() + "\n" + message);
        // 一番下にスクロール
        final ScrollView scroll = findViewById(R.id.scroll);
        scroll.post(new Runnable() {
                public void run() {
                    scroll.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
    }

    protected void clear() {
        TextView view =findViewById(R.id.message);
        view.post(new Runnable() {
                @Override
                public void run() {
                    view.setText("");
                }
            });
    }

    protected void print(String msg) {
        Handler handler = new Handler(Looper.getMainLooper());
        TextView text =findViewById(R.id.message);
        ScrollView scroll = findViewById(R.id.scroll);
        handler.post(new Runnable() {
                @Override
                public void run() {
                    text.setText(text.getText().toString() + msg + "\n");
                    // 一番下にスクロール
                    scroll.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
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
