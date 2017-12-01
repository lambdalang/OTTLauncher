package com.histar.hslauncher;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;

public class AllAppsActivity extends Activity {

    private List<AppsItemInfo> list;
    private GridView gridview;
    private int selectedItemPosition = 0;
    private List<ResolveInfo> appList;
    private int position = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.allapps_activity);

        list = new ArrayList<AppsItemInfo>();
        gridview = (GridView) findViewById(R.id.gridview);

        updateAllApps();

        gridview.setOnItemClickListener(new ClickListener());
        gridview.setOnKeyListener(new View.OnKeyListener() {


            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                position = gridview.getSelectedItemPosition();
                if (keyCode == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_DOWN) {
                    unLoad(getApplicationContext(), appList.get(position));
                    selectedItemPosition = position;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        updateAllApps();
        super.onResume();
    }

    private void updateAllApps() {
        list.clear();
        PackageManager pManager = AllAppsActivity.this.getPackageManager();
        appList = getAllApps(AllAppsActivity.this);

        for (int i = 0; i < appList.size(); i++) {
            ResolveInfo pinfo = appList.get(i);
            AppsItemInfo shareItem = new AppsItemInfo();
            // set image
            shareItem.setIcon(pinfo.loadIcon(pManager));
            // set app name
            shareItem.setLabel(pinfo.loadLabel(pManager).toString());
            // set package name
            shareItem.setPackageName(pinfo.activityInfo.packageName);

            list.add(shareItem);
        }
        // set gridview's Adapter
        gridview.setAdapter(new AppsBaseAdapter());

        if (selectedItemPosition >= 0)
            gridview.setSelection(selectedItemPosition);
    }

    private static List<ResolveInfo> getAllApps(Context context) {
        List<ResolveInfo> apps = new ArrayList<ResolveInfo>();
        PackageManager packageManager = context.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> tempAppList = packageManager.queryIntentActivities(mainIntent, 0);
        for (ResolveInfo info : tempAppList) {
            if (!info.activityInfo.packageName.equals(context.getPackageName()))
                apps.add(info);
        }
        return apps;
    }

    private class AppsBaseAdapter extends BaseAdapter {
        LayoutInflater inflater = LayoutInflater.from(AllAppsActivity.this);

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.app, null);
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView
                        .findViewById(R.id.apps_image);
                holder.label = (TextView) convertView
                        .findViewById(R.id.apps_textview);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ResolveInfo rs = appList.get(position);
            holder.icon.setImageDrawable(list.get(position).getIcon());
            holder.label.setText(list.get(position).getLabel());

            return convertView;
        }
    }

    private class ViewHolder {
        private ImageView icon;
        private TextView label;
    }

    private class ClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            selectedItemPosition = arg2;
            Intent intent;
            intent = AllAppsActivity.this.getPackageManager().getLaunchIntentForPackage(list.get(arg2).getPackageName());
            startActivity(intent);
        }
    }

    private class AppsItemInfo {
        private Drawable icon;
        private String label;
        private String packageName;

        public Drawable getIcon() {
            return icon;
        }

        public void setIcon(Drawable icon) {
            this.icon = icon;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }
    }

    public boolean filterApp(ApplicationInfo info) {
        if ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
            return true;
        }
        return false;
    }

    public void unLoad(Context context, ResolveInfo info) {
        final ApplicationInfo appInfo = info.activityInfo.applicationInfo;
        String strinfo = getResources().getString(R.string.please_sure, info.loadLabel(getPackageManager()));
        /*if (filterApp(appInfo)) {
            //showToast(context, R.string.no_del_sys_app);
            Toast.makeText(getApplicationContext(), R.string.no_del_sys_app, Toast.LENGTH_SHORT).show();
        } else {
            String strUri = "package:" + appInfo.packageName;
            // Uri is used to access to uninstall the package name
            Uri uri = Uri.parse(strUri);
            Intent deleteIntent = new Intent();
            deleteIntent.setAction(Intent.ACTION_DELETE);
            deleteIntent.setData(uri);
            deleteIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(deleteIntent);
        }*/

        new AlertDialog.Builder(AllAppsActivity.this)
                .setTitle(strinfo)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // unLoad app here
                        // judge is a system application?
                        if (filterApp(appInfo)) {
                            //showToast(context, R.string.no_del_sys_app);
                            Toast.makeText(getApplicationContext(), R.string.no_del_sys_app, Toast.LENGTH_LONG).show();
                        } else {
                            String strUri = "package:" + appInfo.packageName;
                            // Uri is used to access to uninstall the package name
                            Uri uri = Uri.parse(strUri);
                            Intent deleteIntent = new Intent();
                            deleteIntent.setAction(Intent.ACTION_DELETE);
                            deleteIntent.setData(uri);
                            deleteIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getApplicationContext().startActivity(deleteIntent);
                        }
                    }

                }).setNegativeButton(R.string.details, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strUri = "package:" + appInfo.packageName;
                Uri uri = Uri.parse(strUri);
                Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setData(uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
            }
        }).create().show();
    }


}