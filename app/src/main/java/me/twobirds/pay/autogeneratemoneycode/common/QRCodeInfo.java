package me.twobirds.pay.autogeneratemoneycode.common;

import java.util.ArrayList;
import java.util.List;

/**
 * | version | date        | author         | description
 * 0.0.1     2018/7/6       cth          init
 * <p>
 * desc:
 *
 * @author cth
 */
public class QRCodeInfo {

    private int screenWidth;
    private int screenHeight;
    private int codeX;
    private int codeY;
    private int codeWidth;

    public int getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }

    public int getCodeX() {
        return codeX;
    }

    public void setCodeX(int codeX) {
        this.codeX = codeX;
    }

    public int getCodeY() {
        return codeY;
    }

    public void setCodeY(int codeY) {
        this.codeY = codeY;
    }

    public int getCodeWidth() {
        return codeWidth;
    }

    public void setCodeWidth(int codeWidth) {
        this.codeWidth = codeWidth;
    }

    public static List<QRCodeInfo> getQRCodeInfoList() {
        List<QRCodeInfo> qrCodeInfoList = new ArrayList<>();

        QRCodeInfo screen_1260_1911 = new QRCodeInfo();
        screen_1260_1911.screenWidth = 1260;
        screen_1260_1911.screenHeight = 1911;
        screen_1260_1911.codeX = 252;
        screen_1260_1911.codeY = 651;
        screen_1260_1911.codeWidth = 755;
        qrCodeInfoList.add(screen_1260_1911);

        QRCodeInfo screen_1080_1638 = new QRCodeInfo();
        screen_1260_1911.screenWidth = 1080;
        screen_1260_1911.screenHeight = 1638;
        screen_1260_1911.codeX = 214;
        screen_1260_1911.codeY = 560;
        screen_1260_1911.codeWidth = 650;
        qrCodeInfoList.add(screen_1080_1638);

        QRCodeInfo screen_720_1092 = new QRCodeInfo();
        screen_1260_1911.screenWidth = 720;
        screen_1260_1911.screenHeight = 1092;
        screen_1260_1911.codeX = 143;
        screen_1260_1911.codeY = 372;
        screen_1260_1911.codeWidth = 435;
        qrCodeInfoList.add(screen_720_1092);


        return qrCodeInfoList;
    }
}
