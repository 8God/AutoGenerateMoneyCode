package me.twobirds.pay.autogeneratemoneycode.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.twobirds.pay.autogeneratemoneycode.R;
import me.twobirds.pay.autogeneratemoneycode.base.CommonConstants;
import me.twobirds.pay.autogeneratemoneycode.base.MyApplication;
import me.twobirds.pay.autogeneratemoneycode.common.QRCodeInfo;
import me.twobirds.pay.autogeneratemoneycode.entity.GenerateQRCodeTask;
import me.twobirds.pay.autogeneratemoneycode.service.AlipayGenerateCodeService;
import me.twobirds.pay.autogeneratemoneycode.utils.AccessibilityUtils;
import me.twobirds.pay.autogeneratemoneycode.utils.AppUtils;

/**
 * | version | date        | author         | description
 * 0.0.1     2018/6/26       cth          init
 * <p>
 * desc:
 *
 * @author cth
 */
public class GenerateQRCodeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int FILE_SELECT_CODE = 0;
    public static final String KEY_IS_GENRATING_QRCODE = "KEY_IS_GENRATING_QRCODE";

    private List<Float> moneyList;
    private List<String> moneyStrList;
    private ProgressDialog progressDialog;

    private Button btn_generate_qrcode;
    private Button btn_continue_generate;

    private boolean isGenratingQRCode = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("cth", "onCreate");

        setContentView(R.layout.activity_generate_qrcode);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        requestStoragePermission();

        isGenratingQRCode = getIntent().getBooleanExtra(KEY_IS_GENRATING_QRCODE, isGenratingQRCode);

        init();


    }

    private void init() {
        btn_generate_qrcode = findViewById(R.id.btn_generate_qrcode);
        btn_continue_generate = findViewById(R.id.btn_continue_generate);
        Button btn_zip_qrcode = findViewById(R.id.btn_zip_qrcode);

        btn_generate_qrcode.setOnClickListener(this);
        btn_continue_generate.setOnClickListener(this);
        btn_zip_qrcode.setOnClickListener(this);

        progressDialog = new ProgressDialog(GenerateQRCodeActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (progressDialog.getProgress() < progressDialog.getMax()) { //当进度小于最大进度（即未完成），用户手动关闭对话框则重新显示
                    progressDialog.show();
                }
            }
        });

        refreshGeneratingBtn();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //检查是否开启AccessibilityService
        boolean isOpenAlipayGenerateCodeService = AccessibilityUtils.isAccessibilitySettingsOn(this, AlipayGenerateCodeService.class.getCanonicalName());

        if (!isOpenAlipayGenerateCodeService) {
            AccessibilityUtils.startAccessibilitySettings(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Log.i("cth", "onNewIntent");

        setIntent(intent);

        Intent mIntent = getIntent();

        isGenratingQRCode = mIntent.getBooleanExtra(KEY_IS_GENRATING_QRCODE, isGenratingQRCode);

        refreshGeneratingBtn();
    }

    /**
     * 初始化金额列表
     */
    private void initMoneyList() {
        GenerateQRCodeTask generateQRCodeTask1 = new GenerateQRCodeTask();
        generateQRCodeTask1.setMoneyStart(10);
        generateQRCodeTask1.setMoneyEnd(1000);
        generateQRCodeTask1.setMoneyGap(10);
        generateQRCodeTask1.setMoneyGapFloatingCount(9);

//        GenerateQRCodeTask generateQRCodeTask2 = new GenerateQRCodeTask();
//        generateQRCodeTask2.setMoneyStart(310);
//        generateQRCodeTask2.setMoneyEnd(1000);
//        generateQRCodeTask2.setMoneyGap(10);
//        generateQRCodeTask2.setMoneyGapFloatingCount(5);

        GenerateQRCodeTask generateQRCodeTask3 = new GenerateQRCodeTask();
        generateQRCodeTask3.setMoneyStart(1100);
        generateQRCodeTask3.setMoneyEnd(5000);
        generateQRCodeTask3.setMoneyGap(100);
        generateQRCodeTask3.setMoneyGapFloatingCount(9);

        GenerateQRCodeTask generateQRCodeTask4 = new GenerateQRCodeTask();
        generateQRCodeTask4.setMoneyStart(6000);
        generateQRCodeTask4.setMoneyEnd(10000);
        generateQRCodeTask4.setMoneyGap(1000);
        generateQRCodeTask4.setMoneyGapFloatingCount(9);


        List<GenerateQRCodeTask> generateQRCodeTaskList = new ArrayList<>();
        generateQRCodeTaskList.add(generateQRCodeTask1);
//        generateQRCodeTaskList.add(generateQRCodeTask2);
        generateQRCodeTaskList.add(generateQRCodeTask3);
        generateQRCodeTaskList.add(generateQRCodeTask4);

        DecimalFormat bigDecimal = new DecimalFormat("#.00");

        if (null != moneyList && moneyList.size() > 0) {
            moneyList.clear();
            moneyList = null;
        }

        if (null != moneyStrList && moneyStrList.size() > 0) {
            moneyStrList.clear();
            moneyStrList = null;
        }

        moneyList = new ArrayList<>();
        moneyStrList = new ArrayList<>();

        moneyList.add(0.91f);
        moneyStrList.add("0.91");

        for (int i = 0; i < generateQRCodeTaskList.size(); i++) {
            GenerateQRCodeTask mTask = generateQRCodeTaskList.get(i);

            float money = mTask.getMoneyStart();
            while (money <= mTask.getMoneyEnd()) {

                for (int j = mTask.getMoneyGapFloatingCount(); j > 0; j--) {
                    moneyList.add((money - 0.01f * j));
                    moneyStrList.add(((money - 0.01f * j)) + "");
                }

                moneyList.add(money);
                moneyStrList.add(bigDecimal.format(money));
                money += mTask.getMoneyGap();
            }

        }
        if (null != progressDialog) {
            progressDialog.setMax(moneyList.size());
        }

//        Log.i("cth","moneyStrList = " + moneyStrList);
//        Log.i("cth","moneyStrList size = " + moneyStrList.size());
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_generate_qrcode:
                if (isGenratingQRCode) {
                    isGenratingQRCode = false;
                    refreshGeneratingBtn();
                } else {
                    initMoneyList();

                    isGenratingQRCode = true;
                    refreshGeneratingBtn();

                    generateQRCode();
                }

                break;
            case R.id.btn_zip_qrcode:
                showFileChooser();

                break;
            case R.id.btn_continue_generate:
                AlipayGenerateCodeService.isSetMoney = false;
                AlipayGenerateCodeService.isSaveQRCode = false;

                try {
                    AppUtils.openAppByPackageName(this, CommonConstants.ALIPAY_PKG_NAME, -1); //启动支付宝

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                break;
        }
    }

    private void refreshGeneratingBtn() {
        if (isGenratingQRCode) {
            btn_generate_qrcode.setText("关闭 1-5000元 二维码生成");
            btn_generate_qrcode.setVisibility(View.VISIBLE);
            btn_continue_generate.setVisibility(View.VISIBLE);
        } else {
            MyApplication.getInstance().setGenerateQRCodeTask(null);
            MyApplication.getInstance().setStartGeneratingQRCode(false);

            AlipayGenerateCodeService.generateQRCodeTask = null;
            AlipayGenerateCodeService.isGeneratingCode = false;
            AlipayGenerateCodeService.isSaveQRCode = false;
            AlipayGenerateCodeService.isSetMoney = false;

            btn_generate_qrcode.setText("开始 1-5000元 二维码生成");
            btn_generate_qrcode.setVisibility(View.VISIBLE);
            btn_continue_generate.setVisibility(View.GONE);
        }
    }

    /**
     * 设置金额列表，打开支付宝开始生成二维码
     */
    private void generateQRCode() {
        GenerateQRCodeTask generateQRCodeTask = new GenerateQRCodeTask();
        generateQRCodeTask.setMoneyList(moneyList);

        MyApplication.getInstance().setGenerateQRCodeTask(generateQRCodeTask);
        MyApplication.getInstance().setStartGeneratingQRCode(true);

        try {
            AppUtils.openAppByPackageName(this, CommonConstants.ALIPAY_PKG_NAME, -1); //启动支付宝

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示选择文件的文件管理器
     */
    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    // Get the path
                    String path = null;
                    try {
                        path = getPath(this, uri);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }

                    if (TextUtils.isEmpty(path)) {
                        Toast.makeText(this, "路径选择错误", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    final String mPath = path;
                    if (!progressDialog.isShowing()) {
                        progressDialog.setProgress(0);
                        progressDialog.show();
                    }
                    new Thread(new Runnable() { //开启子线程开始修改名字并截图
                        @Override
                        public void run() {
                            handlerSelectedPath(mPath);
                        }
                    }).start();

                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it  Or Log it.
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * 处理选择的路径，包括新建文件夹、改名、截图并保持图片到新文件夹
     *
     * @param path
     */
    private void handlerSelectedPath(String path) {

        initMoneyList();

        /* 支付宝二维码保存图片规则：
         * 1、根据屏幕宽度，定下一个图片宽度，图片宽度小于等于屏幕宽度，所以不能直接获取屏幕宽度，而应该获取图片宽度；
         * 2、图片高度跟图片宽度比为91：60；
         * 3、二维码左上角的X坐标在图片宽度的0.2处；
         * 4、二维码左上角的Y坐标在图片的（31/91）处，大约三分之一；
         * 5、二维码在原图片的宽度等于图片的0.6宽度。
         *
         * 由以上规则，得到以下换算步骤
         */
        int width = getImageWidth(path);
        int height = width * 91 / 60;

        int codeX = (int) (width * 0.2f);   //二维码在源图片的x坐标
        int codeY = height * 31 / 91;      //二维码在源图片的y坐标
        int codeWidth = (int) (width * 0.6); //二维码在源图片的宽度

        String parentDirStr = path.substring(0, path.lastIndexOf("/")); //截取父目录
        File parentDir = new File(parentDirStr);

        if (!parentDir.exists()) return;

        File sortedDir = new File(parentDirStr + "_sorted");  //新建新文件夹，用来存放当前文件夹下截图改名后的二维码
        sortedDir.mkdir();

        File[] childFiles = parentDir.listFiles();
        List<File> sortedFileList = Arrays.asList(childFiles);
        Collections.sort(sortedFileList, new Comparator<File>() { //按文件名称排序
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && o2.isFile())
                    return -1;
                if (o1.isFile() && o2.isDirectory())
                    return 1;
                return o1.getName().compareTo(o2.getName());
            }
        });

        for (int i = 0; i < sortedFileList.size(); i++) {
            File childFile = sortedFileList.get(i);
            if (null != childFile && childFile.exists()) {
                File sortedFile = new File(sortedDir, moneyStrList.get(i) + ".jpg"); // 在新文件夹创建文件，并按MoneyList顺序命名，用来保存截图后的文件

                Bitmap bitmap = BitmapFactory.decodeFile(childFile.getAbsolutePath());
                Bitmap cutBitmap = cutBitmap(bitmap, codeX, codeY, codeWidth); //裁剪图片

                if (null == cutBitmap) { //当返回的截图为空时，停止截图操作
                    progressDialog.setProgress(progressDialog.getMax());
                    break;
                }

                saveImageFile(sortedFile, cutBitmap);
            }
            progressDialog.setProgress(i + 1);

        }
//        progressDialog.dismiss();
    }

    /**
     * 裁剪图片
     *
     * @param src 源图片的Bitmap
     * @return
     */
    private Bitmap cutBitmap(Bitmap src, int codeX, int codeY, int codeWidth) {

        //获取内置的屏幕信息列表，判断并获取对应的二维码在源图片的x、y坐标和二维码宽度
//        List<QRCodeInfo> qrCodeInfoList = QRCodeInfo.getQRCodeInfoList();
//        for (QRCodeInfo qrCodeInfo : qrCodeInfoList) {
//            if (qrCodeInfo.getScreenWidth() == width && qrCodeInfo.getScreenHeight() == height) {
//                codeX = qrCodeInfo.getCodeX();
//                codeY = qrCodeInfo.getCodeY();
//                codeWidth = qrCodeInfo.getCodeWidth();
//
//                break;
//            }
//        }
//        if (codeWidth == 0) {
//            return null;
//        }

        return Bitmap.createBitmap(src, codeX, codeY, codeWidth, codeWidth);
    }

    private boolean saveImageFile(File file, Bitmap imageBitmap) {
        boolean isSaved = true;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 70, out);// (quality:0-100)压缩文件
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

        FileOutputStream fileIn = null;
        try {
            fileIn = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = in.read(buffer)) != -1) {
                fileIn.write(buffer, 0, len);
            }
            fileIn.flush();
        } catch (FileNotFoundException e) {
            isSaved = false;
            e.printStackTrace();
        } catch (IOException e) {
            isSaved = false;
            e.printStackTrace();
        } finally {
            if (null != fileIn) {
                try {
                    fileIn.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }

        return isSaved;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(GenerateQRCodeActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
    }

    private int getImageWidth(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();

        /**
         * 最关键在此，把options.inJustDecodeBounds = true;
         * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
         */
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options); // 此时返回的bitmap为null
        /**
         *options.outHeight为原始图片的高
         */
        return options.outWidth;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_diycode:
                startActivity(new Intent(GenerateQRCodeActivity.this,MainActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
