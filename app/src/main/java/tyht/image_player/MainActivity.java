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
                    Bitmap outbitmap = comp(finalbitmap);

                    String S = "原始图片大小为：" + finalbitmap.getWidth() + "X" + finalbitmap.getHeight();
                    S += " 缩放后大小为：" + outbitmap.getWidth() + "X" + outbitmap.getHeight() ;
                    text.setText(S);
                    image.setImageBitmap(outbitmap);
                }
            });
        }


    }

    private Bitmap compressImage(Bitmap image) {
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

    private Bitmap comp(Bitmap image) {

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
}
