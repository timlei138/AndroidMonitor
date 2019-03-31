package com.lc.monitor.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.lc.monitor.CommCont;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ImageUtils {

    public static String SaveImage(Context context, byte[] bytes){
        String savePath = CommCont.getMediaDir(context,CommCont.TYPE_IMAGE);
        File file = new File(savePath,System.currentTimeMillis()+".jpg");
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



}
