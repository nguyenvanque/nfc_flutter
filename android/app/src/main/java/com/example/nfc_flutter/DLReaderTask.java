package com.example.nfc_flutter;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.nfc.Tag;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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
    private static final String TAG = MainActivity.TAG;
    private static final String DPIN = "****";
    private Tag nfcTag;
    private String pin1;
    private String pin2;
    private App activity;
    HashMap<String,String> hashMap=new HashMap<String,String>();


    public DLReaderTask(App activity, Tag nfcTag) {
        this.activity = activity;
        this.nfcTag = nfcTag;
    }

    private void publishProgress(String msg) {
         activity.print(msg);
    }

//    private void sendData(){
//        activity.getHasData(hashMap);
//    }

    public void run() {
        Log.d(TAG, getClass().getSimpleName() + "#run()");
//        this.activity.clear();
//        pin1 = activity.getPin1();
//        pin2 = activity.getPin2();
        pin1 = MainActivity.pin1;
        pin2 = MainActivity.pin2;

        activity.hideKeyboard();

//        publishProgress("# ????????????????????????????????????????????????????????????");
        // ???????????????????????????????????????
//       ProgressDialogFragment progress = new ProgressDialogFragment();
//        progress.show(activity., "progress");


        ProgressDialog progressdialog = new ProgressDialog(activity);
        progressdialog.setMessage("Loading....");
        progressdialog.show();


        try {
            JeidReader reader = new JeidReader(this.nfcTag);
//            publishProgress("## ????????????????????????????????????");
            CardType type = reader.detectCardType();
//            publishProgress("CardType: " + type);
            if (type != CardType.DL) {
//                publishProgress("????????????????????????????????????");
                return;
            }
            DriverLicenseAP ap = reader.selectDriverLicenseAP();

            // PIN???????????????????????????????????????????????????????????????
            // DriverLicenseAP#getCommonData()????????????????????????
            // PIN1??????????????????DriverLicenseAP#readFiles()????????????????????????
            // ????????????????????????????????????(PIN)????????????????????????????????????
            DriverLicenseFiles freeFiles = ap.readFiles();
            DriverLicenseCommonData commonData = freeFiles.getCommonData();
            DLPinSetting pinSetting = freeFiles.getPinSetting();
//            publishProgress("## ?????????????????????");
//            publishProgress(commonData.toString());
//            publishProgress("## ????????????(PIN)??????");
//            publishProgress(pinSetting.toString());



            if (pin1.isEmpty()) {
//                publishProgress("????????????1???????????????????????????");
                return;
            }
            if (!pinSetting.isPinSet()) {
//                publishProgress("????????????(PIN)?????????false???????????????????????????PIN??????****??????????????????????????????????????????\n");
                pin1 = DPIN;
            }

            try {
                ap.verifyPin1(pin1);
            } catch (InvalidPinException e) {
//                MainActivity.showInvalidPinDialog("????????????1", e);
                return;
            }

            if (!pin2.isEmpty()) {
                if (!pinSetting.isPinSet()) {
                    pin2 = DPIN;
                }
                try {
                    ap.verifyPin2(pin2);
                } catch (InvalidPinException e) {
//                    MainActivity.showInvalidPinDialog("????????????2", e);
                    return;
                }
            }
            // PIN?????????????????????DriverLicenseAP#readFiles()?????????????????????
            // ???????????????PIN?????????????????????????????????????????????????????????????????????
            // PIN1??????????????????????????????PIN2?????????????????????????????????(????????????)???????????????????????????
            DriverLicenseFiles files = ap.readFiles();

            // ?????????????????????
            DriverLicenseEntries entries = files.getEntries();
            // ???????????????
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
                // ???????????????1?????????????????????2???????????????????????????????????????????????????
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
                obj.put("dl-sc", pscName.replace("???????????????", ""));
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


            // ?????????????????????(????????????????????????
            DriverLicenseChangedEntries changedEntries = files.getChangedEntries();
            publishProgress(changedEntries.toString());

            hashMap.put("name",entries.getName().toString());
            hashMap.put("kana",entries.getKana());
            hashMap.put("birthDate",String.valueOf(entries.getBirthDate()));
            hashMap.put("licenseNumber",entries.getLicenseNumber());
            hashMap.put("entries",changedEntries.toString());

//            sendData();

            JSONArray changesObj = new JSONArray();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));

            //7
            if (changedEntries.isChanged()) {
                for (DriverLicenseChangedEntry entry : changedEntries.getNewAddrList()) {
                    JSONObject entryObj = new JSONObject();

                    entryObj.put("label", "?????????");
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
                    entryObj.put("label", "?????????");
                    entryObj.put("date", entry.getDate().toString());
                    entryObj.put("ad", sdf.format(entry.getDate().toDate()));
                    entryObj.put("value", new JSONArray(entry.getValue().toJSON()));
                    entryObj.put("psc", entry.getPsc());
                    changesObj.put(entryObj);
                }
                for (DriverLicenseChangedEntry entry : changedEntries.getNewConditionList()) {
                    JSONObject entryObj = new JSONObject();
                    entryObj.put("label", "?????????");
                    entryObj.put("date", entry.getDate().toString());
                    entryObj.put("ad", sdf.format(entry.getDate().toDate()));
                    entryObj.put("value", new JSONArray(entry.getValue().toJSON()));
                    entryObj.put("psc", entry.getPsc());
                    changesObj.put(entryObj);
                }
                for (DriverLicenseChangedEntry entry : changedEntries.getConditionCancellationList()) {
                    JSONObject entryObj = new JSONObject();
                    entryObj.put("label", "????????????");
                    entryObj.put("date", entry.getDate().toString());
                    entryObj.put("ad", sdf.format(entry.getDate().toDate()));
                    entryObj.put("value", new JSONArray(entry.getValue().toJSON()));
                    entryObj.put("psc", entry.getPsc());
                    changesObj.put(entryObj);
                }

            }

            try {
                // ???????????????
                DriverLicenseRegisteredDomicile registeredDomicile = files.getRegisteredDomicile();
                String value = registeredDomicile.getRegisteredDomicile().toJSON();
                if (value != null) {
                    obj.put("dl-registered-domicile", new JSONArray(value));
                }
//                publishProgress(registeredDomicile.toString());
                // ???????????????
                DriverLicensePhoto photo = files.getPhoto();
//                publishProgress("????????????????????????...");
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
                // ???????????????????????????????????????
                changedEntries = files.getChangedRegisteredDomicile();
                if (changedEntries.isChanged()) {
                    for (DriverLicenseChangedEntry entry : changedEntries.getNewRegisteredDomicileList()) {
                        JSONObject entryObj = new JSONObject();
                        entryObj.put("label", "?????????");
                        entryObj.put("date", entry.getDate().toString());
                        entryObj.put("ad", sdf.format(entry.getDate().toDate()));
                        entryObj.put("value", new JSONArray(entry.getValue().toJSON()));
                        entryObj.put("psc", entry.getPsc());
                        changesObj.put(entryObj);
                    }
                }
                // ?????????????????????
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

                // ???????????????
                ValidationResult result = files.validate();
                obj.put("dl-verified", result.isValid());
//                publishProgress("?????????????????????: " + result);
            } catch(FileNotFoundException e) {
                // PIN2????????????????????????files?????????????????????
                // FileNotFoundException???throw????????????
            } catch(UnsupportedOperationException e) {
                // free???????????????????????????????????????
                // UnsupportedOperationException ??????????????????
            }

            // ?????????????????????(????????????????????????????????????????????????????????????
            // ?????????????????????JSON?????????
            obj.put("dl-changes", changesObj);
            progressdialog.dismiss();


            // Viewer?????????
//            Intent intent = new Intent(activity, DLViewerActivity.class);
//            intent.putExtra("json", obj.toString());
//            activity.startActivity(intent);

//            Intent intent = new Intent(activity, MainActivity.class);
//            intent.putExtra("data",obj.toString());
//                intent.setAction(Intent.ACTION_RUN);
//                intent.putExtra("route", "/reader_page");
//            activity.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "error", e);
//            publishProgress("?????????: " + e);
        } finally {
             progressdialog.dismiss();
//            progress.dismissAllowingStateLoss();
        }
    }
}
