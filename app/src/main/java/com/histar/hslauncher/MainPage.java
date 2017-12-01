package com.histar.hslauncher;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Locale;

public class MainPage extends RelativeLayout implements View.OnFocusChangeListener, View.OnClickListener {
    private static final String TAG = "MainPage";

    private MainActivity mContext;

    private ImageView[] views;
    private TextView[] texts;
    private RelativeLayout[] rLayouts;
    //private static final int MSG_WEATHER = 1;

    public MainPage(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = (MainActivity) context;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View parent = inflater.inflate(R.layout.main_page, this);
        initView(parent);

        IntentFilter filter = new IntentFilter();
        filter.addAction(WeatherIntentService.ACTION_REPORTWEATHER);
        context.registerReceiver(broadcastReceiver, filter);

        WeatherIntentService.startQueryWeather(context);
    }

    private void initView(View parent) {
        rLayouts = new RelativeLayout[]{
                (RelativeLayout) parent.findViewById(R.id.rLayout_1),
                (RelativeLayout) parent.findViewById(R.id.rLayout_2),
                (RelativeLayout) parent.findViewById(R.id.rLayout_3),
                (RelativeLayout) parent.findViewById(R.id.rLayout_4),
                (RelativeLayout) parent.findViewById(R.id.rLayout_5),
                (RelativeLayout) parent.findViewById(R.id.rLayout_6),
                (RelativeLayout) parent.findViewById(R.id.rLayout_7)
        };

        texts = new TextView[]{
                (TextView) findViewById(R.id.mainpage_text1),
                (TextView) findViewById(R.id.mainpage_text2),
                (TextView) findViewById(R.id.mainpage_text3),
                (TextView) findViewById(R.id.mainpage_text4),
                (TextView) findViewById(R.id.mainpage_text5),
                (TextView) findViewById(R.id.mainpage_text6)
        };

        for (RelativeLayout v : rLayouts) {
            v.setOnClickListener(this);
            v.getBackground().setAlpha(0);
            v.setOnFocusChangeListener(this);
        }

        rLayouts[0].requestFocus();
    }

    /*private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (!Thread.currentThread().isInterrupted()){
                switch (msg.what){
                    case MSG_WEATHER:
                        final ProgressDialog progressDialog = ProgressDialog.show(getContext(),getResources().getString(R.string.weather_info),getResources().getString(R.string.weather_update),false,true);
                        new Thread(){
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(4000);
                                    progressDialog.dismiss();
                                    Log.i("xxx", "progressDialog.dismiss");
                                    //DownloadActivity.this.finish();

                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        break;
                    default:
                        break;
                }
            }
        }
    };*/

    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WeatherIntentService.ACTION_REPORTWEATHER)) {
                String info = intent.getStringExtra("info");
                // info "city;temp;text;code"
                Log.i(TAG, "weather info:" + info);
                String[] infos = info.split(";");
                String city;
                if (infos.length == 4) {
                    int index = Integer.parseInt(infos[3]);
                    String text = getResources().getStringArray(R.array.weather)[index];
                    String code = WeatherIntentService.iconSelect(index);
                    TextView weather = (TextView) findViewById(R.id.weather);
                    TextView temp = (TextView) findViewById(R.id.temp);
                    TextView location = (TextView) findViewById(R.id.location);
//                    String able = getResources().getConfiguration().locale.getCountry();
                    String able = Locale.getDefault().getLanguage();
                    if (able.equals("zh")) {
                        try {
                            location.setText(py2Chinese(infos[0]));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        location.setText(infos[0]);
                    }

                    temp.setText(infos[1] + "℃\n");
                    weather.setText(text);
                    int resId = getResources().getIdentifier(code, "drawable", getContext().getPackageName());
                    /*Class drawable = R.drawable.class;
                    Field field = null;*/
                    ImageView ivIcon = (ImageView) findViewById(R.id.ivWeather);
                    ivIcon.setBackgroundResource(resId);


                   /* try {
                        field = drawable.getField("123");
                        int res_ID = field.getInt(field.getName());
                        ivIcon.setBackgroundResource(res_ID);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/


                }
            }
        }
    };

    public String readFile() {
        String fileName = "city.json"; //文件名字
        String res = "";
        try {
            //得到资源中的asset数据流
            InputStream in = getResources().getAssets().open(fileName);
            int length = in.available();
            byte[] buffer = new byte[length];

            in.read(buffer);
            in.close();
            res = EncodingUtils.getString(buffer, "UTF-8");
        } catch (Exception e) {

            e.printStackTrace();
        }
        return res;
    }


    public String py2Chinese(String args) throws JSONException {
        JSONArray arr = new JSONArray(readFile());
        for (int i = 0; i < arr.length(); i++) {
            JSONObject temp = (JSONObject) arr.get(i);
            String pinyin = temp.getString("pinyin");
            String name = temp.getString("name");
            if (pinyin.equals(args)) {
                    args = name;
            }
        }
        return args;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    public void onClick(View v) {
        String tag;
        if (v == rLayouts[0] || v == rLayouts[4]) {
            Intent intent = new Intent();
            if (v == rLayouts[0]) {
                intent.setAction("myvst.intent.action.VodTypeActivity");
            } else if (v == rLayouts[4]) {
                intent.setAction("myvst.intent.action.LivePlayer");
            }
            intent.putExtra("vodtype", "1");
            mContext.startActivity(intent);
            tag = "-";
        } else if (v == rLayouts[3]) {
            Intent intent = new Intent(mContext, AllAppsActivity.class);
            mContext.startActivity(intent);
            return;
        } else if (v == rLayouts[6]) {
            Log.i("2222", "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
            WeatherIntentService.startQueryWeather(getContext());
            Toast toast = Toast.makeText(getContext(),getResources().getString(R.string.weather_refresh),Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 730, 30);
            toast.show();
            /*Message message = new Message();
            message.what = MSG_WEATHER;
            mHandler.sendMessage(message);*/
            tag = "-";
        } else {
            tag = v.getTag().toString().trim();
        }
        if (tag.equals("-")) {
            return;
        }

        try {
            if (!tag.contains("|")) {
                Intent intent = getContext().getPackageManager().getLaunchIntentForPackage(tag);
                if (intent != null)
                    getContext().startActivity(intent);
            } else {
                String pkgs[] = tag.split("\\|");
                String packagename = pkgs[0];
                String activityname = pkgs[1];
                Intent i = new Intent();
                i.setClassName(packagename, activityname);
                getContext().startActivity(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        //Toast.makeText(this.getContext(), "focus changed", Toast.LENGTH_LONG).show();
        if (hasFocus) {
            v.bringToFront();
            v.getBackground().setAlpha(255);
            v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(Constant.SCARE_ANIMATION_DURATION).start();
        } else {
            v.getBackground().setAlpha(0);
            v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(Constant.SCARE_ANIMATION_DURATION).start();
        }
    }

    private String getAppName(String strPackageName) {
        PackageManager pm = mContext.getPackageManager();
        PackageInfo packageinfo = null;
        try {
            packageinfo = pm.getPackageInfo(strPackageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            return "";
        } else {
            return packageinfo.applicationInfo.loadLabel(pm).toString();
        }
        //info.strPackageName = strPackageName;
        //info.strClassName = packageinfo.applicationInfo.className;
        //info.strAppName = packageinfo.applicationInfo.loadLabel(pm).toString();
        //info.icon = packageinfo.applicationInfo.loadIcon(pm);
    }
}
