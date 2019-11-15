package tyht.image_player.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

/**
 * Created by lenovo on 2019/11/14.
 */

public class Utils {
    //private static String file_path = "/storage/emulated/0/image_player/";
    private static final String TAG = "newplayer";

    public static String getfilepath(){
        String file_path = "/sdcard/AndroidImage/"; //Environment.getExternalStorageDirectory().getAbsolutePath();
        //file_path = file_path + "/Android/";
        return file_path;
    }

    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        //循环判断如果压缩后图片是否大于100kb,大于继续压缩
        while ( baos.toByteArray().length / 1024>100) {
            //重置baos即清空baos
            baos.reset();
            //这里压缩options%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 10;//每次都减少10
        }
        //把压缩后的数据baos存放到ByteArrayInputStream中
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        //把ByteArrayInputStream数据生成图片
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }

    public static Bitmap comp(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        //判断如果图片大于1M,进行压缩避免在生成图片
        //（BitmapFactory.decodeStream）时溢出
        if( baos.toByteArray().length / 1024>1024) {
            baos.reset();//重置baos即清空baos
            //这里压缩50%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 256f;//这里设置高度为800f
        float ww = 256f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
    }

    // 将字符串写入到文本文件中
    public static void writeTxtToFile(String strcontent, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        //Log.i(TAG," begin writeTxtToFile strcontent = " + strcontent );
        makeFilePath(filePath, fileName);

        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        //Log.i(TAG," writeTxtToFile strFilePath = " + strFilePath );
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            //Log.i(TAG,"write end");
            raf.close();
        } catch (Exception e) {
            Log.e(TAG, "Error on write File:" + e);
        }
    }
    private static File makeFilePath(String filePath, String fileName) {
        //Log.i(TAG,"begin makeFilePath");
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Log.i(TAG,"makeFilePath file = " + file);
        return file;
    }

//生成文件夹

    private static void makeRootDirectory(String filePath) {
        File file = null;
        //Log.i(TAG,"makeRootDirectory filePath = " + filePath);
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i(TAG, e + "");
        }
    }

    public static String ReadTxtFile(String strFilePath)
    {
        String path = strFilePath;
        String content = ""; //文件内容字符串

        Log.i(TAG,"ReadTxtFile strFilePath = " + strFilePath);
        //打开文件
        File file = new File(path);
        //如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory())
        {
            Log.d(TAG, "The File doesn't not exist.");
        }
        else
        {
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null)
                {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    //分行读取
                    while (( line = buffreader.readLine()) != null) {
                        content += line + "\n";
                    }
                    instream.close();
                }
            }
            catch (java.io.FileNotFoundException e)
            {
                Log.d(TAG, "The File doesn't not exist.");
            }
            catch (IOException e)
            {
                Log.d(TAG, e.getMessage());
            }
        }
        //Log.i(TAG,"ReadTxtFile content = " + content);
        return content;
    }

    public static void removeFile(String strFilePath) {
        String path = strFilePath;
        File file = new File(path);
        Log.i(TAG,"file is exists = " + file.exists() + " begin delete");
        if(file.exists()&& file.isDirectory() == false){
            file.delete();
        }
        Log.i(TAG,"after delete file is exists = " + file.exists());
    }

    public static void processFile(Bitmap bitmap, String newfilename) {
        String store_filepath = getfilepath() + newfilename;
        removeFile(store_filepath);
        Log.i(TAG,"begin processFile newfilename = " + newfilename);
        Log.i(TAG,"bitmap width = " + bitmap.getWidth() + " height = " + bitmap.getHeight());
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int r = 0;
        if(w > h ){
            r = (int)(h/2);
        }else{
            r = (int)(w/2);
        }
        Log.i(TAG," processFile r = " + r);
        int start_x = (int)(w/2);
        int start_y = (int)(h/2);
        Log.i(TAG,"start_x = " + start_x + " start_y = " + start_y);

        for(int i = 0; i< 360; i++){
            int line = i+1;
            String temp_log = "";
            String red_line = "" + line +" red =";
            String green_line = "" + line + " green = ";
            String blue_line = "" + line + " blue = ";
            for(int j = 0; j < r; j ++){
                int temp_y = (int)(start_y + j * Math.sin(2*Math.PI*i/360));//从中心点竖直往下为第一帧，逆时针转
                int temp_x = (int)(start_x + j * Math.cos(2*Math.PI*i/360));
                int color = bitmap.getPixel(temp_x, temp_y);
                String red = Integer.toString(Color.red(color));
                String green = Integer.toString(Color.green(color));
                String blue = Integer.toString(Color.blue(color));
                red_line += red + " ";
                green_line += green + " ";
                blue_line += blue + " ";
                temp_log += " (" + temp_x + "," + temp_y +") = (" + red + "," + green + "," + blue + ") ";
            }
            writeTxtToFile(red_line, getfilepath(),newfilename);
            writeTxtToFile(green_line, getfilepath(),newfilename);
            writeTxtToFile(blue_line, getfilepath(),newfilename);
            Log.i(TAG," red_line = " + red_line);
            Log.i(TAG," green_line = " + green_line);
            Log.i(TAG," blue_line = " + blue_line);
        }

    }
}
