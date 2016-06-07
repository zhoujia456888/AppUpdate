package cn.zhoujia.appupdate.Util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.KeyEvent;

/**
 * Created by Zhoujia on 2016/6/7.
 */
public class Util {

    /**
     * 获取手机系统版本
     * @return
     */
    public static int getAndroidSDKVersion() {
        int version = 0;
        try {
            version = Integer.valueOf(android.os.Build.VERSION.SDK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }

    /**
     * 获取版本号
     *
     * @return
     */
    public static String getVersion(Context contect) {
        try {
            PackageManager manager = contect.getPackageManager();
            PackageInfo info = manager.getPackageInfo(contect.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "1.0.0";
        }
    }

    /**
     * 检查网络是否可用，判断是用手机网络还是WIFI网络或者模块热点.
     */
    public static Boolean checkNetType(Context context) {
        int type = getConnectedType(context);
        return type != -1;
    }

    /**
     * 判断当前网络是手机网络还是WIFI.
     *
     * @param context 上下文
     * @return ConnectedType 数据类型
     * <p/>
     * *
     */
    public static int getConnectedType(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            // 获取代表联网状态的NetWorkInfo对象
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            // 判断NetWorkInfo对象是否为空；判断当前的网络连接是否可用
            if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
                return mNetworkInfo.getType();
            }
        }
        return -1;
    }

    //强制更新点击返回按键Dialog不消失
    public static DialogInterface.OnKeyListener keylistener = new DialogInterface.OnKeyListener() {
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                return true;
            } else {
                return false;
            }
        }
    };

}
