/**
 * Created by zhaohb on 15-8-14.
 */
package com.histar.hslauncher;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class StorageUtil {
    private static final String TAG = "StorageUtil";

    //获取内部SD分区挂载路径
    static public String getInternalSDPath() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ?
                Environment.getExternalStorageDirectory().toString() : null;
    }

    //获取外部设备所有挂载路径
    static public List<String> getVolumePaths(Context context) {
        List<String> stringList = new ArrayList<String>();
        try {
            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            Method getVolumePaths = StorageManager.class.getMethod("getVolumePaths");
            String[] volumePaths = (String[]) getVolumePaths.invoke(storageManager);
            if (volumePaths != null) {
                for (String path : volumePaths) {
                    if(!path.equals(getInternalSDPath()))
                        stringList.add(path);
                    Log.i(TAG, "volumePaths : " + path);
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return stringList;
    }

}
