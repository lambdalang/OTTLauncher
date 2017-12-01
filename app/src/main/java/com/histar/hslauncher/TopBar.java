package com.histar.hslauncher;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class TopBar extends RelativeLayout {

    private static final String TAG = "TopBar";
    private TextView mTimeView;
    private ImageView mSdcardView;
    private ImageView mUsbView;
    private ImageView mEtheView;
    private ImageView mWifiView;
    private View[] mViews;
    private Context mContext;
    private static final int CLOCK = 3;
    private static final int CLOCK_CYCLE = 30000;
    private int wifiStatus[] = {R.mipmap.wifi0,
            R.mipmap.wifi1,
            R.mipmap.wifi2,
            R.mipmap.wifi3};

    private boolean isTimeEnable = false;
    private boolean isSdcardEnable = false;
    private boolean isUsbEnable = false;
    private boolean isEtheEnable = false;
    private boolean isWifiEnable = false;

    public static final String FLAG_TIME = "time";
    public static final String FLAG_ETHE = "ethernet";
    public static final String FLAG_USB = "usb";
    public static final String FLAG_SD = "sdcard";
    public static final String FLAG_WIFI = "wifi";

    public TopBar(Context context ,AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.main_topbar, this);
        mContext = context;
        mViews = new View[]{
                mTimeView = (TextView) findViewById(R.id.timeView),
                mSdcardView = (ImageView) findViewById(R.id.sdcardView),
                mUsbView = (ImageView) findViewById(R.id.usbView),
                mEtheView = (ImageView) findViewById(R.id.netView),
                mWifiView = (ImageView) findViewById(R.id.wifiView)
        };
        try {
            for (View view : mViews) {
                view.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] a = {"time","ethernet", "usb"};
        setFlags(a);

        if (isTimeEnable) {
            Timer showTimeTimer = new Timer();
            showTimeTimer.schedule(new TimerTask() {
                public void run() {
                    showTime();
                }
            }, 2000, CLOCK_CYCLE);
        }

        if (isUsbEnable) {
            updateUsb(mContext);
        }

        IntentFilter netFilter = new IntentFilter();
        netFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(mainReceiver, netFilter);

        IntentFilter storageFilter = new IntentFilter();
        storageFilter.addAction("android.intent.action.MEDIA_MOUNTED");
        storageFilter.addAction("android.intent.action.MEDIA_UNMOUNTED");
        storageFilter.addDataScheme("file");
        mContext.registerReceiver(mainReceiver, storageFilter);
    }

    public void setFlags(String[] lstFlags) {
        for (String flag : lstFlags) {
            if (flag.equals(FLAG_TIME)) {
                isTimeEnable = true;
                // time show
            } else if (flag.equals(FLAG_ETHE)) {
                isEtheEnable = true;
                // net show
            } else if (flag.equals(FLAG_USB)) {
                isUsbEnable = true;
                // usb show
            }
        }
    }


    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CLOCK:
                    mTimeView.setText(msg.obj.toString());
                    mTimeView.setVisibility(View.VISIBLE);
                    break;
                default:
            }
        }
    };



    private BroadcastReceiver mainReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "BroadcastReceiver : " + action);
            if (isEtheEnable && action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                NetworkInfo netInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                switch (netInfo.getType()) {
                    case ConnectivityManager.TYPE_ETHERNET:
                        Log.i(TAG, "TYPE_ETHERNET isConnected = " + netInfo.isConnected());
                        mEtheView.setBackgroundResource(netInfo.isConnected() ? R.mipmap.ethernet1 : R.mipmap.ethernet0);
                        mEtheView.setVisibility(View.VISIBLE);
                        break;
                    case ConnectivityManager.TYPE_WIFI:
                        Log.i(TAG, "TYPE_WIFI isConnected = " + netInfo.isConnected());
                        mEtheView.setBackgroundResource(netInfo.isConnected() ? wifiStatus[NetworkUtil.getWifiRssi(context)] : R.mipmap.wifi0);
                        mEtheView.setVisibility(View.VISIBLE);
                        break;
                    default:
                }
            } else if (isUsbEnable && action.equals("android.intent.action.MEDIA_MOUNTED")) {
                updateUsb(context);
            } else if (isUsbEnable && action.equals("android.intent.action.MEDIA_UNMOUNTED")) {
                updateUsb(context);
            }
        }
    };

    public void showTime() {
        boolean b24Format = android.text.format.DateFormat.is24HourFormat(mContext.getApplicationContext());
        Calendar mCalendar = Calendar.getInstance();
        //mCalendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String currentZone = TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
        mCalendar.setTimeZone(TimeZone.getTimeZone(currentZone));
        //String strYear = String.valueOf(mCalendar.get(Calendar.YEAR));
        //String strMonth = String.format("%02d", mCalendar.get(Calendar.MONTH) + 1);
        //String strDay = String.format("%02d", mCalendar.get(Calendar.DAY_OF_MONTH));
        int nHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        String strAmPm = (nHour < 12) ? mContext.getString(R.string.am) : mContext.getString(R.string.pm);
        if (!b24Format && nHour > 12)
            nHour -= 12;
        String strHour = String.format("%02d", nHour);
        String strMinute = String.format("%02d", mCalendar.get(Calendar.MINUTE));
        //String strSecond = String.format("%02d", mCalendar.get(Calendar.SECOND));
        //String strWeek = String.valueOf(mCalendar.get(Calendar.DAY_OF_WEEK) - 1);
        String clockText = strHour + ":" + strMinute;

        if (!b24Format) {
            clockText += " " + strAmPm;
        }

        Message msg = TopBar.this.mHandler.obtainMessage(CLOCK, clockText);
        TopBar.this.mHandler.sendMessage(msg);
    }

    private void updateUsbView(int i) {
        mUsbView.setVisibility(i != 0 ? View.VISIBLE : View.GONE);
        TextView tvUsb = (TextView) findViewById(R.id.usbText);
        tvUsb.setVisibility(i != 0 ? View.VISIBLE : View.GONE);
        tvUsb.setText(String.valueOf(i));
    }

    private void updateSdcardView(int i) {
        ImageView ivSdcard = (ImageView) findViewById(R.id.sdcardView);
        ivSdcard.setVisibility(i != 0 ? View.VISIBLE : View.GONE);
    }

    private void updateUsb(Context context) {
        int storageNumber = StorageUtil.getVolumePaths(context).size();
        //int sdcardNumber = StorageUtil.getMountPathList("SDCARD").size();
        //updateSdcardView(sdcardNumber);
        updateUsbView(storageNumber);
    }

}
