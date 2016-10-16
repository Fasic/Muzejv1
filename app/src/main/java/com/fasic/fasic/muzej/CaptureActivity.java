package com.fasic.fasic.muzej;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Locale;

public class CaptureActivity extends Activity {
    private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;
    private CameraManager mCameraManager;

    private TextView scanResult;
    private FrameLayout scanPreview;
    //private Button scanRestart;
    private RelativeLayout scanContainer;
    private RelativeLayout scanCropView;
    private ImageView scanLine;

    private Rect mCropRect = null;
    //private boolean barcodeScanned = false;
    private boolean previewing = true;
    private ImageScanner mImageScanner = null;

    private String jezik = "Srb";

    static {
        System.loadLibrary("iconv");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Intent intent = getIntent();
        jezik = intent.getStringExtra("jezik");
        setJezik(jezik);
        super.onCreate(savedInstanceState);

    }

    public void onResume(){
        super.onResume();
        releaseCamera();
        setContentView(R.layout.activity_capture);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//proveriti dal baguje!
        findViewById();
        initViews();
        setFont();

        ImageView logo = (ImageView) findViewById(R.id.logo);
        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void findViewById() {
        scanPreview = (FrameLayout) findViewById(R.id.capture_preview);
        //scanResult = (TextView) findViewById(R.id.capture_scan_result);
        //scanRestart = (Button) findViewById(R.id.capture_restart_scan);
        scanContainer = (RelativeLayout) findViewById(R.id.capture_container);
        scanCropView = (RelativeLayout) findViewById(R.id.capture_crop_view);
        scanLine = (ImageView) findViewById(R.id.capture_scan_line);
    }

	/*private void addEvents() {
		scanRestart.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (barcodeScanned) {
					barcodeScanned = false;
					scanResult.setText("Scanning...");
					mCamera.setPreviewCallback(previewCb);
					mCamera.startPreview();
					previewing = true;
					mCamera.autoFocus(autoFocusCB);
				}
			}
		});
	}*/

    private void initViews() {
        mImageScanner = new ImageScanner();
        mImageScanner.setConfig(0, Config.X_DENSITY, 3);
        mImageScanner.setConfig(0, Config.Y_DENSITY, 3);

        autoFocusHandler = new Handler();
        mCameraManager = new CameraManager(this);
        try {
            mCameraManager.openDriver();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCamera = mCameraManager.getCamera();
        mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
        scanPreview.addView(mPreview);

        TranslateAnimation animation = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.85f);
        animation.setDuration(3000);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.REVERSE);
        scanLine.startAnimation(animation);
    }

    public void onPause() {
        super.onPause();
        releaseCamera();
    }

    private void releaseCamera() {
        if (mCamera != null) {
            previewing = false;
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing)
                mCamera.autoFocus(autoFocusCB);
        }
    };

    Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Size size = camera.getParameters().getPreviewSize();


            byte[] rotatedData = new byte[data.length];
            for (int y = 0; y < size.height; y++) {
                for (int x = 0; x < size.width; x++)
                    rotatedData[x * size.height + size.height - y - 1] = data[x
                            + y * size.width];
            }

            int tmp = size.width;
            size.width = size.height;
            size.height = tmp;

            initCrop();

            Image barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(rotatedData);
            barcode.setCrop(mCropRect.left, mCropRect.top, mCropRect.width(),
                    mCropRect.height());

            int result = mImageScanner.scanImage(barcode);
            String resultStr = null;

            if (result != 0) {
                SymbolSet syms = mImageScanner.getResults();
                for (Symbol sym : syms) {
                    resultStr = sym.getData();
                }
            }

            if (!TextUtils.isEmpty(resultStr)) {
                previewing = false;
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();

                /*Intent dataIntent = new Intent();
                dataIntent.putExtra("Podatak", resultStr);
                setResult(Activity.RESULT_OK, dataIntent);*/

                //Toast.makeText(CaptureActivity.this, resultStr, Toast.LENGTH_SHORT).show(); //ovde ide send to webview

                Intent intent = new Intent(CaptureActivity.this, Prikaz.class);
                intent.putExtra("id", resultStr);
                intent.putExtra("jezik", jezik);
                startActivityForResult(intent, 0);
                //http://192.168.1.100/muzej/srb.html


                //finish();
            }
        }
    };

    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };

    private void initCrop() {
        int cameraWidth = mCameraManager.getCameraResolution().y;
        int cameraHeight = mCameraManager.getCameraResolution().x;


        float px = getResources().getDimension(R.dimen.header_height);
        int[] location = new int[2];
        scanCropView.getLocationInWindow(location);

        int cropLeft = location[0];
        int cropTop = location[1] - (int)px;

        int cropWidth = scanCropView.getWidth();
        int cropHeight = scanCropView.getHeight();


        int containerWidth = scanContainer.getWidth();
        int containerHeight = scanContainer.getHeight();


        int x = cropLeft * cameraWidth / containerWidth;
        int y = cropTop * cameraHeight / containerHeight;

        int width = cropWidth * cameraWidth / containerWidth;
        int height = cropHeight * cameraHeight / containerHeight;
        mCropRect = new Rect(x, y, width + x, height + y);
    }

    private int getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    protected void setJezik(String jezik){
        Locale locale;
        String drzava;

        if(jezik.equals("sr")){
            Log.i("-->", "srCA");
            drzava = "RS";
        }
        else{
            Log.i("-->","enCA");
            drzava = "US";
        }

        locale = new Locale(jezik, drzava);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    private void setFont(){
        Typeface font = Typeface.createFromAsset(getAssets(),  getResources().getString(R.string.font));
        font.isBold();

        TextView naslovTB = (TextView) findViewById(R.id.naslov);
        naslovTB.setTypeface(font);
        //scanResult.setTypeface(font);
    }
}
/*public class CaptureActivity extends Activity {
    private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;
    private CameraManager mCameraManager;

    private FrameLayout scanPreview;
    //private Button scanRestart;
    private RelativeLayout scanContainer;
    private RelativeLayout scanCropView;
    private ImageView scanLine;

    private Rect mCropRect = null;
    //private boolean barcodeScanned = false;
    private boolean previewing = true;
    private ImageScanner mImageScanner = null;

    private String jezik = "sr";

    static {
        System.loadLibrary("iconv");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Intent intent = getIntent();
        jezik = intent.getStringExtra("jezik");
        setJezik(jezik);
        super.onCreate(savedInstanceState);



    }

    public void onResume(){
        super.onResume();
        releaseCamera();
        setContentView(R.layout.activity_capture);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//proveriti dal baguje!
        findViewById();
        initViews();
        setFont();

        ImageView logo = (ImageView) findViewById(R.id.logo);
        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void findViewById() {
        scanPreview = (FrameLayout) findViewById(R.id.capture_preview);
        //scanRestart = (Button) findViewById(R.id.capture_restart_scan);
        scanContainer = (RelativeLayout) findViewById(R.id.capture_container);
        scanCropView = (RelativeLayout) findViewById(R.id.capture_crop_view);
        scanLine = (ImageView) findViewById(R.id.capture_scan_line);
    }OVDE*/

	/*private void addEvents() {
		scanRestart.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (barcodeScanned) {
					barcodeScanned = false;
					scanResult.setText("Scanning...");
					mCamera.setPreviewCallback(previewCb);
					mCamera.startPreview();
					previewing = true;
					mCamera.autoFocus(autoFocusCB);
				}
			}
		});
	}*/

    /*private void initViews() {
        mImageScanner = new ImageScanner();
        mImageScanner.setConfig(0, Config.X_DENSITY, 3);
        mImageScanner.setConfig(0, Config.Y_DENSITY, 3);

        autoFocusHandler = new Handler();
        mCameraManager = new CameraManager(this);
        try {
            mCameraManager.openDriver();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCamera = mCameraManager.getCamera();

        Camera.Parameters params = mCamera.getParameters();
        if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        } else if (params.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)){
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        mCamera.setParameters(params);


        mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
        scanPreview.addView(mPreview);

        TranslateAnimation animation = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.85f);
        animation.setDuration(3000);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.REVERSE);
        scanLine.startAnimation(animation);
    }

    public void onPause() {
        super.onPause();
        releaseCamera();
    }

    private void releaseCamera() {
        if (mCamera != null) {
            previewing = false;
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing)
                mCamera.autoFocus(autoFocusCB);
        }
    };

    Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Size size = camera.getParameters().getPreviewSize();


            byte[] rotatedData = new byte[data.length];
            for (int y = 0; y < size.height; y++) {
                for (int x = 0; x < size.width; x++)
                    rotatedData[x * size.height + size.height - y - 1] = data[x
                            + y * size.width];
            }

            int tmp = size.width;
            size.width = size.height;
            size.height = tmp;

            initCrop();

            Image barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(rotatedData);
            barcode.setCrop(mCropRect.left, mCropRect.top, mCropRect.width(),mCropRect.height());

            int result = mImageScanner.scanImage(barcode);
            String resultStr = null;

            if (result != 0) {
                SymbolSet syms = mImageScanner.getResults();
                for (Symbol sym : syms) {
                    resultStr = sym.getData();
                }
            }

            if (!TextUtils.isEmpty(resultStr)) {
                previewing = false;
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();OVDE*/

                /*Intent dataIntent = new Intent();
                dataIntent.putExtra("Podatak", resultStr);
                setResult(Activity.RESULT_OK, dataIntent);*/

                //Toast.makeText(CaptureActivity.this, resultStr, Toast.LENGTH_SHORT).show(); //ovde ide send to webview

               /* Intent intent = new Intent(CaptureActivity.this, Prikaz.class);
                intent.putExtra("id", resultStr);
                intent.putExtra("jezik", jezik);
                startActivityForResult(intent, 0);
                //http://192.168.1.100/muzej/srb.html


                //finish();
            }
        }
    };

    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            //autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };

    private void initCrop() {
        int cameraWidth = mCameraManager.getCameraResolution().y;
        int cameraHeight = mCameraManager.getCameraResolution().x;

        int[] location = new int[2];
        scanCropView.getLocationInWindow(location);

        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.header_height), r.getDisplayMetrics());

        int cropLeft = location[0];
        int cropTop = location[1] - (int)px;

        int cropWidth = scanCropView.getWidth();
        int cropHeight = scanCropView.getHeight();


        int containerWidth = scanContainer.getWidth();
        int containerHeight = scanContainer.getHeight();


        int x = cropLeft * cameraWidth / containerWidth;
        int y = cropTop * cameraHeight / containerHeight;

        int width = cropWidth * cameraWidth / containerWidth;
        int height = cropHeight * cameraHeight / containerHeight;
        mCropRect = new Rect(x, y, width + x, height + y);
    }

    private int getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    protected void setJezik(String jezik){
        Locale locale;
        String drzava;

        if(jezik.equals("sr")){
            drzava = "RS";
        }
        else{
            drzava = "US";
        }

        locale = new Locale(jezik, drzava);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    private void setFont(){
        Typeface font = Typeface.createFromAsset(getAssets(),  getResources().getString(R.string.font));
        font.isBold();

        TextView naslovTB = (TextView) findViewById(R.id.naslov);
        naslovTB.setTypeface(font);
    }
}*/
