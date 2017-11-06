package com.isaac.smartdrawer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends Activity {
    private static final long DELAY_TIME = 2000L;

    private static final String PREF_FIRST_LAUNCH = "isfirst";

    public static final int TAKE_PHOTO = 0;//定义启动相机
    public static final int CROP_PHOTO = 1;//定义启动相机之后的裁剪照片
    public static final int CHOOSE_PHOTO = 2;//定义从相册中查找照片
    private static boolean isCHOOSE_PHOTO = false;

    private String mImagePath;

    private ViewPager mPager;

    @BindView(R.id.user_head_portrait) ImageView mImageView;
    @BindView(R.id.set_user_name) EditText mUserName;
    @BindView(R.id.set_server_ip) EditText mHost;
    @BindView(R.id.set_server_port) EditText mPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (!getSharedPreferences(PREF_FIRST_LAUNCH, MODE_PRIVATE).getBoolean(PREF_FIRST_LAUNCH, true)) {
            setContentView(R.layout.activity_welcome);
            turnToMain();
        } else {
            setContentView(R.layout.activity_login);
            mImagePath = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "userImage.jpg").getAbsolutePath();
            mPager = (ViewPager) findViewById(R.id.login_pager);
            View login_first = getLayoutInflater().inflate(R.layout.login_first, null);
            View login_second = getLayoutInflater().inflate(R.layout.login_second, null);
            View login_third = getLayoutInflater().inflate(R.layout.login_third, null);
            View login_last = getLayoutInflater().inflate(R.layout.login_last, null);

            ButterKnife.bind(this, login_last);
            mImageView.setImageResource(R.drawable.login);

            final List<View> views = Arrays.asList(login_first, login_second, login_third, login_last);

            final PagerAdapter adapter = new PagerAdapter() {
                @Override
                public int getCount() {
                    return views.size();
                }

                @Override
                public boolean isViewFromObject(View view, Object object) {
                    return view == object;
                }

                @Override
                public void destroyItem(ViewGroup container, int position, Object object) {
                    container.removeView(views.get(position));
                }

                @Override
                public Object instantiateItem(ViewGroup container, int position) {
                    container.addView(views.get(position));
                    return views.get(position);
                }
            };

            mPager.setAdapter(adapter);

            mPager.setPageTransformer(true, new ParallaxTransformer(2f, 0.8f));
        }
    }

    private void turnToMain() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(ContentListPagerActivity.newIntent(LoginActivity.this));
                LoginActivity.this.finish();
            }
        }, DELAY_TIME);
    }

    @OnClick({R.id.user_login, R.id.user_head_portrait})
    void onUserLogin(View view) {
        switch (view.getId()) {
            case R.id.user_login:
                SettingsActivity.setUserName(this, mUserName.getText().toString());
                SettingsActivity.setAddr(this, mHost.getText() + ":" + mPort.getText());
                SharedPreferences.Editor editor = getSharedPreferences(PREF_FIRST_LAUNCH, MODE_PRIVATE).edit();
                editor.putBoolean(PREF_FIRST_LAUNCH, false);
                editor.apply();
                startActivity(ContentListPagerActivity.newIntent(this));
                finish();
                break;
            case R.id.user_head_portrait:
                final Dialog bottomsheet = new BottomSheetDialog(this);
                bottomsheet.setContentView(R.layout.dialog_bottom_sheet_photo);
                View.OnClickListener bottomsheetOnClick = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (view.getId()) {
                            case R.id.choose_from_camera:
                                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                Uri uri = Uri.fromFile(new File(mImagePath));
                                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                                startActivityForResult(cameraIntent, TAKE_PHOTO);
                                break;
                            case R.id.choose_from_album:
                                PackageManager pm = getPackageManager();
                                if (PackageManager.PERMISSION_GRANTED !=
                                        pm.checkPermission("android.permission.READ_EXTERNAL_STORAGE",
                                                "com.isaac.smartdrawer")) {
                                    View parent = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
                                    Snackbar.make(parent, "请允许应用访问存储！", Snackbar.LENGTH_LONG)
                                            .setAction("去设置", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    Intent intent = new Intent();
                                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                    intent.setData(Uri.fromParts("package", getPackageName(), null));
                                                    startActivity(intent);
                                                }
                                            })
                                            .show();
                                    break;
                                }

                                Intent pickIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                                pickIntent.addCategory(Intent.CATEGORY_OPENABLE);
                                pickIntent.setType("image/*");
                                startActivityForResult(pickIntent, CHOOSE_PHOTO);
                                break;
                        }
                        bottomsheet.dismiss();
                    }
                };
                bottomsheet.findViewById(R.id.choose_from_camera).setOnClickListener(bottomsheetOnClick);
                bottomsheet.findViewById(R.id.choose_from_album).setOnClickListener(bottomsheetOnClick);
                bottomsheet.show();
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO:
                Log.d("requestCode", "Need 0");
                if(resultCode == Activity.RESULT_OK){//启动相机
                    Log.d("resultCode", "OK!!!" + mImagePath);
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(Uri.fromFile(new File(mImagePath)), "image/*");
                    intent.putExtra("crop", true);
                    intent.putExtra("scale", true);
                    //aspectX aspectY 是宽高的比例，这里设置的是正方形（长宽比为1:1）
                    intent.putExtra("aspectX", 1);
                    intent.putExtra("aspectY", 1);
                    //outputX outputY 是裁剪图片宽高
                    intent.putExtra("outputX", 200);
                    intent.putExtra("outputY", 200);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mImagePath)));
                    startActivityForResult(intent, CROP_PHOTO);//相机拍照结束后进入裁剪界面
                }
                break;
            case CROP_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        displayImage(mImagePath);//裁剪后的照片显示
                    }

                }
                break;
            case CHOOSE_PHOTO://从相机获取照片
                if (resultCode == Activity.RESULT_OK) {
                    isCHOOSE_PHOTO = true;
                    if (Build.VERSION.SDK_INT >= 19) {
                        handleImageAbobeKitKat(data);
                    } else {
                        handleImageBeforeKitKat(data);
                    }
                }
                break;
            default:
                break;
        }
    }

    @TargetApi(19)
    private void handleImageAbobeKitKat(Intent data) {//打开相册使用
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(
                    uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" +id;
                imagePath = getImagePath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contenntUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contenntUri, null);
            }
        } else if("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);
        }
        displayImage(imagePath);
    }

    private void handleImageBeforeKitKat(Intent data) {//打开相册使用
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    private void displayImage(String imagePath) {//照片显示，拍照与打开相册共用
        if (imagePath != null) {
            if (isCHOOSE_PHOTO) {
                try {
                    fileCopy(imagePath, mImagePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isCHOOSE_PHOTO = false;
            }
            Log.d("LoginActivity", imagePath);
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            mImageView.setImageBitmap(bitmap);
        }
    }

    private String getImagePath(Uri uri, String selection) {//打开相册使用
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    public static boolean fileCopy(String oldFilePath,String newFilePath) throws IOException {//打开相册将选择的照片复制到指定目录
        //如果原文件不存在
        if(fileExists(oldFilePath) == false){
            return false;
        }

        File file = new File(newFilePath);
        if (file.exists())
            file.delete();
        //获得原文件流
        FileInputStream inputStream = new FileInputStream(new File(oldFilePath));
        byte[] data = new byte[1024];
        //输出流
        FileOutputStream outputStream =new FileOutputStream(new File(newFilePath));
        //开始处理流
        while (inputStream.read(data) != -1) {
            outputStream.write(data);
        }
        inputStream.close();
        outputStream.close();
        return true;
    }

    public static boolean fileExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }


    private class ParallaxTransformer implements ViewPager.PageTransformer {
        float mParallaxCoefficient;
        float mDistanceCoefficient;

        public ParallaxTransformer(float parallaxCoefficient, float distanceCoefficient) {
            mParallaxCoefficient = parallaxCoefficient;
            mDistanceCoefficient = distanceCoefficient;
        }

        @Override
        public void transformPage(View page, float position) {
            float scroolXOffset = page.getWidth() * mParallaxCoefficient;

            int[] layer = new int[]{R.id.user_head_portrait,R.id.set_user_name,R.id.set_server_ip,
                    R.id.set_server_port,R.id.user_login,
                    R.id.lamp,R.id.bed,R.id.sofa, R.id.clock,R.id.mirror,R.id.desk,R.id.bookshelf_1,
                    R.id.wardrobe,R.id.bookshelf,R.id.chair};

            for (int id : layer) {
                View view = page.findViewById(id);
                if (view != null) view.setTranslationX(scroolXOffset * position);
                scroolXOffset *= mDistanceCoefficient;
            }

        }
    }
}

