package me.twobirds.pay.autogeneratemoneycode.base;

/**
 * | version | date        | author         | description
 * 0.0.1     2018/3/22       cth          init
 * <p>
 * desc:
 *
 * @author cth
 */

public class CommonConstants {

    public static final String ALIPAY_PKG_NAME = "com.eg.android.AlipayGphone";
    public static final String WECHAT_PKG_NAME = "com.tencent.mm";
    public static final String ALIPAY_MAIN_CLASS = "com.eg.android.AlipayGphone.AlipayLogin";
    public static final String PAYEE_QRCODE_CLASS = "com.alipay.mobile.payee.ui.PayeeQRActivity";
    public static final String PAYEE_QRCODE_SET_MONEY_CLASS = "com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity";

    public static final String WECHAT_COLLECT_MAIN_UI = "com.tencent.mm.plugin.collect.ui.CollectMainUI";//二维码收款主页
    public static final String WECHAT_COLLECT_CREATE_QRCODE_UI = "com.tencent.mm.plugin.collect.ui.CollectCreateQRCodeUI";//设置金额
}
