package tyht.image_player;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image = (ImageView)findViewById(R.id.image);
        text = (TextView)findViewById(R.id.text);
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
                String title = uristring.substring(index);
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
                        Log.i(TAG,"filepath = " + filepath);
                        //Uri path_uri = MediaStore.Video.Media.getContentUri(filepath);
                        //Log.i(TAG,"path_uri = " + path_uri);
                        MediaMetadataRetriever object = new MediaMetadataRetriever();
                        object.setDataSource(filepath);
                        Bitmap saveBitmap = object.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST);
                        image.setImageBitmap(saveBitmap);
                        String saveFilename = title.substring(0,title.indexOf(".")) + ".jpg";
                        Log.i(TAG,"saveFilename = " + saveFilename);
                        try{
                            File file = new File(Utils.getfilepath() + saveFilename);
                            FileOutputStream out = new FileOutputStream(file);
                            saveBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            out.flush();
                            out.close();
                            Log.i(TAG,"save file = " + file.getAbsolutePath());
                            Log.w(TAG,"save picture finish!");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

//                ContentResolver cr = this.getContentResolver();
//                Cursor cursor = cr.query(uri, null, null, null, null);
//                if(cursor != null){
                    // 视频ID:MediaStore.Audio.Media._ID
                    //int videoId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                    // 视频名称：MediaStore.Audio.Media.TITLE
//                    String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
//                    // 视频路径：MediaStore.Audio.Media.DATA
//                    String videoPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
//                    // 视频时长：MediaStore.Audio.Media.DURATION
//                    int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
//                    // 视频大小：MediaStore.Audio.Media.SIZE
//                    long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
//
//                    // 视频缩略图路径：MediaStore.Images.Media.DATA
//                    String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
//                    // 缩略图ID:MediaStore.Audio.Media._ID
//                    int imageId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
//                    // 方法一 Thumbnails 利用createVideoThumbnail 通过路径得到缩略图，保持为视频的默认比例
//                    // 第一个参数为 ContentResolver，第二个参数为视频缩略图ID， 第三个参数kind有两种为：MICRO_KIND和MINI_KIND 字面意思理解为微型和迷你两种缩略模式，前者分辨率更低一些。
//                    Bitmap bitmap1 = MediaStore.Video.Thumbnails.getThumbnail(cr, imageId, MediaStore.Video.Thumbnails.MICRO_KIND, null);
//
//                    // 方法二 ThumbnailUtils 利用createVideoThumbnail 通过路径得到缩略图，保持为视频的默认比例
//                    // 第一个参数为 视频/缩略图的位置，第二个依旧是分辨率相关的kind
//                    Bitmap bitmap2 = ThumbnailUtils.createVideoThumbnail(imagePath, MediaStore.Video.Thumbnails.MICRO_KIND);
//                    // 如果追求更好的话可以利用 ThumbnailUtils.extractThumbnail 把缩略图转化为的制定大小
////                        ThumbnailUtils.extractThumbnail(bitmap, width,height ,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
//                    int width = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.WIDTH));
//                    int height = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT));
//                    String S= "视频名称：" + title +" 分辨率：" + width + "X" + height;
//                    text.setText(S);
//                    image.setImageBitmap(bitmap1);
//                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
