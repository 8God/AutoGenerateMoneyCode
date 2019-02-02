package me.twobirds.pay.autogeneratemoneycode.service;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.List;

import me.twobirds.pay.autogeneratemoneycode.activity.GenerateQRCodeActivity;
import me.twobirds.pay.autogeneratemoneycode.activity.MainActivity;
import me.twobirds.pay.autogeneratemoneycode.base.CommonConstants;
import me.twobirds.pay.autogeneratemoneycode.base.MyApplication;
import me.twobirds.pay.autogeneratemoneycode.entity.GenerateQRCodeTask;

/**
 * | version | date        | author         | description
 * 0.0.1     2018/3/21       cth          init
 * <p>
 * desc:
 *
 * @author cth
 */

public class AlipayGenerateCodeService extends AccessibilityService {

    private static final String CLASS_AP_NOTICE_POP_DIALOG = "com.alipay.mobile.commonui.widget.APNoticePopDialog";

    private String currentActivity;

    public static GenerateQRCodeTask generateQRCodeTask;

    public static boolean isGeneratingCode = false; //是否正在走生成单张支付宝的流程
    public static boolean isSetMoney = false;       //是否设置金额
    public static boolean isSaveQRCode = false;     //是否保存二维码

    private ToastHandler toastHandler = new ToastHandler();

    class ToastHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (null == msg) {
                return;
            }
            Toast.makeText(getApplicationContext(), msg.obj + "", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        Log.i("cth", "accessibilityEvent = " + accessibilityEvent);
        Log.i("cth", "isGeneratingCode = " + isGeneratingCode);
        Log.i("cth", "isSaveQRCode = " + isSaveQRCode);
        Log.i("cth", "isSetMoney = " + isSetMoney);

        if (!MyApplication.getInstance().isStartGeneratingQRCode()) {  //非生成二维码操作则返回
            return;
        }

        if (CLASS_AP_NOTICE_POP_DIALOG.equals(accessibilityEvent.getClassName())) {
            clickDialogConfirmBtn(accessibilityEvent);
            return;
        }


        if (!isGeneratingCode) { //是否正在走生成单张支付宝的流程,是则不走该流程，否则进入该流程，取得要生成二维码的任务信息
            if (null == generateQRCodeTask) {
                generateQRCodeTask = MyApplication.getInstance().getGenerateQRCodeTask();
            }

            if (null == generateQRCodeTask) {
                finishGenerating();
                return;
            }

            if (null != generateQRCodeTask.getMoneyList() && generateQRCodeTask.getMoneyList().size() > 0) {
                generateQRCodeTask.setCurrentAccount(generateQRCodeTask.getMoneyList().remove(0));
            } else {
                finishGenerating();
                return;
            }
        }
        Log.i("cth", "accessibilityEvent = " + accessibilityEvent);
        generateQRCode(accessibilityEvent, generateQRCodeTask.getCurrentAccount());

//        generateQRCode1(accessibilityEvent, generateQRCodeTask.getCurrentAccount());
    }

    private void clickDialogConfirmBtn(AccessibilityEvent accessibilityEvent) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (null == accessibilityNodeInfo) {
            accessibilityNodeInfo = accessibilityEvent.getSource();
        }

        List<AccessibilityNodeInfo> confirmBtnList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.alipay.mobile.antui:id/ensure");

        if (null != confirmBtnList && confirmBtnList.size() > 0) {
            AccessibilityNodeInfo confirmBtn = confirmBtnList.get(0);
            if (null != confirmBtn) {
                confirmBtn.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    private void generateQRCode(AccessibilityEvent accessibilityEvent, float money) {
        if (null == accessibilityEvent) {
            return;
        }

        if (!MyApplication.getInstance().isStartGeneratingQRCode()) {
            return;
        }

        isGeneratingCode = true; //开始走生成单张二维码的流程

        if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == accessibilityEvent.getEventType()) {
            currentActivity = accessibilityEvent.getClassName().toString(); //记录当前Activity
        }

        if (CommonConstants.PAYEE_QRCODE_CLASS.equals(currentActivity)) { //如果在设置金额页面
            isSetMoney = checkIsSetMoney(accessibilityEvent); //检查是否设置金额
            if (!isSetMoney) {  //未设置金额则清除isClearMoney标志，然后点击进入设置金额的页面
                clickSettingMoney(accessibilityEvent);
            } else if (!isSaveQRCode) { //已设置金额但未保存二维码，则点击保存二维码按钮
                saveQRCode(accessibilityEvent);
            }
        } else if (CommonConstants.PAYEE_QRCODE_SET_MONEY_CLASS.equals(currentActivity)) { //进入设置金额页面
            if (!isSetMoney) {  //未设置金额
                setMoney(accessibilityEvent, money);  //设置金额
                confirmMoney(accessibilityEvent);  //并点击确定该金额按钮
            }
        }
    }


    private boolean checkIsSetMoney(AccessibilityEvent accessibilityEvent) {
        boolean isSetMoney = false;
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (null == accessibilityNodeInfo) {
            accessibilityNodeInfo = accessibilityEvent.getSource();
        }
        if (null == accessibilityNodeInfo) return false;

        List<AccessibilityNodeInfo> modifyMoneyNodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.alipay.mobile.payee:id/payee_QRCodePayModifyMoney");

        if (null != modifyMoneyNodeInfoList && modifyMoneyNodeInfoList.size() > 0) {
            AccessibilityNodeInfo modifyMoneyNodeInfo = modifyMoneyNodeInfoList.get(0);
            if (null != modifyMoneyNodeInfo) {
                if ("设置金额".equals(modifyMoneyNodeInfo.getText().toString())) {
                    isSetMoney = false;
                } else if ("清除金额".equals(modifyMoneyNodeInfo.getText().toString())) {
                    isSetMoney = true;
                }
            }
        }

        return isSetMoney;
    }

    private void clickSettingMoney(AccessibilityEvent accessibilityEvent) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (null == accessibilityNodeInfo) {
            accessibilityNodeInfo = accessibilityEvent.getSource();
        }
        if (null == accessibilityNodeInfo) return;
        List<AccessibilityNodeInfo> modifyMoneyNodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.alipay.mobile.payee:id/payee_QRCodePayModifyMoney");

        if (null != modifyMoneyNodeInfoList && modifyMoneyNodeInfoList.size() > 0) {
            AccessibilityNodeInfo modifyMoneyNodeInfo = modifyMoneyNodeInfoList.get(0);
            if (null != modifyMoneyNodeInfo) {
                if ("设置金额".equals(modifyMoneyNodeInfo.getText().toString())) {
                    modifyMoneyNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
    }

    private void setMoney(AccessibilityEvent accessibilityEvent, float money) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (null == accessibilityNodeInfo) {
            accessibilityNodeInfo = accessibilityEvent.getSource();
        }
        if (null == accessibilityNodeInfo) return;
        List<AccessibilityNodeInfo> setMoneyNodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.alipay.mobile.ui:id/content");

        if (null != setMoneyNodeInfoList && setMoneyNodeInfoList.size() > 0) {
            AccessibilityNodeInfo setMoneyNodeInfo = setMoneyNodeInfoList.get(0);
            if (null != setMoneyNodeInfo) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Bundle arguments = new Bundle();
                    arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, money + "");
                    setMoneyNodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                } else {
                    setMoneyNodeInfo.setText(money + "");
                }
            }
        }
    }

    private void confirmMoney(AccessibilityEvent accessibilityEvent) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (null == accessibilityNodeInfo) {
            accessibilityNodeInfo = accessibilityEvent.getSource();
        }
        if (null == accessibilityNodeInfo) return;
        List<AccessibilityNodeInfo> saveMoneyBtnNodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.alipay.mobile.payee:id/payee_NextBtn");
        if (null != saveMoneyBtnNodeInfoList && saveMoneyBtnNodeInfoList.size() > 0) {
            AccessibilityNodeInfo saveMoneyBtnNodeInfo = saveMoneyBtnNodeInfoList.get(0);
            if (null != saveMoneyBtnNodeInfo) {
                saveMoneyBtnNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    private void saveQRCode(AccessibilityEvent accessibilityEvent) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (null == accessibilityNodeInfo) {
            accessibilityNodeInfo = accessibilityEvent.getSource();
        }
        if (null == accessibilityNodeInfo) return;
        List<AccessibilityNodeInfo> saveMoneyNodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.alipay.mobile.payee:id/payee_save_qrcode");
        if (null != saveMoneyNodeInfoList && saveMoneyNodeInfoList.size() > 0) {
            AccessibilityNodeInfo saveMoneyNodeInfo = saveMoneyNodeInfoList.get(0);
            if (null != saveMoneyNodeInfo) {
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                saveMoneyNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                isSaveQRCode = true;
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                clearMoney(accessibilityEvent);
            }
        }
    }

    private void clearMoney(AccessibilityEvent accessibilityEvent) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (null == accessibilityNodeInfo) {
            accessibilityNodeInfo = accessibilityEvent.getSource();
        }
        if (null == accessibilityNodeInfo) return;
        List<AccessibilityNodeInfo> modifyMoneyNodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.alipay.mobile.payee:id/payee_QRCodePayModifyMoney");

        if (null != modifyMoneyNodeInfoList && modifyMoneyNodeInfoList.size() > 0) {
            AccessibilityNodeInfo modifyMoneyNodeInfo = modifyMoneyNodeInfoList.get(0);
            if (null != modifyMoneyNodeInfo && "清除金额".equals(modifyMoneyNodeInfo.getText().toString())) {
                modifyMoneyNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                isSetMoney = false;
                isSaveQRCode = false;
                isGeneratingCode = false;
            }
        }
    }


    private void generateQRCode1(AccessibilityEvent accessibilityEvent, float money) {
        if (null == accessibilityEvent) {
            return;
        }

        if (!MyApplication.getInstance().isStartGeneratingQRCode()) {
            return;
        }

        isGeneratingCode = true;

        if (CommonConstants.PAYEE_QRCODE_CLASS.equals(accessibilityEvent.getClassName().toString())) {
            currentActivity = CommonConstants.PAYEE_QRCODE_CLASS;

            AccessibilityNodeInfo payeeQRPageNodeInfo = getRootInActiveWindow();

            if (null != payeeQRPageNodeInfo) {
                List<AccessibilityNodeInfo> modifyMoneyNodeInfoList = payeeQRPageNodeInfo.findAccessibilityNodeInfosByViewId("com.alipay.mobile.payee:id/payee_QRCodePayModifyMoney");

                if (null != modifyMoneyNodeInfoList && modifyMoneyNodeInfoList.size() > 0) {
                    AccessibilityNodeInfo modifyMoneyNodeInfo = modifyMoneyNodeInfoList.get(0);
                    if (null != modifyMoneyNodeInfo) {
                        if ("设置金额".equals(modifyMoneyNodeInfo.getText().toString())) {
                            modifyMoneyNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        } else if ("清除金额".equals(modifyMoneyNodeInfo.getText().toString())) {
                            List<AccessibilityNodeInfo> saveMoneyNodeInfoList = payeeQRPageNodeInfo.findAccessibilityNodeInfosByViewId("com.alipay.mobile.payee:id/payee_save_qrcode");
                            if (null != saveMoneyNodeInfoList && saveMoneyNodeInfoList.size() > 0) {
                                AccessibilityNodeInfo saveMoneyNodeInfo = saveMoneyNodeInfoList.get(0);
                                if (null != saveMoneyNodeInfo) {
                                    saveMoneyNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                                    try {
                                        Thread.sleep(500L);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    modifyMoneyNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);

                                    isGeneratingCode = false;
                                    try {
                                        Thread.sleep(1000L);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }


                            }


                        }
                    }
                }
            }
        } else if (CommonConstants.PAYEE_QRCODE_SET_MONEY_CLASS.equals(accessibilityEvent.getClassName().toString())) {
            currentActivity = CommonConstants.PAYEE_QRCODE_SET_MONEY_CLASS;

            AccessibilityNodeInfo payeeSetMoneyPageNodeInfo = getRootInActiveWindow();
            List<AccessibilityNodeInfo> setMoneyNodeInfoList = payeeSetMoneyPageNodeInfo.findAccessibilityNodeInfosByViewId("com.alipay.mobile.ui:id/content");

            if (null != setMoneyNodeInfoList && setMoneyNodeInfoList.size() > 0) {
                AccessibilityNodeInfo setMoneyNodeInfo = setMoneyNodeInfoList.get(0);
                if (null != setMoneyNodeInfo) {
//                    setMoneyNodeInfo.setText(money + "");

//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
//                        ClipData clip = ClipData.newPlainText(label, text);
//                        clipboard.setPrimaryClip(clip);
//                        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
//                        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
//                    } else
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Bundle arguments = new Bundle();
                        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, money + "");
                        setMoneyNodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                    }


                }
            } else {
                Log.d("cth", "no set money");
            }

        } else if ("android.widget.Button".equals(accessibilityEvent.getClassName().toString())
                && CommonConstants.PAYEE_QRCODE_SET_MONEY_CLASS.equals(currentActivity)) {
            AccessibilityNodeInfo saveMoneyPageNodeInfo = getRootInActiveWindow();
            List<AccessibilityNodeInfo> saveMoneyBtnNodeInfoList = saveMoneyPageNodeInfo.findAccessibilityNodeInfosByViewId("com.alipay.mobile.payee:id/payee_NextBtn");
            if (null != saveMoneyBtnNodeInfoList && saveMoneyBtnNodeInfoList.size() > 0) {
                AccessibilityNodeInfo saveMoneyBtnNodeInfo = saveMoneyBtnNodeInfoList.get(0);
                if (null != saveMoneyBtnNodeInfo) {
                    saveMoneyBtnNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
    }

    @Override
    public void onInterrupt() {

    }

    private void finishGenerating() {
        MyApplication.getInstance().setStartGeneratingQRCode(false);
        MyApplication.getInstance().setGenerateQRCodeTask(null);

        isGeneratingCode = false;
        generateQRCodeTask = null;
        currentActivity = null;

        Intent openMain = new Intent(getApplicationContext(), GenerateQRCodeActivity.class);
        openMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        openMain.putExtra(GenerateQRCodeActivity.KEY_IS_GENRATING_QRCODE, false);
        getApplicationContext().startActivity(openMain);

        showToast("支付宝二维码生成完成");
    }

    private void showToast(String msg) {
        Message message = toastHandler.obtainMessage();
        message.obj = msg;

        toastHandler.sendMessage(message);
    }
}
