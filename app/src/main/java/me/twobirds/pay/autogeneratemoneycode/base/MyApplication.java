package me.twobirds.pay.autogeneratemoneycode.base;

import android.app.Application;

import me.twobirds.pay.autogeneratemoneycode.entity.GenerateQRCodeTask;

/**
 * | version | date        | author         | description
 * 0.0.1     2018/3/22       cth          init
 * <p>
 * desc:
 *
 * @author cth
 */

public class MyApplication extends Application {

    private static MyApplication instance;

    private GenerateQRCodeTask generateQRCodeTask;

    private boolean isStartGeneratingQRCode = false;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
    }

    public static MyApplication getInstance() {
        return instance;
    }

    public GenerateQRCodeTask getGenerateQRCodeTask() {
        return generateQRCodeTask;
    }

    public void setGenerateQRCodeTask(GenerateQRCodeTask generateQRCodeTask) {
        this.generateQRCodeTask = generateQRCodeTask;
    }

    public boolean isStartGeneratingQRCode() {
        return isStartGeneratingQRCode;
    }

    public void setStartGeneratingQRCode(boolean startGeneratingQRCode) {
        isStartGeneratingQRCode = startGeneratingQRCode;
    }
}
