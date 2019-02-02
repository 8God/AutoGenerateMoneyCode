package me.twobirds.pay.autogeneratemoneycode.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import me.twobirds.pay.autogeneratemoneycode.R;
import me.twobirds.pay.autogeneratemoneycode.base.CommonConstants;
import me.twobirds.pay.autogeneratemoneycode.base.MyApplication;
import me.twobirds.pay.autogeneratemoneycode.entity.GenerateQRCodeTask;
import me.twobirds.pay.autogeneratemoneycode.service.AlipayGenerateCodeService;
import me.twobirds.pay.autogeneratemoneycode.utils.AccessibilityUtils;
import me.twobirds.pay.autogeneratemoneycode.utils.AppUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int QRCODE_TYPE_ALIPAY = 1;
    private static final int QRCODE_TYPE_WECHAT = 2;

    private EditText money_range_start;
    private EditText money_range_end;
    private EditText money_gap;
    private EditText money_gap_floating_count;
    private Button btn_generate_alipay_qrcode;
    private Button btn_get_qrcode_url;
    private Button btn_gap_1;
    private Button btn_gap_300;
    private Button btn_gap_1000;
    private Button btn_convenient_function;
//    private Button btn_generate_wechat_qrcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        money_range_start = findViewById(R.id.money_range_start);
        money_range_end = findViewById(R.id.money_range_end);
        money_gap = findViewById(R.id.money_gap);
        money_gap_floating_count = findViewById(R.id.money_gap_floating_count);

        btn_generate_alipay_qrcode = findViewById(R.id.btn_generate_alipay_qrcode);
//        btn_generate_wechat_qrcode = (Button) findViewById(R.id.btn_generate_wechat_qrcode);
//        btn_get_qrcode_url = (Button) findViewById(R.id.btn_get_qrcode_url);
        btn_gap_1 = findViewById(R.id.btn_gap_1);
        btn_gap_300 = findViewById(R.id.btn_gap_300);
        btn_gap_1000 = findViewById(R.id.btn_gap_1000);
        btn_convenient_function = findViewById(R.id.btn_convenient_function);

        btn_gap_1.setOnClickListener(this);
        btn_gap_300.setOnClickListener(this);
        btn_gap_1000.setOnClickListener(this);
        btn_convenient_function.setOnClickListener(this);

        btn_generate_alipay_qrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateQRcode(QRCODE_TYPE_ALIPAY);
            }
        });
//        btn_generate_wechat_qrcode.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                generateQRcode(QRCODE_TYPE_WECHAT);
//            }
//        });

//        btn_get_qrcode_url.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
//                        + "8zfb" + File.separator + "0.95.jpg";
//
//                Bitmap bitmap = FileUtils.transformFileToBitmap(filePath);
//                if (null != bitmap) {
//                    FileUtils.getUrl(bitmap);
//                }
//            }
//        });


//        ActivityCompat.requestPermissions(
//                this,
//                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                1);


    }

    private void generateQRcode(int qrcodeType) {
        GenerateQRCodeTask generateQRCodeTask = new GenerateQRCodeTask();

        String moneyStartStr = TextUtils.isEmpty(money_range_start.getText()) ? "" : money_range_start.getText().toString();
        if (!TextUtils.isEmpty(moneyStartStr)) {
            generateQRCodeTask.setMoneyStart(Integer.parseInt(moneyStartStr));
        } else {
            showToast("请输入金额下限");
            return;
        }

        String moneyEnd = TextUtils.isEmpty(money_range_end.getText()) ? "" : money_range_end.getText().toString();
        if (!TextUtils.isEmpty(moneyEnd)) {
            int moneyEndInt = Integer.parseInt(moneyEnd);

            if (moneyEndInt > generateQRCodeTask.getMoneyStart()) {
                generateQRCodeTask.setMoneyEnd(moneyEndInt);
            } else {
                showToast("金额上限必须大于金额下限");
                return;
            }

        } else {
            showToast("请输入金额上限");
            return;
        }

        String moneyGap = TextUtils.isEmpty(money_gap.getText()) ? "" : money_gap.getText().toString();
        if (!TextUtils.isEmpty(moneyGap)) {
            generateQRCodeTask.setMoneyGap(Integer.parseInt(moneyGap));
        } else {
            showToast("请输入金额间隔");
            return;
        }

        String moneyGapFloatingCount = TextUtils.isEmpty(money_gap_floating_count.getText()) ? "" : money_gap_floating_count.getText().toString();
        if (!TextUtils.isEmpty(moneyGapFloatingCount)) {
            generateQRCodeTask.setMoneyGapFloatingCount(Integer.parseInt(moneyGapFloatingCount));
        } else {
            showToast("请输入金额间隔浮动个数");
            return;
        }

        List<Float> moneyList = new ArrayList<>();
        float money = generateQRCodeTask.getMoneyStart();
        while (money <= generateQRCodeTask.getMoneyEnd()) {

            for (int i = generateQRCodeTask.getMoneyGapFloatingCount(); i > 0; i--) {
                moneyList.add((money - 0.01f * i));
            }

            moneyList.add(money);

            //暂时取消上浮
//            for (int i = 1; i <= generateQRCodeTask.getMoneyGapFloatingCount(); i++) {
//                moneyList.add((money + 0.01f * i));
//            }

            money += generateQRCodeTask.getMoneyGap();
        }

        generateQRCodeTask.setMoneyList(moneyList);

        MyApplication.getInstance().setGenerateQRCodeTask(generateQRCodeTask);
        MyApplication.getInstance().setStartGeneratingQRCode(true);
        try {

            switch (qrcodeType) {
                case QRCODE_TYPE_ALIPAY:
                    AppUtils.openAppByPackageName(this, CommonConstants.ALIPAY_PKG_NAME, -1); //启动支付宝
                    break;
                case QRCODE_TYPE_WECHAT:
                    AppUtils.openAppByPackageName(this, CommonConstants.WECHAT_PKG_NAME, -1); //启动微信
                    break;
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean isOpenAlipayGenerateCodeService = AccessibilityUtils.isAccessibilitySettingsOn(this, AlipayGenerateCodeService.class.getCanonicalName());

        if (!isOpenAlipayGenerateCodeService) {
            AccessibilityUtils.startAccessibilitySettings(this);
        }
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_gap_1:
                setInfo("1", "300", "1", "5");
                break;
            case R.id.btn_gap_300:
                setInfo("310", "1000", "10", "5");
                break;
            case R.id.btn_gap_1000:
                setInfo("1100", "5000", "100", "5");
                break;
            case R.id.btn_convenient_function:
                startActivity(new Intent(MainActivity.this,GenerateQRCodeActivity.class));
                break;
        }
    }

    private void setInfo(String start, String end, String gap, String floatCount) {
        money_range_start.setText(start);
        money_range_end.setText(end);
        money_gap.setText(gap);
        money_gap_floating_count.setText(floatCount);
    }
}
