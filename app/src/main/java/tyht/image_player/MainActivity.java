package tyht.image_player;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import tyht.image_player.utils.Utils;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "newplayer";
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
        Utils.removeFile("/sdcard/Android_Image/filelist.txt");

        try{
            assets = getAssets();
            Images = assets.list("");
            String filelist = "filelist.txt";
            for(int i = 0; i < Images.length; i++){
                String filename = Images[i];
                Log.i(TAG," filename" + i + " = " + filename );
                if(filename.endsWith(".jpg")){
                    String newfilename = filename.substring(0, filename.indexOf(".jpg"));
                    Log.i(TAG," newfilename = " + newfilename);
                    Utils.writeTxtToFile(newfilename, Utils.getfilepath(), filelist);
                    RGBfilelist.add(newfilename);
                    InputStream srcInput = assets.open(filename);
                    Bitmap srcBitmap = BitmapFactory.decodeStream(srcInput,null, new BitmapFactory.Options());
                    Utils.processFile(Utils.comp(srcBitmap), newfilename);
                }
                //Utils.
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        InputStream input=null;
        try{
            input=assets.open(Images[currentImg++]);
        }catch(IOException e){
            e.printStackTrace();
        }

        BitmapDrawable bitmapDrawable=(BitmapDrawable)image.getDrawable();   //将image包装

        if(bitmapDrawable!=null&&!bitmapDrawable.getBitmap().isRecycled()){
            bitmapDrawable.getBitmap().recycle();
        }

        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, newOpts);
        String S = "原始图片大小为：" + newOpts.outWidth + "X" + newOpts.outHeight;
        text.setText(S);
        image.setImageBitmap(bitmap);
        int resize_width = 128;
        int old_width = (newOpts.outWidth > newOpts.outHeight)? newOpts.outHeight: newOpts.outWidth;

        if(old_width > resize_width){
            final Bitmap finalbitmap = bitmap;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG,"onClick");
                    Bitmap outbitmap = Utils.comp(finalbitmap);
                    String S = "原始图片大小为：" + finalbitmap.getWidth() + "X" + finalbitmap.getHeight();
                    S += " 缩放后大小为：" + outbitmap.getWidth() + "X" + outbitmap.getHeight() ;
                    text.setText(S);
                    image.setImageBitmap(outbitmap);
                    //Utils.ReadTxtFile("/sdcard/Android_Image/filelist.txt");
//                    for(int i = 0 ; i < RGBfilelist.size(); i++ ){
//                        String filename = Utils.getfilepath() + RGBfilelist.get(i);
//                        Log.i(TAG,"filename = " + filename);
//                        Utils.ReadTxtFile(filename);
//                    }
//                    Log.i(TAG,"ReadTextFile end ");
                }
            });
        }
        Log.i(TAG," OnCreate end ");
    }
}
