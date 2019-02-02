package me.twobirds.pay.autogeneratemoneycode.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import java.util.List;


/**
 * | version | date        | author         | description
 * 0.0.1     2018/3/4       cth          init
 * <p>
 * desc:
 *
 * @author cth
 */

public class AppUtils {

    public static void openAppByPackageName(Context context, String packageName, int requestCode) throws PackageManager.NameNotFoundException {
        if (context == null || TextUtils.isEmpty(packageName)) {
            return;
        }

        PackageInfo pi;
        try {
            pi = context.getPackageManager().getPackageInfo(packageName, 0);
            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.setPackage(pi.packageName);
            PackageManager pManager = context.getPackageManager();
            List<ResolveInfo> apps = pManager.queryIntentActivities(resolveIntent, 0);
            ResolveInfo ri = apps.iterator().next();
            if (ri != null) {
                packageName = ri.activityInfo.packageName;
                String className = ri.activityInfo.name;
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);//重点是加这个
                ComponentName cn = new ComponentName(packageName, className);
                intent.setComponent(cn);
                if (requestCode < 0) {
                    context.startActivity(intent);
                } else if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(intent, requestCode);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void startAppSetting(Context context, String packageName, int requestCode) {
        if (context == null || TextUtils.isEmpty(packageName)) {
            return;
        }

        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        localIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        localIntent.setData(Uri.fromParts("package", packageName, null));
        if (requestCode < 0) {
            context.startActivity(localIntent);
        } else if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(localIntent, requestCode);
        }
    }

    public static String getMetaData(Context context, String key) {
        String value = null;

        ApplicationInfo appInfo;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (null != appInfo && appInfo.metaData != null) {
                value = appInfo.metaData.getString(key);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return value;
    }
}
