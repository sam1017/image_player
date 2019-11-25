package tyht.image_player.utils;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.util.Log;

/**
 * Created by lenovo on 2019/11/20.
 */

public class MediaDecoder {
    private static String TAG = "MediaDecoder";
    private String filepath = null;
    private MediaMetadataRetriever retriever = null;
    private String fileLength;

    public void initfilepath(String path){
        filepath = path;
    }

    public MediaDecoder(String path){
        if(Utils.checkFile(path)){
            retriever = new MediaMetadataRetriever();
            filepath = path;
            retriever.setDataSource(path);
            fileLength = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            Log.i(TAG,"fileLength = " + fileLength);
        }
    }
    /**
     * 获取视频某一帧
     * @param timeMs 毫秒
     * @param listener
     */
    public boolean decodeFrame(long timeMs,OnGetBitmapListener listener){
        if(retriever == null) return false;
        Bitmap bitmap = retriever.getFrameAtTime(timeMs * 1000, MediaMetadataRetriever.OPTION_CLOSEST);
        if(bitmap == null) return false;
        listener.getBitmap(bitmap, timeMs);
        return true;
    }
}
