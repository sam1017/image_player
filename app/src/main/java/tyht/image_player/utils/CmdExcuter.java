package tyht.image_player.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by lenovo on 2019/11/21.
 */

public class CmdExcuter {
    private static String TAG = "CmdExcuter";

    public static void exec(List<String> cmd, CmdOutputGetter getter) {

        Log.i(TAG,"exec command: ");
        StringBuilder sb = new StringBuilder();//StringBuilder是字符串生成器，上面这部分和cmd操作没关系
        for (String c : cmd) {
            sb.append(c).append(" ");
        }
        Log.i(TAG,sb.toString());

        //cmd操作部分
        try {
            ProcessBuilder builder = new ProcessBuilder();//创建新线程
            builder.command(cmd);//执行FFmpeg命令
            builder.redirectErrorStream(true);
            Process proc = builder.start();
            BufferedReader stdout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            while ((line = stdout.readLine()) != null) {
                if (getter != null)
                    getter.dealLine(line);
            }
            proc.waitFor();
            stdout.close();
        } catch (Exception e) {
            Log.e(TAG,e.getMessage(), e);
        }
    }

    public interface CmdOutputGetter {
        public void dealLine(String str);
    }
}
