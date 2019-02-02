package me.twobirds.pay.autogeneratemoneycode.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

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

public class WechatGenerateCodeService extends AccessibilityService {

    private String currentActivity;

    private boolean isGeneratingCode = false;

    private GenerateQRCodeTask generateQRCodeTask;

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

        Log.i("cth", "MyApplication.getInstance().isStartGeneratingQRCode() = " + MyApplication.getInstance().isStartGeneratingQRCode());
        if (!MyApplication.getInstance().isStartGeneratingQRCode()) {
            return;
        }


        if (!isGeneratingCode) {
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


        generateQRCode(accessibilityEvent, generateQRCodeTask.getCurrentAccount());
    }


    private void generateQRCode(AccessibilityEvent accessibilityEvent, float money) {
        Log.i("cth", "money = " + money);
        if (null == accessibilityEvent) {
            return;
        }

        if (!MyApplication.getInstance().isStartGeneratingQRCode()) {
            return;
        }

        isGeneratingCode = true;

        if (CommonConstants.WECHAT_COLLECT_MAIN_UI.equals(accessibilityEvent.getClassName().toString())) {
            currentActivity = CommonConstants.WECHAT_COLLECT_MAIN_UI;

            Log.i("cth", "CommonConstants.WECHAT_COLLECT_MAIN_UI");

            AccessibilityNodeInfo payeeQRPageNodeInfo = getRootInActiveWindow();

            if (null != payeeQRPageNodeInfo) {
                List<AccessibilityNodeInfo> modifyMoneyNodeInfoList = payeeQRPageNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/akv");

                if (null != modifyMoneyNodeInfoList && modifyMoneyNodeInfoList.size() > 0) {
                    AccessibilityNodeInfo modifyMoneyNodeInfo = modifyMoneyNodeInfoList.get(0);
                    if (null != modifyMoneyNodeInfo) {
                        Log.i("cth", "modifyMoneyNodeInfo.getText() = " + modifyMoneyNodeInfo.getText());

                        if ("设置金额".equals(modifyMoneyNodeInfo.getText().toString())) {
                            Log.i("cth", "设置金额.equals");
//                            modifyMoneyNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            String content = exec("adb shell input tap 240 830");

                            Log.e("cth", "点击设置金额 : content = " + content);
                        } else if ("清除金额".equals(modifyMoneyNodeInfo.getText().toString())) {
                            List<AccessibilityNodeInfo> saveMoneyNodeInfoList = payeeQRPageNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/akw");
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
                } else {
                    Log.e("cth", "no modifyMoneyNodeInfoList");
                }
            } else {
                Log.e("cth", "null == payeeQRPageNodeInfo");
            }
        } else if (CommonConstants.WECHAT_COLLECT_CREATE_QRCODE_UI.equals(accessibilityEvent.getClassName().toString())) {
            currentActivity = CommonConstants.WECHAT_COLLECT_CREATE_QRCODE_UI;

            AccessibilityNodeInfo payeeSetMoneyPageNodeInfo = getRootInActiveWindow();
            List<AccessibilityNodeInfo> setMoneyNodeInfoList = payeeSetMoneyPageNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bt");

            if (null != setMoneyNodeInfoList && setMoneyNodeInfoList.size() > 0) {
                AccessibilityNodeInfo setMoneyNodeInfo = setMoneyNodeInfoList.get(0);
                if (null != setMoneyNodeInfo) {
                    Log.d("cth", "set money =  " + money);
                    Log.d("cth", "setMoneyNodeInfo.getClassName =  " + setMoneyNodeInfo.getClassName());
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

                        try {
                            Thread.sleep(500L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        List<AccessibilityNodeInfo> saveMoneyBtnNodeInfoList = payeeSetMoneyPageNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/akb");
                        if (null != saveMoneyBtnNodeInfoList && saveMoneyBtnNodeInfoList.size() > 0) {
                            AccessibilityNodeInfo saveMoneyBtnNodeInfo = saveMoneyBtnNodeInfoList.get(0);
                            if (null != saveMoneyBtnNodeInfo) {
                                saveMoneyBtnNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            }
                        }
                    }


                }
            } else {
                Log.d("cth", "no set money");
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

        Intent openMain = new Intent(getApplicationContext(), MainActivity.class);
        openMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(openMain);

        showToast("微信二维码生成完成");
    }

    private void showToast(String msg) {
        Message message = toastHandler.obtainMessage();
        message.obj = msg;

        toastHandler.sendMessage(message);
    }

    public void execShell(String cmd) {
        try {
            //权限设置
            Process p = Runtime.getRuntime().exec("sh");  //su为root用户,sh普通用户
            //获取输出流
            OutputStream outputStream = p.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            //将命令写入
            dataOutputStream.writeBytes(cmd);
            //提交命令
            dataOutputStream.flush();
            //关闭流操作
            dataOutputStream.close();
            outputStream.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public String exec(String cmd) {
        BufferedReader reader = null;
        String content = "";
        try {
            //("ps -P|grep bg")执行失败，PC端adb shell ps -P|grep bg执行成功
            //Process process = Runtime.getRuntime().exec("ps -P|grep tv");
            //-P 显示程序调度状态，通常是bg或fg，获取失败返回un和er
            // Process process = Runtime.getRuntime().exec("ps -P");
            //打印进程信息，不过滤任何条件
            Process process = Runtime.getRuntime().exec(cmd);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuffer output = new StringBuffer();
            int read;
            char[] buffer = new char[4096];
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();
            content = output.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content;

    }
}
