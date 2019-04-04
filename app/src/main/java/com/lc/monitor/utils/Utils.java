package com.lc.monitor.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.util.Log;

import com.lc.mail.EmailMessage;
import com.lc.mail.tencent.TencentProtocolSmtp;
import com.lc.monitor.CommCont;
import com.lc.monitor.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Utils {

    private static final String TAG = "Utils";

    public static String SaveImage(Context context, byte[] bytes){
        String savePath = CommCont.getMediaDir(context,CommCont.TYPE_IMAGE);
        String name = CommCont.getRecordName(context);
        File file = new File(savePath,name+"-"+System.currentTimeMillis()+".jpg");
        Bitmap bitmap = null;
        FileOutputStream fos = null;
        try {
            file.createNewFile();
            fos = new FileOutputStream(file);
            bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            bitmap.compress(Bitmap.CompressFormat.JPEG,90,fos);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(bitmap!= null && !bitmap.isRecycled()){
                bitmap.recycle();
            }
        }
        return file != null ? file.getAbsolutePath() : "";
    }


    public static void sendEmail(Context context, final List<File> images){
        final EmailMessage message = new EmailMessage();
        message.setFromName(CommCont.SENDER_EMAIL_FROMNAME);
        message.setFromEmail(CommCont.SENDER_EMAIL_ADDRESS);
        message.setAccount(CommCont.SENDER_EMAIL_ACCOUNT);
        message.setAuthCode(CommCont.SENDER_EMAIL_AUTH);
        message.setTitle(context.getString(R.string.email_title));
        message.setMessage(context.getString(R.string.email_message));
        String email = CommCont.getWatchEmail(context);
        List<String> emailList= new ArrayList<>();
        emailList.add(email);
        message.setToAddress(emailList);
        message.setImageFiles(images);

        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                Log.d(TAG,"send Email--->");
                TencentProtocolSmtp smtp = new TencentProtocolSmtp();
                smtp.sendEmail(message);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                images.clear();
            }
        }.execute();
    }


    public static void sendSms(final Context context){
        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                Log.d(TAG,"send SMS--->");
                SmsManager smsManager = SmsManager.getDefault();
                String message = context.getString(R.string.sms_message);
                String phone = CommCont.getWatchPhone(context);
                smsManager.sendTextMessage(phone,null,message,null,null);
                return null;
            }
        }.execute();

    }



}
