package tyht.image_player;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import tyht.image_player.utils.Utils;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "newplayer";
    private static final int REQUEST_VIDEO_CODE = 1;
    String[] Images = null;
    AssetManager assets = null;
    int currentImg=0;
    ImageView image;
    TextView text;
    Button button;
    ArrayList<String> RGBfilelist;
    ProgressDialog progressDialog;
    Bitmap coverBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image = (ImageView)findViewById(R.id.image);
        text = (TextView)findViewById(R.id.text);
        text.setText("请选择一个视频文件");
        button = (Button)findViewById(R.id.button);
        RGBfilelist = new ArrayList<>();
        Log.i(TAG," onCreate ");
        Utils.removeFile("/sdcard/AndroidImage/filelist.txt");

        try{
            assets = getAssets();
            Images = assets.list("");
            final String filelist = "filelist.txt";
            for(int i = 0; i < Images.length; i++){
                final String filename = Images[i];
                Log.i(TAG," filename" + i + " = " + filename );
                if(filename.endsWith(".jpg")){
                    new Thread() {
                        @Override
                        public void run() {
                             String newfilename = filename.substring(0, filename.indexOf(".jpg"));
                            Log.i(TAG," newfilename = " + newfilename);
                            Utils.writeTxtToFile(newfilename, Utils.getfilepath(), filelist);
                            RGBfilelist.add(newfilename);
                            InputStream srcInput = null;
                            try {
                                srcInput = assets.open(filename);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            final Bitmap srcBitmap = BitmapFactory.decodeStream(srcInput,null, new BitmapFactory.Options());
                            Utils.processFile(Utils.comp(srcBitmap), newfilename);
                        }
                    }.start();
                }
                //Utils.
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        initProgressDialog();
//        ExtractMpegFramesTest mTest = new ExtractMpegFramesTest();
//        try {
//            mTest.testExtractMpegFrames();
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_PICK);
                //intent.addCategory(Intent.CATEGORY_OPENABLE);
                //Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_VIDEO_CODE);
            }
        });
        Log.i(TAG," OnCreate end ");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_VIDEO_CODE){
            if(resultCode == RESULT_OK){
                Uri uri = data.getData();
                Log.i(TAG,"onActivityResult uri = " + uri);
                String uristring = uri.toString();
                Log.i(TAG,"uristring = " + uristring);
                if(uristring.startsWith("file:"));
                int index = uristring.lastIndexOf("/") + 1;
                final String title = uristring.substring(index);
                ContentResolver cr = this.getContentResolver();
                Uri baseUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;//获取媒体数据库的外部存储内容的Uri
                //String wherecause = "_data like = ?";
                Log.i(TAG,"title = " + title);

                Cursor cursor = cr.query(baseUri,null,"_data like ?", new String[]{"%" + title + "%"}, null);
                if(cursor != null){
                    cursor.moveToFirst();
                    Log.i(TAG,"count = " + cursor.getCount() + " cloumn = " + cursor.getColumnCount());
                    int width = 0;
                    int height = 0;
                    if((cursor.getColumnIndex(MediaStore.Video.Media.WIDTH))!= -1){
                         width = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.WIDTH));
                    }
                    if((cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT)) != -1){
                         height = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT));
                    }
                    String S= "视频名称：" + title +" 分辨率：" + width + "X" + height;
                    text.setText(S);
                    if(cursor.getColumnIndex(MediaStore.Video.Media.DATA) != -1){
                        String filepath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                        final MediaMetadataRetriever object = new MediaMetadataRetriever();
                        object.setDataSource(filepath);
                        final int duration = Integer.parseInt(object.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                        coverBitmap = object.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST);
                        image.setImageBitmap(coverBitmap);
                        Log.i(TAG,"filepath = " + filepath);
                        progressDialog.show();
                        //Uri path_uri = MediaStore.Video.Media.getContentUri(filepath);
                        //Log.i(TAG,"path_uri = " + path_uri);
                        new Thread(){
                            @Override
                            public void run() {
                                int index = 0;
                                Log.i(TAG,"duration = " + duration);
                                String saveFilename = title.substring(0,title.indexOf("."));
                                File newFolder = new File(Utils.getfilepath() + saveFilename + "/");
                                if(!newFolder.exists()){
                                    new File(Utils.getfilepath() + saveFilename + "/").mkdir();
                                }
                                Log.i(TAG,"is Folder exit = " + newFolder.exists() + " isDirectory = " + newFolder.isDirectory());
                                Log.i(TAG,"saveFilename = " + saveFilename);
                                for(int timeus = 0; timeus < duration; timeus += 42){
                                    index ++;
                                    Log.i(TAG,"begin save index = " + index);
                                    Log.w(TAG,"timeus = " + timeus);
                                    Bitmap saveBitmap = object.getFrameAtTime(timeus*1000, MediaMetadataRetriever.OPTION_CLOSEST);
                                    if(saveBitmap != null){
                                        try{
                                            File file = new File( newFolder, "image-" + index + ".jpg");
                                            FileOutputStream out = new FileOutputStream(file);
                                            saveBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                                            out.flush();
                                            out.close();
                                            Log.i(TAG,"save file = " + file.getAbsolutePath());
                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                //Bitmap saveBitmap = object.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST);
                                //image.setImageBitmap(saveBitmap);
                                //Log.i(TAG,"saveFilename = " + saveFilename);
                                if(progressDialog.isShowing()){
                                    progressDialog.dismiss();
                                }
                                Log.i(TAG,"save files finish !!!");
                                Uri uri = Uri.fromFile(newFolder);
                                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                                Log.w(TAG,"sendBroadcast uri = " + uri);
                            }
                        }.start();
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setIndeterminate(false);//循环滚动
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("loading...");
        progressDialog.setCancelable(false);//false不能取消显示，true可以取消显示
    }
}
