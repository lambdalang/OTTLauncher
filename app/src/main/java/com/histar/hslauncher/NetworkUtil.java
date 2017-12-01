package com.histar.hslauncher;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;



public class NetworkUtil {

    private static final String TAG = "*** NetworkUtil ***";

    public static int getWifiRssi(Context context) {
        int ret;
        WifiManager wifi_service = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifi_service.getConnectionInfo();
        int level = wifiInfo.getRssi();
        if (level <= 0 && level >= -50) {
            ret = 3;
        } else if (level < -50 && level >= -75) {
            ret = 2;
        } else if (level < -75 && level >= -100) {
            ret = 1;
        } else {
            ret = 0;
        }
        return ret;
    }

    public static boolean isEthernetAvailable(Context context) {
        ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] info = mgr.getAllNetworkInfo();
        if (info != null) {
            for (int i = 0; i < info.length; i++) {
                if (info[i].getState() == NetworkInfo.State.CONNECTED && info[i].getType()==mgr.TYPE_ETHERNET) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isWifiAvailable(Context context) {
        ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] info = mgr.getAllNetworkInfo();
        if (info != null) {
            for (int i = 0; i < info.length; i++) {
                if (info[i].getState() == NetworkInfo.State.CONNECTED && info[i].getType()==mgr.TYPE_WIFI) {
                    return true;
                }
            }
        }
        return false;
    }
}
