package me.twobirds.pay.autogeneratemoneycode.utils;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * | version | date        | author         | description
 *   0.0.1     2018/2/23     CTH              创建
 * <p>
 * desc:
 *
 * @author CTH
 * @version 0.0.1
 */
public class AccessibilityUtils {

    /**
     * 检测辅助功能是否开启<br>
     * 方 法 名：isAccessibilitySettingsOn <br>
     * 创 建 人 <br>
     * 创建时间：2016-6-22 下午2:29:24 <br>
     * 修 改 人： <br>
     * 修改日期： <br>
     *
     * @param mContext
     * @return boolean
     */
    public static boolean isAccessibilitySettingsOn(Context mContext, String serviceName) {
        int accessibilityEnabled = 0;
        serviceName =  mContext.getPackageName() + "/" + serviceName;
        // TestService为对应的服务
//        final String service = mContext.getPackageName() + "/" + TestService.class.getCanonicalName();
        Log.i(TAG, "serviceName : " + serviceName);
        // com.z.buildingaccessibilityservices/android.accessibilityservice.AccessibilityService
        try {
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: " + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            // com.z.buildingaccessibilityservices/com.z.buildingaccessibilityservices.TestService
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    Log.v(TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + serviceName);
                    if (accessibilityService.equalsIgnoreCase(serviceName)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v(TAG, "***ACCESSIBILITY IS DISABLED***");
        }
        return false;
    }

    public static void startAccessibilitySettings(Context mContext) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        mContext.startActivity(intent);

    }
}
