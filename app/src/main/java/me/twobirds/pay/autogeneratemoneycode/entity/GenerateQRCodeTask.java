package me.twobirds.pay.autogeneratemoneycode.entity;

import java.util.List;

/**
 * | version | date        | author         | description
 * 0.0.1     2018/3/22       cth          init
 * <p>
 * desc:
 *
 * @author cth
 */

public class GenerateQRCodeTask {

    private int moneyStart;
    private int moneyEnd;
    private int moneyGap;
    private int moneyGapFloatingCount;

    private float currentAccount = 0;

    private List<Float> moneyList;

    public int getMoneyStart() {
        return moneyStart;
    }

    public void setMoneyStart(int moneyStart) {
        this.moneyStart = moneyStart;
    }

    public int getMoneyEnd() {
        return moneyEnd;
    }

    public void setMoneyEnd(int moneyEnd) {
        this.moneyEnd = moneyEnd;
    }

    public int getMoneyGap() {
        return moneyGap;
    }

    public void setMoneyGap(int moneyGap) {
        this.moneyGap = moneyGap;
    }

    public int getMoneyGapFloatingCount() {
        return moneyGapFloatingCount;
    }

    public void setMoneyGapFloatingCount(int moneyGapFloatingCount) {
        this.moneyGapFloatingCount = moneyGapFloatingCount;
    }

    public float getCurrentAccount() {
        return currentAccount;
    }

    public void setCurrentAccount(float currentAccount) {
        this.currentAccount = currentAccount;
    }

    public List<Float> getMoneyList() {
        return moneyList;
    }

    public void setMoneyList(List<Float> moneyList) {
        this.moneyList = moneyList;
    }
}
