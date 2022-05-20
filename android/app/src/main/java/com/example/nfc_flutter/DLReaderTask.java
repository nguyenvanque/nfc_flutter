package com.example.nfc_flutter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.nfc.Tag;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import jp.co.osstech.libjeid.CardType;
import jp.co.osstech.libjeid.DriverLicenseAP;
import jp.co.osstech.libjeid.InvalidPinException;
import jp.co.osstech.libjeid.JeidReader;
import jp.co.osstech.libjeid.ValidationResult;
import jp.co.osstech.libjeid.dl.DLDate;
import jp.co.osstech.libjeid.dl.DLPinSetting;
import jp.co.osstech.libjeid.dl.DriverLicenseCategory;
import jp.co.osstech.libjeid.dl.DriverLicenseChangedEntries;
import jp.co.osstech.libjeid.dl.DriverLicenseChangedEntry;
import jp.co.osstech.libjeid.dl.DriverLicenseCommonData;
import jp.co.osstech.libjeid.dl.DriverLicenseEntries;
import jp.co.osstech.libjeid.dl.DriverLicenseExternalCharactors;
import jp.co.osstech.libjeid.dl.DriverLicenseFiles;
import jp.co.osstech.libjeid.dl.DriverLicensePhoto;
import jp.co.osstech.libjeid.dl.DriverLicenseRegisteredDomicile;
import jp.co.osstech.libjeid.dl.DriverLicenseSignature;
import jp.co.osstech.libjeid.util.BitmapARGB;
import jp.co.osstech.libjeid.util.Hex;

public class DLReaderTask implements Runnable {
    private static final String TAG = MainNfcActivity.TAG;
    private static final String DPIN = "****";
    private Tag nfcTag;
    private String pin1;
    private String pin2;
    private DLReaderActivity activity;
//    private  String name="氏名: ";
//    private  String nameKana="呼び名(カナ): ";
//    private  String birthday="生年月日: ";
//    private  String addressOrg="住所: ";
//    private  String expiredDate=" 有効期限の末日: ";
//    private  String licenseIdNo="免許証の番号: ";
//    private  String newAddress="新住所: ";


    public DLReaderTask(DLReaderActivity activity,
                        Tag nfcTag) {
        this.activity = activity;
        this.nfcTag = nfcTag;
    }

    private void publishProgress(String msg) {
        this.activity.print(msg);
    }

    public void run() {
        Log.d(TAG, getClass().getSimpleName() + "#run()");
        this.activity.clear();
        pin1 = activity.getPin1();
        pin2 = activity.getPin2();

        activity.hideKeyboard();

//        publishProgress("# 読み取り開始、カードを離さないでください");
        // 読み取り中ダイアログを表示
       ProgressDialogFragment progress = new ProgressDialogFragment();
        progress.show(activity.getSupportFragmentManager(), "progress");

        try {
            JeidReader reader = new JeidReader(this.nfcTag);
//            publishProgress("## 運転免許証の読み取り開始");
            CardType type = reader.detectCardType();
//            publishProgress("CardType: " + type);
            if (type != CardType.DL) {
//                publishProgress("運転免許証ではありません");
                return;
            }
            DriverLicenseAP ap = reader.selectDriverLicenseAP();

            // PINを入力せず共通データ要素を読み出す場合は、
            // DriverLicenseAP#getCommonData()を利用できます。
            // PIN1を入力せずにDriverLicenseAP#readFiles()を実行した場合、
            // 共通データ要素と暗証番号(PIN)設定のみを読み出します。
            DriverLicenseFiles freeFiles = ap.readFiles();
            DriverLicenseCommonData commonData = freeFiles.getCommonData();
       DLPinSetting pinSetting = freeFiles.getPinSetting();
//            publishProgress("## 共通データ要素");
//            publishProgress(commonData.toString());
//            publishProgress("## 暗証番号(PIN)設定");
//            publishProgress(pinSetting.toString());

            if (pin1.isEmpty()) {
//                publishProgress("暗証番号1を設定してください");
                return;
            }
            if (!pinSetting.isPinSet()) {
//                publishProgress("暗証番号(PIN)設定がfalseのため、デフォルトPINの「****」を暗証番号として使用します\n");
                pin1 = DPIN;
            }

            try {
                ap.verifyPin1(pin1);
            } catch (InvalidPinException e) {
                activity.showInvalidPinDialog("暗証番号1", e);
                return;
            }

            if (!pin2.isEmpty()) {
                if (!pinSetting.isPinSet()) {
                    pin2 = DPIN;
                }
                try {
                    ap.verifyPin2(pin2);
                } catch (InvalidPinException e) {
                    activity.showInvalidPinDialog("暗証番号2", e);
                    return;
                }
            }
            // PINを入力した後、DriverLicenseAP#readFiles()を実行すると、
            // 入力されたPINで読み出し可能なファイルをすべて読み出します。
            // PIN1のみを入力した場合、PIN2の入力が必要なファイル(本籍など)は読み出しません。
            DriverLicenseFiles files = ap.readFiles();

            // 券面情報の取得
            DriverLicenseEntries entries = files.getEntries();
            // 外字の取得
            DriverLicenseExternalCharactors extChars = files.getExternalCharactors();
            JSONObject obj = new JSONObject();
            // 1
            obj.put("dl-name", new JSONArray(entries.getName().toJSON()));
//            name=name+ String.valueOf(entries.getName());

            //2
            obj.put("dl-kana", entries.getKana());
//            nameKana=nameKana+entries.getKana();


            DLDate birthDate = entries.getBirthDate();
            if (birthDate != null) {
                //3
                obj.put("dl-birth", birthDate.toString());
//                birthday=birthday+birthDate.toString();
            }
            //4
            obj.put("dl-addr", new JSONArray(entries.getAddr().toJSON()));
//            addressOrg=addressOrg+entries.getAddr().toString();
            DLDate issueDate = entries.getIssueDate();
            if (issueDate != null) {
                obj.put("dl-issue", issueDate.toString());
            }
            obj.put("dl-ref", entries.getRefNumber());
            obj.put("dl-color-class", entries.getColorClass());
            DLDate expireDate = entries.getExpireDate();
            if (expireDate != null) {
                //5
                obj.put("dl-expire", expireDate.toString());
//                expiredDate=expiredDate+expireDate.toString();
                Calendar expireCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"));
                // 有効期限が1日までの場合、2日になった時点で有効期限切れとなる
                expireCal.setTime(expireDate.toDate());
                expireCal.add(Calendar.DAY_OF_MONTH, 1);
                Date now = new Date();
                boolean isExpired = now.compareTo(expireCal.getTime()) >= 0;
                obj.put("dl-is-expired",  isExpired);
            }
            //6
            obj.put("dl-number", entries.getLicenseNumber());
//            licenseIdNo=licenseIdNo+entries.getLicenseNumber();

            String pscName = entries.getPscName();
            if (pscName != null) {
                obj.put("dl-sc", pscName.replace("公安委員会", ""));
            }
            int i = 1;
            for (String condition : entries.getConditions()) {
                obj.put(String.format(Locale.US, "dl-condition%d", i++), condition);
            }
            JSONArray categories = new JSONArray();
            for (DriverLicenseCategory category : entries.getCategories()) {
                JSONObject categoryObj = new JSONObject();
                categoryObj.put("tag", category.getTag());
                categoryObj.put("name", category.getName());
                categoryObj.put("date", category.getDate().toString());
                categoryObj.put("licensed", category.isLicensed());
                categories.put(categoryObj);
            }
            obj.put("dl-categories", categories);

//            publishProgress(entries.toString());
            publishProgress(entries.getName().toString());
            publishProgress(entries.getKana());
            publishProgress(String.valueOf(entries.getBirthDate()));
            publishProgress(entries.getAddr().toString());
            publishProgress(expireDate.toString());
            publishProgress(entries.getLicenseNumber());
            // 記載事項変更等(本籍除く）を取得
            DriverLicenseChangedEntries changedEntries = files.getChangedEntries();
            publishProgress(changedEntries.toString());

            JSONArray changesObj = new JSONArray();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));

            //7
            if (changedEntries.isChanged()) {
                for (DriverLicenseChangedEntry entry : changedEntries.getNewAddrList()) {
                    JSONObject entryObj = new JSONObject();

                    entryObj.put("label", "新住所");
                    entryObj.put("date", entry.getDate().toString());
                    entryObj.put("ad", sdf.format(entry.getDate().toDate()));
                    entryObj.put("value", new JSONArray(entry.getValue().toJSON()));
                    entryObj.put("psc", entry.getPsc());
                    //7
//                    newAddress=newAddress+entry.getDate().toString()+ sdf.format(entry.getDate().toDate())+new JSONArray(entry.getValue().toJSON())+ entry.getPsc();
                    changesObj.put(entryObj);
                }
                for (DriverLicenseChangedEntry entry : changedEntries.getNewNameList()) {
                    JSONObject entryObj = new JSONObject();
                    entryObj.put("label", "新氏名");
                    entryObj.put("date", entry.getDate().toString());
                    entryObj.put("ad", sdf.format(entry.getDate().toDate()));
                    entryObj.put("value", new JSONArray(entry.getValue().toJSON()));
                    entryObj.put("psc", entry.getPsc());
                    changesObj.put(entryObj);
                }
                for (DriverLicenseChangedEntry entry : changedEntries.getNewConditionList()) {
                    JSONObject entryObj = new JSONObject();
                    entryObj.put("label", "新条件");
                    entryObj.put("date", entry.getDate().toString());
                    entryObj.put("ad", sdf.format(entry.getDate().toDate()));
                    entryObj.put("value", new JSONArray(entry.getValue().toJSON()));
                    entryObj.put("psc", entry.getPsc());
                    changesObj.put(entryObj);
                }
                for (DriverLicenseChangedEntry entry : changedEntries.getConditionCancellationList()) {
                    JSONObject entryObj = new JSONObject();
                    entryObj.put("label", "条件解除");
                    entryObj.put("date", entry.getDate().toString());
                    entryObj.put("ad", sdf.format(entry.getDate().toDate()));
                    entryObj.put("value", new JSONArray(entry.getValue().toJSON()));
                    entryObj.put("psc", entry.getPsc());
                    changesObj.put(entryObj);
                }

            }

            try {
                // 本籍を取得
                DriverLicenseRegisteredDomicile registeredDomicile = files.getRegisteredDomicile();
                String value = registeredDomicile.getRegisteredDomicile().toJSON();
                if (value != null) {
                    obj.put("dl-registered-domicile", new JSONArray(value));
                }
//                publishProgress(registeredDomicile.toString());
                // 写真を取得
                DriverLicensePhoto photo = files.getPhoto();
//                publishProgress("写真のデコード中...");
                BitmapARGB argb = photo.getPhotoBitmapARGB();
                Bitmap bitmap = Bitmap.createBitmap(argb.getData(),
                                                    argb.getWidth(),
                                                    argb.getHeight(),
                                                    Bitmap.Config.ARGB_8888);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                byte[] jpeg = os.toByteArray();
                String src = "data:image/jpeg;base64," + Base64.encodeToString(jpeg, Base64.DEFAULT);
                obj.put("dl-photo", src);
                // 記載事項変更（本籍）を取得
                changedEntries = files.getChangedRegisteredDomicile();
                if (changedEntries.isChanged()) {
                    for (DriverLicenseChangedEntry entry : changedEntries.getNewRegisteredDomicileList()) {
                        JSONObject entryObj = new JSONObject();
                        entryObj.put("label", "新本籍");
                        entryObj.put("date", entry.getDate().toString());
                        entryObj.put("ad", sdf.format(entry.getDate().toDate()));
                        entryObj.put("value", new JSONArray(entry.getValue().toJSON()));
                        entryObj.put("psc", entry.getPsc());
                        changesObj.put(entryObj);
                    }
                }
                // 電子署名を取得
                DriverLicenseSignature signature = files.getSignature();
                String signatureIssuer = signature.getIssuer();
//                publishProgress("Issuer: " + signatureIssuer);
                obj.put("dl-signature-issuer", signatureIssuer);
                String signatureSubject = signature.getSubject();
//                publishProgress("Subject: " + signatureSubject);
                obj.put("dl-signature-subject", signatureSubject);
                String signatureSKI = Hex.encode(signature.getSubjectKeyIdentifier(), ":");
//                publishProgress("Subject Key Identifier: " + signatureSKI);
                obj.put("dl-signature-ski", signatureSKI);

                // 真正性検証
                ValidationResult result = files.validate();
                obj.put("dl-verified", result.isValid());
//                publishProgress("真正性検証結果: " + result);
            } catch(FileNotFoundException e) {
                // PIN2を入力していないfilesオブジェクトは
                // FileNotFoundExceptionをthrowします。
            } catch(UnsupportedOperationException e) {
                // free版の場合、真正性検証処理で
                // UnsupportedOperationException が返ります。
            }

            // 記載事項変更等(本籍除く）と記載事項変更（本籍）合わせた
            // オブジェクトをJSONに追加
            obj.put("dl-changes", changesObj);

//            this.activity.showText(name+"\n"+nameKana+"\n"+birthday+"\n"+addressOrg+"\n"+expiredDate+"\n"+licenseIdNo+"\n"+newAddress);

            // Viewerを起動
            Intent intent = new Intent(activity, DLViewerActivity.class);
            intent.putExtra("json", obj.toString());
            activity.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "error", e);
//            publishProgress("エラー: " + e);
        } finally {
            progress.dismissAllowingStateLoss();
        }
    }
}
