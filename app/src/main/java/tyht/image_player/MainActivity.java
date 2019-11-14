package tyht.image_player;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import tyht.image_player.utils.Utils;

public class MainActivity extends AppCompatActivity {

    String[] Images = null;
    AssetManager assets = null;
    int currentImg=0;
    ImageView image;
    TextView text;
    Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image = (ImageView)findViewById(R.id.image);
        text = (TextView)findViewById(R.id.text);
        button = (Button)findViewById(R.id.button);

        try{
            assets = getAssets();
            Images = assets.list("");
            String filelist = "filelist.txt";
            for(int i = 0; i < Images.length; i++){
                String filename = Images[i];
                Utils.writeTxtToFile(filename, Utils.getfilepath(), filelist);
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
                    Bitmap outbitmap = Utils.comp(finalbitmap);

                    String S = "原始图片大小为：" + finalbitmap.getWidth() + "X" + finalbitmap.getHeight();
                    S += " 缩放后大小为：" + outbitmap.getWidth() + "X" + outbitmap.getHeight() ;
                    text.setText(S);
                    image.setImageBitmap(outbitmap);
                }
            });
        }
    }


}
